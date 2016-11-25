package net.simpleframework.common;

import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

import net.simpleframework.common.logger.Log;
import net.simpleframework.common.logger.LogFactory;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class IoUtils_kryo {
	static Log log = LogFactory.getLogger(IoUtils_kryo.class);

	static Object kryo;
	static {
		try {
			kryo = Class.forName("com.esotericsoftware.kryo.Kryo").newInstance();
			BeanUtils.setProperty(kryo, "references", false);
			log.info("Kryo serialize enabled!");
		} catch (final Throwable ex) {
		}
	}

	public static byte[] serialize(final Object obj, final Class<?> typeClass) throws IOException {
		if (kryo != null) {
			if (obj == null) {
				return null;
			}

			final Kryo _kryo = (Kryo) kryo;
			// _kryo.setReferences(true);
			final ByteBufferOutput buffer = new ByteBufferOutput(1024, -1);
			if (typeClass != null) {
				_kryo.register(typeClass);
				_kryo.writeObject(buffer, obj);
			} else {
				_kryo.writeClassAndObject(buffer, obj);
			}
			return buffer.toBytes();
		} else {
			return IoUtils.serialize(obj);
		}
	}

	public static Object deserialize(final byte[] bytes, final Class<?> typeClass)
			throws IOException, ClassNotFoundException {
		if (kryo != null) {
			if (bytes == null || bytes.length == 0) {
				return null;
			}

			final Kryo _kryo = (Kryo) kryo;
			if (typeClass != null) {
				return _kryo.readObject(new ByteBufferInput(bytes), typeClass);
			} else {
				return _kryo.readClassAndObject(new ByteBufferInput(bytes));
			}
		} else {
			return IoUtils.deserialize(bytes);
		}
	}
}
