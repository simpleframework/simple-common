package net.simpleframework.ado.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class ListDataQuery<T> extends AbstractDataQuery<T> {

	private List<T> list;

	public ListDataQuery() {
		this(null);
	}

	public ListDataQuery(final Collection<T> list) {
		this.list = new ArrayList<T>();
		if (list != null) {
			this.list.addAll(list);
		}
	}

	public List<T> list() {
		return list;
	}

	@Override
	public T next() {
		return ++i < getCount() && list != null ? list.get(i) : null;
	}

	@Override
	public int getCount() {
		if (list != null && count < 0) {
			count = list.size();
		}
		return count;
	}

	@Override
	public int getFetchSize() {
		return 0;
	}

	@Override
	public ListDataQuery<T> setFetchSize(final int fetchSize) {
		return this;
	}
}
