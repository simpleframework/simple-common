package net.simpleframework.ado.query;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public interface IDataQueryListener<T> {

	/**
	 * 
	 * @param dataQuery
	 * @param bean
	 * @param pIndex
	 * @param pageEnd
	 */
	void next(IDataQuery<T> dataQuery, T bean, int pIndex, boolean pageEnd);
}
