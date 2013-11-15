package net.simpleframework.common.object;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.simpleframework.common.Convert;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class ObjectUtils {

	public static boolean objectEquals(final Object newVal, final Object oldVal) {
		if (newVal == oldVal) {
			return true;
		} else if ((newVal == null) || (oldVal == null)) {
			return false;
		} else if (newVal.getClass().isArray() && oldVal.getClass().isArray()) {
			final int nLength = Array.getLength(newVal);
			final int oLength = Array.getLength(oldVal);
			if (nLength != oLength) {
				return false;
			}
			for (int i = 0; i < nLength; i++) {
				if (!objectEquals(Array.get(newVal, i), Array.get(oldVal, i))) {
					return false;
				}
			}
			return true;
		} else {
			return newVal.equals(oldVal);
		}
	}

	public static int length(final Object obj) {
		if (obj == null) {
			return 0;
		}
		if (obj instanceof String) {
			return ((String) obj).length();
		}
		if (obj instanceof Collection) {
			return ((Collection<?>) obj).size();
		}
		if (obj instanceof Map) {
			return ((Map<?, ?>) obj).size();
		}
		int count;
		if (obj instanceof Iterator) {
			final Iterator<?> iter = (Iterator<?>) obj;
			count = 0;
			while (iter.hasNext()) {
				count++;
				iter.next();
			}
			return count;
		}
		if (obj instanceof Enumeration) {
			final Enumeration<?> enumeration = (Enumeration<?>) obj;
			count = 0;
			while (enumeration.hasMoreElements()) {
				count++;
				enumeration.nextElement();
			}
			return count;
		}
		if (obj.getClass().isArray() == true) {
			return Array.getLength(obj);
		}
		return -1;
	}

	public static String hashStr(final Object object) {
		if (object == null) {
			return null;
		}
		final int hash = object.hashCode();
		return hash > 0 ? String.valueOf(hash) : "0" + Math.abs(hash);
	}

	public static int hashInt(final Object object) {
		return Convert.toInt(hashStr(object));
	}

	public static <T extends IObjectOrderAware> void sort(final List<T> coll) {
		Collections.sort(coll, new Comparator<IObjectOrderAware>() {
			@Override
			public int compare(final IObjectOrderAware o1, final IObjectOrderAware o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});
	}
}
