package net.simpleframework.ado.query;

import java.util.Collection;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IDataQuery<T> {

	/**
	 * 获取下一个可用的对象，null表示已没有可用的数据
	 * 
	 * @return
	 */
	T next();

	/**
	 * 移动游标
	 * 
	 * @param pos
	 */
	void move(int pos);

	/**
	 * 当前游标的位置
	 * 
	 * @return
	 */
	int position();

	int getCount();

	void setCount(int count);

	int getFetchSize();

	/**
	 * 表示一次从数据源获取对象到DataQuery中的数量
	 * 
	 * @param fetchSize
	 */
	IDataQuery<T> setFetchSize(int fetchSize);

	void reset();

	void close();

	Collection<IDataQueryListener<T>> getListeners();

	void addListener(IDataQueryListener<T> listener);

	boolean removeListener(IDataQueryListener<T> listener);
}
