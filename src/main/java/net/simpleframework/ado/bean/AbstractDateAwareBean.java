package net.simpleframework.ado.bean;

import java.util.Date;

import net.simpleframework.ado.ColumnMeta;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@SuppressWarnings("serial")
public abstract class AbstractDateAwareBean extends AbstractIdBean implements IDateAwareBean {

	@ColumnMeta(columnText = "#(AbstractDateAwareBean.0)")
	private Date createDate;

	@Override
	public Date getCreateDate() {
		if (createDate == null) {
			createDate = new Date();
		}
		return createDate;
	}

	@Override
	public void setCreateDate(final Date createDate) {
		this.createDate = createDate;
	}
}
