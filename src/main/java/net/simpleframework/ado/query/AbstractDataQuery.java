package net.simpleframework.ado.query;

import java.util.Collection;
import java.util.LinkedHashSet;

import net.simpleframework.common.object.ObjectEx;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractDataQuery<T> extends ObjectEx implements IDataQuery<T> {
	protected int i;

	protected int count;

	public AbstractDataQuery() {
		reset();
		enableAttributes();
	}

	@Override
	public int position() {
		return i;
	}

	@Override
	public void move(final int toIndex) {
		i = Math.max(toIndex, -1);
	}

	@Override
	public void reset() {
		move(-1);
		count = -1;
	}

	@Override
	public void setCount(final int count) {
		this.count = count;
	}

	@Override
	public void close() {
	}

	/**
	 * 监听器
	 */
	private Collection<IDataQueryListener<T>> listeners;

	@Override
	public Collection<IDataQueryListener<T>> getListeners() {
		if (listeners == null) {
			listeners = new LinkedHashSet<IDataQueryListener<T>>();
		}
		return listeners;
	}

	@Override
	public void addListener(final IDataQueryListener<T> listener) {
		getListeners().add(listener);
	}

	@Override
	public boolean removeListener(final IDataQueryListener<T> listener) {
		return getListeners().remove(listener);
	}
}
