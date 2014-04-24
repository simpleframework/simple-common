package net.simpleframework.ado.bean;

import java.io.InputStream;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IAttachmentLobAware {

	String getMd();

	InputStream getAttachment();
}
