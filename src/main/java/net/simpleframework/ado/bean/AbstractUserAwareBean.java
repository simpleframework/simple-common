package net.simpleframework.ado.bean;

import net.simpleframework.common.ID;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@SuppressWarnings("serial")
public abstract class AbstractUserAwareBean extends AbstractDateAwareBean implements IUserAwareBean {

	/* 创建人 */
	private ID userId;

	@Override
	public ID getUserId() {
		return userId;
	}

	@Override
	public void setUserId(final ID userId) {
		this.userId = userId;
	}
}
