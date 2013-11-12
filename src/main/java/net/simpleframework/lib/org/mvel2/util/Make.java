package net.simpleframework.lib.org.mvel2.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Make {
	public static class Map<K, V> {
		public static <K, V> Map<K, V> $() {
			return start();
		}

		public static <K, V> Map<K, V> start() {
			return new Map(new HashMap());
		}

		public static <K, V> Map<K, V> start(final Class<? extends java.util.Map> mapImpl) {
			try {
				return new Map(mapImpl.newInstance());
			} catch (final Throwable t) {
				throw new RuntimeException("error creating instance", t);
			}
		}

		private final java.util.Map<K, V> mapInstance;

		private Map(final java.util.Map<K, V> mapInstance) {
			this.mapInstance = mapInstance;
		}

		public Map<K, V> _(final K key, final V value) {
			mapInstance.put(key, value);
			return this;
		}

		public java.util.Map<K, V> _() {
			return finish();
		}

		public java.util.Map<K, V> finish() {
			return mapInstance;
		}
	}

	public static class String {
		public static String $() {
			return start();
		}

		public static String start() {
			return new String(new StringBuilder());
		}

		public java.lang.String _() {
			return finish();
		}

		public java.lang.String finish() {
			return stringAppender.toString();
		}

		private final StringBuilder stringAppender;

		String(final StringBuilder stringAppender) {
			this.stringAppender = stringAppender;
		}

		public String _(final char c) {
			stringAppender.append(c);
			return this;
		}

		public String _(final CharSequence cs) {
			stringAppender.append(cs);
			return this;
		}

		public String _(final String s) {
			stringAppender.append(s);
			return this;
		}
	}

	public static class List<V> {
		public static <V> List<V> $() {
			return start();
		}

		public static <V> List<V> start() {
			return new List(new ArrayList());
		}

		public static <V> List<V> start(final Class<? extends java.util.List> listImpl) {
			try {
				return new List(listImpl.newInstance());
			} catch (final Throwable t) {
				throw new RuntimeException("error creating instance", t);
			}
		}

		private final java.util.List<V> listInstance;

		List(final java.util.List<V> listInstance) {
			this.listInstance = listInstance;
		}

		public List<V> _(final V value) {
			listInstance.add(value);
			return this;
		}

		public java.util.List<V> _() {
			return finish();
		}

		public java.util.List<V> finish() {
			return listInstance;
		}
	}

	public static class Set<V> {
		public static <V> Set<V> $() {
			return start();
		}

		public static <V> Set<V> start() {
			return new Set(new HashSet());
		}

		public static <V> Set<V> start(final Class<? extends java.util.Set> listImpl) {
			try {
				return new Set(listImpl.newInstance());
			} catch (final Throwable t) {
				throw new RuntimeException("error creating instance", t);
			}
		}

		private final java.util.Set<V> listInstance;

		Set(final java.util.Set<V> listInstance) {
			this.listInstance = listInstance;
		}

		public Set<V> _(final V value) {
			listInstance.add(value);
			return this;
		}

		public java.util.Set<V> _() {
			return finish();
		}

		public java.util.Set<V> finish() {
			return listInstance;
		}
	}

}
