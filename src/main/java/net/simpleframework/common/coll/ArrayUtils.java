package net.simpleframework.common.coll;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class ArrayUtils {

	public static Object[] removeDuplicatesAndNulls(final Object[] array) {
		if (array == null) {
			return null;
		}
		final LinkedHashSet<Object> ht = new LinkedHashSet<Object>();
		for (final Object element : array) {
			if (element == null) {
				continue;
			}
			ht.add(element);
		}

		final Object[] ret = (Object[]) Array.newInstance(array.getClass().getComponentType(),
				ht.size());
		int j = 0;

		final Iterator<?> it = ht.iterator();
		while (it.hasNext()) {
			ret[j++] = it.next();
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] add(T[] arrays, final Class<T> objectClass, final T... objects) {
		if (objects != null && objects.length > 0) {
			if (arrays == null) {
				arrays = (T[]) Array.newInstance(objectClass, 0);
			}
			final T[] result = (T[]) Array.newInstance(objectClass, arrays.length + objects.length);
			int length = 0;
			System.arraycopy(arrays, 0, result, length, arrays.length);
			length += arrays.length;
			System.arraycopy(objects, 0, result, length, objects.length);
			return result;
		} else {
			return arrays;
		}
	}

	public static Object[] add(final Object[] arrays, final Object... objects) {
		return add(arrays, Object.class, objects);
	}

	public static String[] add(final String[] arrays, final String... strings) {
		return add(arrays, String.class, strings);
	}

	public static int indexOf(final Object[] array, final Object value) {
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				if (array[i].equals(value)) {
					return i;
				}
			}
		}
		return -1;
	}

	public static boolean contains(final Object[] array, final Object value) {
		return indexOf(array, value) != -1;
	}

	public static int indexOf(final int[] array, final int value) {
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				if (array[i] == value) {
					return i;
				}
			}
		}
		return -1;
	}

	public static boolean contains(final int[] array, final int value) {
		return indexOf(array, value) != -1;
	}

	public static boolean isEmpty(final Object[] arr) {
		int i;
		if (arr == null || (i = arr.length) == 0) {
			return true;
		}
		for (final Object o : arr) {
			if (o != null) {
				return false;
			}
			i--;
		}
		return i == 0;
	}

	public static <T> List<T> asList(final T... a) {
		return (List<T>) _setColl(new ArrayList<T>(), a);
	}

	public static <T> Vector<T> asVector(final T... a) {
		return (Vector<T>) _setColl(new Vector<T>(), a);
	}

	private static <T> Collection<T> _setColl(final Collection<T> coll, final T... a) {
		if (a != null) {
			for (final T t : a) {
				if (t != null) {
					coll.add(t);
				}
			}
		}
		return coll;
	}
}
