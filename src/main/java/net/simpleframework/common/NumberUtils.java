package net.simpleframework.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class NumberUtils {
	private static Map<String, DecimalFormat> decimalFormats = new ConcurrentHashMap<>();

	public static double toMoney(final Number number) {
		return toDouble(number, 2);
	}

	public static double toDouble(final Number number) {
		return toDouble(number, 1);
	}

	public static double toDouble(final Number number, final int scale) {
		return new BigDecimal(number.doubleValue()).setScale(scale, RoundingMode.HALF_DOWN)
				.doubleValue();
	}

	public static float toFloat(final Number number) {
		return toFloat(number, 1);
	}

	public static float toFloat(final Number number, final int scale) {
		return new BigDecimal(number.doubleValue()).setScale(scale, RoundingMode.HALF_DOWN)
				.floatValue();
	}

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
		return min + (long) (Math.random() * (max - min + 1));
	}

	public static int randomInt(final int min, final int max) {
		return min + (int) (Math.random() * (max - min + 1));
	}
}
