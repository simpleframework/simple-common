package net.simpleframework.common.jedis;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.simpleframework.common.Convert;
import net.simpleframework.common.SerializeUtils;
import net.simpleframework.common.logger.Log;
import net.simpleframework.common.logger.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class JedisMap extends HashMap<String, Object> {
	private final int expire;

	private final boolean hash;
	private final String mkey;

	private JedisPool pool;

	public JedisMap(final JedisPool _pool, final boolean _hash, final String _mkey,
			final int _expire) {
		pool = _pool;
		hash = _hash;
		mkey = hash ? getClass().getSimpleName() + ":" + _mkey : _mkey;
		expire = _expire;

		try {
			if (pool != null) {
				pool.getResource().close();
			}
		} catch (final Throwable e) {
			pool = null;
		}
	}

	public JedisMap(final JedisPool _pool, final String _mkey, final int _expire) {
		this(_pool, true, _mkey, _expire);
	}

	public JedisMap(final JedisPool _pool, final String _key) {
		this(_pool, _key, 0);
	}

	private String gkey(final String sk) {
		return mkey + ":" + sk;
	}

	@Override
	public Object get(final Object key) {
		if (pool != null) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				final String sk = Convert.toString(key);
				if (hash) {
					return deserialize(jedis.hget(mkey.getBytes(), sk.getBytes()));
				} else {
					return deserialize(jedis.get(gkey(sk).getBytes()));
				}
			} catch (final Exception e) {
				log.warn(e);
				return null;
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		} else {
			return super.get(key);
		}
	}

	@Override
	public Object put(final String key, final Object value) {
		if (pool != null) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				if (value == null) {
					remove(key);
					return null;
				} else {
					if (hash) {
						final byte[] sbytes = mkey.getBytes();
						final boolean set_expire = expire > 0 && !jedis.exists(sbytes);
						final Long ret = jedis.hset(sbytes, key.getBytes(), serialize(value));
						if (set_expire) {
							jedis.expire(sbytes, expire);
						}
						return ret;
					} else {
						if (expire > 0) {
							return jedis.setex(gkey(key).getBytes(), expire, serialize(value));
						} else {
							return jedis.set(gkey(key).getBytes(), serialize(value));
						}
					}
				}
			} catch (final IOException e) {
				log.warn(e);
				return null;
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		} else {
			return super.put(key, value);
		}
	}

	@Override
	public Object remove(final Object key) {
		if (pool != null) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				final String sk = Convert.toString(key);
				if (hash) {
					final byte[] sbytes = mkey.getBytes();
					final Long ret = jedis.hdel(sbytes, sk.getBytes());
					if (jedis.hlen(sbytes) == 0) {
						jedis.del(sbytes);
					}
					return ret;
				} else {
					return jedis.del(gkey(sk).getBytes());
				}
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		} else {
			return super.remove(key);
		}
	}

	@Override
	public boolean containsKey(final Object key) {
		if (pool != null) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				final String sk = Convert.toString(key);
				if (hash) {
					return jedis.hexists(mkey.getBytes(), sk.getBytes());
				} else {
					return jedis.exists(gkey(sk).getBytes());
				}
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		} else {
			return super.containsKey(key);
		}
	}

	@Override
	public Set<String> keySet() {
		if (pool != null) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				Set<byte[]> _set;
				if (hash) {
					_set = jedis.hkeys(mkey.getBytes());
				} else {
					_set = jedis.keys((mkey + ":*").getBytes());
				}
				final Set<String> set = new HashSet<>();
				for (final byte[] k : _set) {
					set.add(new String(k));
				}
				return set;
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		} else {
			return super.keySet();
		}
	}

	private byte[] serialize(final Object obj) throws IOException {
		return SerializeUtils.serialize(obj);
	}

	private Object deserialize(final byte[] bytes) throws IOException, ClassNotFoundException {
		return SerializeUtils.deserialize(bytes);
	}

	private static Log log = LogFactory.getLogger(JedisMap.class);

	private static final long serialVersionUID = 379414188959312886L;
}
