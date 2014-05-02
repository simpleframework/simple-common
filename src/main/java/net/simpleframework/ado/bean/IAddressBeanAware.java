package net.simpleframework.ado.bean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IAddressBeanAware {

	String getAddress();

	void setAddress(String address);

	double getLongitude();

	void setLongitude(double longitude);

	double getLatitude();

	void setLatitude(double latitude);
}
