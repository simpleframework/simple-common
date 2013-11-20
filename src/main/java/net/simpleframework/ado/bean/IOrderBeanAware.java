package net.simpleframework.ado.bean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IOrderBeanAware {

	/**
	 * 获取bean的排序
	 * 
	 * @return
	 */
	int getOorder();

	void setOorder(final int oorder);
}
