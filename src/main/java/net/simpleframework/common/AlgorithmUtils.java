package net.simpleframework.common;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AlgorithmUtils {
	public static byte[] base64Decode(final String encoded) {
		return encoded == null ? null : Base64.decode(encoded);
	}

	public static String base64Encode(final byte[] binaryData) {
		return binaryData == null ? null : Base64.encodeToString(binaryData);
	}

	static final int BUFFER = 8 * 1024;

	public static String md5Hex(final InputStream inputStream) throws IOException {
		if (inputStream == null) {
			return null;
		}
		try {
			final MessageDigest digest = MessageDigest.getInstance("MD5");
			final byte[] buf = new byte[BUFFER];
			for (;;) {
				final int numRead = inputStream.read(buf);
				if (numRead == -1) {
					break;
				}
				digest.update(buf);
			}
			return StringUtils.encodeHex(digest.digest());
		} catch (final NoSuchAlgorithmException e) {
			return null;
		}
	}

	public static String md5Hex(final byte[] bytes) {
		try {
			final MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(bytes);
			return StringUtils.encodeHex(digest.digest());
		} catch (final NoSuchAlgorithmException e) {
			return null;
		}
	}

	public static String md5Hex(final String message) {
		return md5Hex(message.getBytes());
	}
}
