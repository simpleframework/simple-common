package net.simpleframework.common;

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

	public static void returnResource(final JedisPool pool, final Jedis jedis) {
		if (jedis == null) {
			return;
		}
		try {
			pool.returnResource(jedis);
		} catch (final Exception e) {
			doJedisException(pool, jedis, e);
		}
	}

	public static void doJedisException(final JedisPool pool, final Jedis jedis, final Exception e) {
		if (jedis != null) {
			log.warn(e);
			try {
				pool.returnBrokenResource(jedis);
			} catch (final Exception ex) {
			}
		}
	}

	public static Object getCache(final JedisPool pool, final String key) {
		final Jedis jedis = pool.getResource();
		try {
			return IoUtils.deserialize(jedis.get(key.getBytes()));
		} catch (final Exception e) {
			// 释放redis对象
			doJedisException(pool, jedis, e);
		} finally {
			// 返还到连接池
			returnResource(pool, jedis);
		}
		return null;
	}

	public static void putCache(final JedisPool pool, final String key, final Object val,
			final int expire) {
		final Jedis jedis = pool.getResource();
		try {
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
			doJedisException(pool, jedis, e);
		} finally {
			returnResource(pool, jedis);
		}
	}

	public static void removeCache(final JedisPool pool, final String key) {
		final Jedis jedis = pool.getResource();
		try {
			jedis.del(key.getBytes());
		} finally {
			returnResource(pool, jedis);
		}
	}

	static Log log = LogFactory.getLogger(JedisUtils.class);
}
