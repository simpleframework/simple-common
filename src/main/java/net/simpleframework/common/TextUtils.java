package net.simpleframework.common;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class TextUtils {

	public static boolean isAscii(final byte[] bytes) {
		for (final byte b : bytes) {
			if ((b & 0x80) != 0) {
				return false;
			}
		}
		return true;
	}

	public static boolean isUTF8(final byte[] rawtext) {
		int score = 0;
		int i, rawtextlen = 0;
		int goodbytes = 0, asciibytes = 0;
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen; i++) {
			if ((rawtext[i] & (byte) 0x7F) == rawtext[i]) {
				asciibytes++;
			} else if (-64 <= rawtext[i] && rawtext[i] <= -33 && // Two bytes
					i + 1 < rawtextlen && -128 <= rawtext[i + 1] && rawtext[i + 1] <= -65) {
				goodbytes += 2;
				i++;
			} else if (-32 <= rawtext[i] && rawtext[i] <= -17
					&& // Three bytes
					i + 2 < rawtextlen && -128 <= rawtext[i + 1] && rawtext[i + 1] <= -65
					&& -128 <= rawtext[i + 2] && rawtext[i + 2] <= -65) {
				goodbytes += 3;
				i += 2;
			}
		}
		if (asciibytes == rawtextlen) {
			return false;
		}
		score = 100 * goodbytes / (rawtextlen - asciibytes);
		if (score > 98) {
			return true;
		} else if (score > 95 && goodbytes > 30) {
			return true;
		} else {
			return false;
		}
	}
}
