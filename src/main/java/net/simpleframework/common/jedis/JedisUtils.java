package net.simpleframework.common.jedis;

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
public abstract class JedisUtils {

	public static Object getCache(final JedisPool pool, final String key) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			return IoUtils.deserialize(jedis.get(key.getBytes()));
		} catch (final Exception e) {
			// 释放redis对象
			log.warn(e);
		} finally {
			// 返还到连接池
			if (jedis != null) {
				jedis.close();
			}
		}
		return null;
	}

	public static void putCache(final JedisPool pool, final String key, final Object val) {
		putCache(pool, key, val, 0);
	}

	public static void putCache(final JedisPool pool, final String key, final Object val,
			final int expire) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			if (val instanceof String) {
				if (expire > 0) {
					jedis.setex(key, expire, (String) val);
				} else {
					jedis.set(key, (String) val);
				}
			} else {
				if (expire > 0) {
					jedis.setex(key.getBytes(), expire, IoUtils.serialize(val));
				} else {
					jedis.set(key.getBytes(), IoUtils.serialize(val));
				}
			}
		} catch (final Exception e) {
			log.warn(e);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	public static void removeCache(final JedisPool pool, final String key) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			jedis.del(key.getBytes());
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	static Log log = LogFactory.getLogger(JedisUtils.class);
}
