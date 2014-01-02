package net.simpleframework.ado.bean;

import net.simpleframework.common.ID;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface ITreeBeanAware {

	/**
	 * 获取父id
	 * 
	 * @return
	 */
	ID getParentId();

	/**
	 * 设置父id
	 * 
	 * @param parentId
	 */
	void setParentId(final ID parentId);
}
