package net.simpleframework.ado.bean;

import java.util.Date;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IDateAwareBean {

	Date getCreateDate();

	void setCreateDate(Date createDate);
}
