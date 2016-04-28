package net.simpleframework.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class IoUtils_hessian {

	static byte[] serialize(final Object obj) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final HessianOutput ho = new HessianOutput(bos);
		ho.writeObject(obj);
		return bos.toByteArray();
	}

	static Object deserialize(final byte[] bytes) throws IOException {
		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		final HessianInput hi = new HessianInput(is);
		return hi.readObject();
	}
}
