package net.simpleframework.common;

import java.io.Serializable;
import java.util.UUID;

import net.simpleframework.common.object.ObjectUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class ID {
	private static Class<?> _type = String.class;

	public static void setType(final Class<?> type) {
		_type = type;
	}

	public static ID NULL_ID = ID.of(null);

	public static ID of(final Object id) {
		if (id instanceof ID) {
			return (ID) id;
		}
		if (id instanceof Long || Long.class.isAssignableFrom(_type)) {
			return new LongID(Convert.toLong(id));
		} else if (id instanceof Number || Number.class.isAssignableFrom(_type)) {
			return new IntegerID(Convert.toInt(id));
		} else {
			return new StringID(Convert.toString(id));
		}
	}

	public static StringID uuid() {
		final String s = UUID.randomUUID().toString();
		final StringBuilder sb = new StringBuilder(32);
		sb.append(s.substring(0, 8)).append(s.substring(9, 13)).append(s.substring(14, 18))
				.append(s.substring(19, 23)).append(s.substring(24));
		return new StringID(sb.toString());
	}

	static Object lock = new Object();

	static long COUNTER = 0;

	/**
	 * 在同一个虚拟机下产生一个唯一的ID，其格式为[time] - [counter]
	 */
	public static StringID uid() {
		final long time = System.currentTimeMillis();
		long id;
		synchronized (lock) {
			id = COUNTER++;
		}
		return new StringID(Long.toString(time, Character.MAX_RADIX)
				+ Long.toString(id, Character.MAX_RADIX));
	}

	/**
	 * 获取id的值
	 * 
	 * @return
	 */
	public abstract Object getValue();

	@SuppressWarnings("serial")
	public static abstract class AbstractID<T extends Comparable<T>> extends ID implements
			Comparable<AbstractID<T>>, Serializable {
		protected T id;

		@Override
		public T getValue() {
			return id;
		}

		@Override
		public int hashCode() {
			return id != null ? id.hashCode() : getClass().hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof ID)) {
				return false;
			}
			final Object id2 = ((ID) obj).getValue();
			if (id instanceof Number && id2 instanceof Number) {
				return ((Number) id).longValue() == ((Number) id2).longValue();
			} else {
				return ObjectUtils.objectEquals(id, id2);
			}
		}

		@Override
		public String toString() {
			return Convert.toString(getValue());
		}

		@Override
		public int compareTo(final AbstractID<T> o) {
			return id != null && o.id != null ? id.compareTo(o.id) : 0;
		}
	}

	public static class StringID extends AbstractID<String> {
		public StringID(final String id) {
			this.id = id != null ? id.trim() : null;
		}

		private static final long serialVersionUID = 8283766253505696610L;
	}

	public static class IntegerID extends AbstractID<Integer> {

		public IntegerID(final int id) {
			this.id = id;
		}

		private static final long serialVersionUID = 8864098349861539868L;
	}

	public static class LongID extends AbstractID<Long> {

		public LongID(final long id) {
			this.id = id;
		}

		private static final long serialVersionUID = 4193421687986152568L;
	}
}
