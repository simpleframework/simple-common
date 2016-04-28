package net.simpleframework.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class IoUtils2 {

	static byte[] kryo_serialize(final Object kryo, final Object obj) {
		final com.esotericsoftware.kryo.io.ByteBufferOutput buffer = new com.esotericsoftware.kryo.io.ByteBufferOutput(
				1024, -1);
		((com.esotericsoftware.kryo.Kryo) kryo).writeClassAndObject(buffer, obj);
		return buffer.toBytes();
	}

	static byte[] hessian_serialize(final Object obj) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final com.caucho.hessian.io.HessianOutput ho = new com.caucho.hessian.io.HessianOutput(bos);
		ho.writeObject(obj);
		return bos.toByteArray();
	}

	static Object kryo_deserialize(final Object kryo, final byte[] bytes, final Class<?> typeClass) {
		final com.esotericsoftware.kryo.Kryo _kryo = (com.esotericsoftware.kryo.Kryo) kryo;
		if (typeClass != null) {
			_kryo.register(typeClass);
			return _kryo
					.readObject(new com.esotericsoftware.kryo.io.ByteBufferInput(bytes), typeClass);
		} else {
			return _kryo.readClassAndObject(new com.esotericsoftware.kryo.io.ByteBufferInput(bytes));
		}
	}

	static Object hessian_deserialize(final byte[] bytes) throws IOException {
		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		final com.caucho.hessian.io.HessianInput hi = new com.caucho.hessian.io.HessianInput(is);
		return hi.readObject();
	}
}
