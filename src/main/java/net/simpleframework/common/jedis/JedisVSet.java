package net.simpleframework.common.jedis;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class JedisVSet extends HashSet<String> {
	private JedisPool pool;

	private final String key;

	public JedisVSet(final JedisPool _pool, final String _key) {
		key = getClass().getSimpleName() + ":" + _key;
		pool = _pool;
		try {
			if (pool != null) {
				pool.getResource().close();
			}
		} catch (final Throwable e) {
			pool = null;
		}
	}

	public Set<String> nSet() {
		if (pool != null) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				return new HashSet<String>(jedis.smembers(key));
			} finally {
				if (jedis != null) {
					jedis.close();
				}
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
	public int size() {
		if (pool != null) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				final Set<String> set = jedis.smembers(key);
				return set != null ? set.size() : 0;
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		} else {
			return super.size();
		}
	}

	@Override
	public boolean add(final String e) {
		if (pool != null) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				jedis.sadd(key, e);
				return true;
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		} else {
			return super.add(e);
		}
	}

	@Override
	public boolean contains(final Object o) {
		final String e = o.toString();
		if (pool != null) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				return jedis.sismember(key, e);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		} else {
			return super.contains(e);
		}
	}

	@Override
	public boolean remove(final Object o) {
		final String e = o.toString();
		if (pool != null) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				jedis.srem(key, e);
				return true;
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		} else {
			return super.remove(e);
		}
	}

	private static final long serialVersionUID = -8332247234732521904L;
}
