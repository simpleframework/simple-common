package net.simpleframework.ado.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.simpleframework.common.coll.CollectionUtils.AbstractIterator;

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
		return new DataQueryIterator<T>(dataQuery);
	}

	public static class DataQueryIterator<T> extends AbstractIterator<T> implements
			IDataQueryAware<T> {
		private T t;

		private final IDataQuery<T> dataQuery;

		public DataQueryIterator(final IDataQuery<T> dataQuery) {
			this.dataQuery = dataQuery;
		}

		@Override
		public boolean hasNext() {
			return dataQuery != null && (t = dataQuery.next()) != null;
		}

		@Override
		public T next() {
			return t;
		}

		@Override
		public IDataQuery<T> getDataQuery() {
			return dataQuery;
		}
	}
}
