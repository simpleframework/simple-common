package net.simpleframework.ado.bean;

import net.simpleframework.common.ID;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IDomainBeanAware {

	/**
	 * 获取域id
	 * 
	 * @return
	 */
	ID getDomainId();

	void setDomainId(ID domainId);
}
