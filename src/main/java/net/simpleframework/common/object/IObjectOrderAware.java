package net.simpleframework.common.object;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IObjectOrderAware {

	/**
	 * 获取对象的顺序
	 * 
	 * @return
	 */
	int getOrder();
}
