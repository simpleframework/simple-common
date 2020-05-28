package net.simpleframework.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class SerializeUtils {
	public static byte[] serialize(final Object obj) throws IOException {
		return hessian_serialize(obj);
	}

	public static Object deserialize(final byte[] bytes) throws IOException, ClassNotFoundException {
		return hessian_deserialize(bytes);
	}

	static byte[] jdk_serialize(final Object obj) throws IOException {
		if (obj == null) {
			return null;
		}

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		return bos.toByteArray();
	}

	static Object jdk_deserialize(final byte[] bytes) throws IOException, ClassNotFoundException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}

		final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		final ObjectInputStream ois = new ObjectInputStream(bis);
		return ois.readObject();
	}

	static byte[] hessian_serialize(final Object obj) throws IOException {
		if (obj == null) {
			return null;
		}

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final HessianOutput ho = new HessianOutput(bos);
		ho.writeObject(obj);
		return bos.toByteArray();
	}

	static Object hessian_deserialize(final byte[] bytes)
			throws IOException, ClassNotFoundException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}

		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		final HessianInput hi = new HessianInput(is);
		return hi.readObject();
	}
}
