package net.simpleframework.ado.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.simpleframework.ado.bean.ITreeBeanAware;
import net.simpleframework.common.ID;
import net.simpleframework.common.coll.CollectionUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class DataQueryUtils {

	public static <T> IDataQuery<T> nullQuery() {
		return new ListDataQuery<T>();
	}

	public static <T> List<T> toList(final IDataQuery<T> dataQuery) {
		final List<T> al = new ArrayList<T>();
		T t;
		while (dataQuery != null && (t = dataQuery.next()) != null) {
			al.add(t);
		}
		return al;
	}

	public static <T> Set<T> toSet(final IDataQuery<T> dataQuery) {
		final Set<T> al = new LinkedHashSet<T>();
		T t;
		while (dataQuery != null && (t = dataQuery.next()) != null) {
			al.add(t);
		}
		return al;
	}

	public static <T> Iterator<T> toIterator(final IDataQuery<T> dataQuery) {
		if (dataQuery == null) {
			return CollectionUtils.EMPTY_ITERATOR();
		}
		return new DataQueryIterator<T>(dataQuery);
	}

	public static <T> Map<ID, Collection<T>> toTreeMap(final IDataQuery<T> dq) {
		dq.setFetchSize(0);
		final Map<ID, Collection<T>> _map = new HashMap<ID, Collection<T>>();
		T t;
		while ((t = dq.next()) != null) {
			final ID k = ((ITreeBeanAware) t).getParentId();
			Collection<T> coll = k != null ? _map.get(k) : _map.get(ID.NULL_ID);
			if (coll == null) {
				_map.put(k, coll = new ArrayList<T>());
			}
			coll.add(t);
		}
		return _map;
	}
}
