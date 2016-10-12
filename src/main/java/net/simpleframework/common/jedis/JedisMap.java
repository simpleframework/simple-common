package net.simpleframework.common.jedis;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.simpleframework.common.Convert;
import net.simpleframework.common.IoUtils;
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

	private final String mkey;

	private JedisPool pool;

	public JedisMap(final JedisPool _pool, final String _mkey, final int _expire) {
		pool = _pool;
		mkey = getClass().getSimpleName() + ":" + _mkey;
		expire = _expire;
		try {
			if (pool != null) {
				pool.getResource().close();
			}
		} catch (final Throwable e) {
			pool = null;
		}
	}

	public JedisMap(final JedisPool _pool, final String _key) {
		this(_pool, _key, 0);
	}

	@Override
	public Object get(final Object key) {
		if (pool != null) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				final String sk = Convert.toString(key);
				return IoUtils.deserialize(jedis.hget(mkey.getBytes(), sk.getBytes()));
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
					final byte[] sbytes = mkey.getBytes();
					final boolean set_expire = expire > 0 && !jedis.exists(sbytes);
					final Long ret = jedis.hset(sbytes, key.getBytes(), IoUtils.serialize(value));
					if (set_expire) {
						jedis.expire(sbytes, expire);
					}
					return ret;
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
				final byte[] sbytes = mkey.getBytes();
				final String sk = Convert.toString(key);
				final Long ret = jedis.hdel(sbytes, sk.getBytes());
				if (jedis.hlen(sbytes) == 0) {
					jedis.del(sbytes);
				}
				return ret;
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
				return jedis.hexists(mkey.getBytes(), sk.getBytes());
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
				final Set<String> set = new HashSet<String>();
				for (final byte[] k : jedis.hkeys(mkey.getBytes())) {
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

	private static Log log = LogFactory.getLogger(JedisMap.class);

	private static final long serialVersionUID = 379414188959312886L;
}
