package net.simpleframework.common.coll;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class CollectionUtils {

	public static <T> List<T> toList(final Enumeration<T> enumeration) {
		if (enumeration == null) {
			return null;
		}
		final List<T> l = new ArrayList<T>();
		while (enumeration.hasMoreElements()) {
			l.add(enumeration.nextElement());
		}
		return l;
	}

	@SuppressWarnings("rawtypes")
	public static Enumeration EMPTY_ENUMERATION = new Enumeration() {

		@Override
		public boolean hasMoreElements() {
			return false;
		}

		@Override
		public Object nextElement() {
			return null;
		}
	};

	public static abstract class AbstractIterator<E> implements Iterator<E> {
		protected int i = -1;

		@Override
		public void remove() {
		}
	}
}
