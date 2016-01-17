package net.simpleframework.ado.query;

import net.simpleframework.common.coll.CollectionUtils.AbstractIterator;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DataQueryIterator<T> extends AbstractIterator<T> {
	private T t;

	private final IDataQuery<T> dataQuery;

	public DataQueryIterator(final IDataQuery<T> dataQuery) {
		this.dataQuery = dataQuery.setFetchSize(0);
	}

	@Override
	public boolean hasNext() {
		return dataQuery != null && (t = dataQuery.next()) != null;
	}

	@Override
	public T next() {
		return t;
	}

	public IDataQuery<T> getDataQuery() {
		return dataQuery;
	}
}
