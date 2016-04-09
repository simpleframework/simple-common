package net.simpleframework.common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class IoUtils2 {

	static byte[] kryo_serialize(final Object kryo, final Object obj) {
		final ByteBufferOutput buffer = new ByteBufferOutput(1024, -1);
		((Kryo) kryo).writeClassAndObject(buffer, obj);
		return buffer.toBytes();
	}

	static Object kryo_deserialize(final Object kryo, final byte[] bytes, final Class<?> typeClass) {
		if (typeClass != null) {
			((Kryo) kryo).register(typeClass);
			return ((Kryo) kryo).readObject(new ByteBufferInput(bytes), typeClass);
		} else {
			return ((Kryo) kryo).readClassAndObject(new ByteBufferInput(bytes));
		}
	}
}
