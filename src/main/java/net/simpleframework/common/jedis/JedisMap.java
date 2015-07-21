package net.simpleframework.common.jedis;

import java.io.IOException;
import java.util.HashMap;

import net.simpleframework.common.Convert;
import net.simpleframework.common.IoUtils;
import net.simpleframework.common.logger.Log;
import net.simpleframework.common.logger.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class JedisMap extends HashMap<String, Object> {
	private final int expire;

	private JedisPool pool;

	public JedisMap(final JedisPool _pool, final int _expire) {
		expire = _expire;
		pool = _pool;
		try {
			if (pool != null) {
				pool.getResource().close();
			}
		} catch (final Throwable e) {
			pool = null;
		}
	}

	public JedisMap(final JedisPool _pool) {
		this(_pool, 0);
	}

	@Override
	public Object get(final Object key) {
		if (pool != null) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				final String sk = Convert.toString(key);
				return IoUtils.deserialize(jedis.get(sk.getBytes()));
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
				if (expire > 0) {
					return jedis.setex(key.getBytes(), expire, IoUtils.serialize(value));
				} else {
					return jedis.set(key.getBytes(), IoUtils.serialize(value));
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
	public boolean containsKey(final Object key) {
		if (pool != null) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				final String sk = Convert.toString(key);
				return jedis.exists(sk.getBytes());
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		} else {
			return super.containsKey(key);
		}
	}

	private static Log log = LogFactory.getLogger(JedisMap.class);

	private static final long serialVersionUID = 379414188959312886L;
}
