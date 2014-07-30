package net.simpleframework.ado.bean;

import net.simpleframework.common.BeanUtils;
import net.simpleframework.common.ID;
import net.simpleframework.common.object.ObjectEx;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@SuppressWarnings("serial")
public abstract class AbstractIdBean extends ObjectEx implements IIdBeanAware {

	private ID id;

	@Override
	public ID getId() {
		return id;
	}

	@Override
	public void setId(final ID id) {
		this.id = id;
	}

	@Override
	public boolean equals(final Object obj) {
		final ID id = getId();
		if (id != null && obj instanceof AbstractIdBean) {
			return id.equals(((IIdBeanAware) obj).getId());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		final ID id = getId();
		return id != null ? id.hashCode() : super.hashCode();
	}

	public Object lock() {
		return getId().getValue();
	}

	@Override
	public AbstractIdBean clone() {
		return BeanUtils.clone(this);
	}
}
