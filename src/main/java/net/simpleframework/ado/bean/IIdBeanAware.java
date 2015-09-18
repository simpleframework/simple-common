package net.simpleframework.ado.bean;

import java.io.Serializable;

import net.simpleframework.common.ID;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IIdBeanAware extends Serializable {

	ID getId();

	/**
	 * 获取id
	 * 
	 * @param gen
	 *        为空时，是否自动生成
	 * @return
	 */
	ID getId(boolean gen);

	/**
	 * 设置id
	 * 
	 * @param id
	 */
	void setId(ID id);
}
