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

	public static String md(final InputStream inputStream, final String algorithm,
			final int bufferSize) throws IOException {
		if (inputStream == null) {
			return null;
		}
		try {
			final MessageDigest digest = MessageDigest.getInstance(algorithm);
			final byte[] buf = new byte[bufferSize];
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

	public static String md5Hex(final InputStream inputStream) throws IOException {
		return md(inputStream, "MD5", BUFFER);
	}

	public static String md(final byte[] bytes, final String algorithm) {
		try {
			final MessageDigest digest = MessageDigest.getInstance(algorithm);
			digest.update(bytes);
			return StringUtils.encodeHex(digest.digest());
		} catch (final NoSuchAlgorithmException e) {
			return null;
		}
	}

	public static String md5Hex(final byte[] bytes) {
		return md(bytes, "MD5");
	}

	public static String md5Hex(final String message) {
		return md5Hex(message.getBytes());
	}

	public static String sha1Hex(final byte[] bytes) {
		return md(bytes, "SHA1");
	}

	public static String sha1Hex(final String message) {
		return sha1Hex(message.getBytes());
	}

	public static String encryptPass(final String password) {
		return md5Hex(password == null ? "" : password.trim());
	}
}
