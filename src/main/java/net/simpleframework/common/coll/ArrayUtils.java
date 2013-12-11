package net.simpleframework.common.coll;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedHashSet;

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
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(value)) {
				return i;
			}
		}
		return -1;
	}

	public static boolean contains(final Object[] array, final Object value) {
		return indexOf(array, value) != -1;
	}

	public static int indexOf(final int[] array, final int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) {
				return i;
			}
		}
		return -1;
	}

	public static boolean contains(final int[] array, final int value) {
		return indexOf(array, value) != -1;
	}

	public static boolean isEmpty(final Object[] arr) {
		int i = arr.length;
		if (arr == null || i == 0) {
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
}
