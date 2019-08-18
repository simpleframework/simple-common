package net.simpleframework.common.coll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class CollectionUtils {

	public static <T> List<T> EMPTY_LIST() {
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	public static <T> Enumeration<T> EMPTY_ENUMERATION() {
		return Collections.enumeration(Collections.EMPTY_LIST);
	}

	public static final <T> Set<T> EMPTY_SET() {
		return Collections.emptySet();
	}

	public static final <K, V> Map<K, V> EMPTY_MAP() {
		return Collections.emptyMap();
	}

	public static <T> List<T> toList(final Iterator<T> it) {
		if (it == null) {
			return null;
		}
		final List<T> l = new ArrayList<>();
		while (it.hasNext()) {
			l.add(it.next());
		}
		return l;
	}

	public static <T> Iterator<T> EMPTY_ITERATOR() {
		return new AbstractIterator<T>() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public T next() {
				return null;
			}
		};
	}

	public static abstract class AbstractIterator<E> implements Iterator<E> {
		protected int i = -1;

		@Override
		public void remove() {
		}
	}

	public static <T extends Enum<T>> void sortEnum(final List<T> l) {
		Collections.sort(l, new Comparator<T>() {
			@Override
			public int compare(final T o1, final T o2) {
				return o1.compareTo(o2);
			}
		});
	}
}
