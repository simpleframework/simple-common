package net.simpleframework.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import net.simpleframework.common.logger.Log;
import net.simpleframework.common.logger.LogFactory;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class IoUtils_hessian {
	static Log log = LogFactory.getLogger(IoUtils_hessian.class);

	static boolean hessianEnabled = false;
	static {
		try {
			Class.forName("com.caucho.hessian.io.HessianInput");
			hessianEnabled = true;
			log.info("Hessian serialize enabled!");
		} catch (final Throwable ex) {
		}
	}

	public static byte[] serialize(final Object obj) throws IOException {
		if (hessianEnabled) {
			if (obj == null) {
				return null;
			}
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final HessianOutput ho = new HessianOutput(bos);
			ho.writeObject(obj);
			return bos.toByteArray();
		} else {
			return IoUtils.serialize(obj);
		}
	}

	public static Object deserialize(final byte[] bytes) throws IOException, ClassNotFoundException {
		if (hessianEnabled) {
			if (bytes == null || bytes.length == 0) {
				return null;
			}

			final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			final HessianInput hi = new HessianInput(is);
			return hi.readObject();
		} else {
			return IoUtils.deserialize(bytes);
		}
	}
}
