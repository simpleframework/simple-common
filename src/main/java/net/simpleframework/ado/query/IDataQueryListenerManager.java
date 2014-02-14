package net.simpleframework.ado.query;

import java.util.Collection;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IDataQueryListenerManager<T> {

	Collection<IDataQueryListener<T>> getListeners();

	void addListener(IDataQueryListener<T> listener);

	boolean removeListener(IDataQueryListener<T> listener);
}
