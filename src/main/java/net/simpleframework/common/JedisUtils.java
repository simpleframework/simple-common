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

	static Log log = LogFactory.getLogger(JedisUtils.class);
}
