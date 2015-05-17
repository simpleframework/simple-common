package net.simpleframework.common;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class NumberUtils {
	private static Map<String, DecimalFormat> decimalFormats = new ConcurrentHashMap<String, DecimalFormat>();

	public static String format(final Number number) {
		return format(number, ".##");
	}

	public static String format(final Number number, final String pattern) {
		if (number == null) {
			return "0";
		}
		if (!StringUtils.hasText(pattern)) {
			return number.toString();
		}
		DecimalFormat formatter = decimalFormats.get(pattern);
		if (formatter == null) {
			decimalFormats.put(pattern, formatter = new DecimalFormat(pattern));
		}
		return formatter.format(number);
	}

	public static String formatPercent(final double number, final int fraction) {
		final NumberFormat nt = NumberFormat.getPercentInstance();
		nt.setMinimumFractionDigits(fraction);
		return nt.format(number);
	}

	public static String formatPercent(final double number) {
		return formatPercent(number, 1);
	}

	public static long randomLong(final long min, final long max) {
		return min + (long) (Math.random() * (max - min));
	}

	public static int randomInt(final int min, final int max) {
		return min + (int) (Math.random() * (max - min));
	}
}
