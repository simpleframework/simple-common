package net.simpleframework.common;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class JedisHashSet extends HashSet<String> {
	private JedisPool pool;

	private final String key;

	public JedisHashSet(final JedisPool _pool, final String _key) {
		key = getClass().getSimpleName() + ":" + _key;
		pool = _pool;
		try {
			pool.getResource().close();
		} catch (final Throwable e) {
			pool = null;
		}
	}

	public Set<String> toSet() {
		if (pool != null) {
			final Jedis jedis = pool.getResource();
			try {
				return jedis.smembers(key);
			} finally {
				jedis.close();
			}
		} else {
			final Set<String> _set = new HashSet<String>();
			for (final Object o : this) {
				_set.add(o.toString());
			}
			return _set;
		}
	}

	@Override
	public boolean add(final String e) {
		if (pool != null) {
			final Jedis jedis = pool.getResource();
			try {
				jedis.sadd(key, e);
				return true;
			} finally {
				jedis.close();
			}
		} else {
			return super.add(e);
		}
	}

	@Override
	public boolean remove(final Object o) {
		final String e = o.toString();
		if (pool != null) {
			final Jedis jedis = pool.getResource();
			try {
				jedis.srem(key, e);
				return true;
			} finally {
				jedis.close();
			}
		} else {
			return super.remove(e);
		}
	}

	private static final long serialVersionUID = -8332247234732521904L;
}
