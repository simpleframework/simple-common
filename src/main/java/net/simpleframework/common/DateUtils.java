package net.simpleframework.common;

import static net.simpleframework.common.I18n.$m;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class DateUtils {

	public static final long HOUR_PERIOD = 60 * 60;

	public static final long DAY_PERIOD = HOUR_PERIOD * 24;

	public static Calendar getZeroPoint() {
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	public static Calendar[] getTodayInterval() {
		final Calendar cal1 = getZeroPoint();
		final Calendar cal2 = Calendar.getInstance();
		cal2.setTime(cal1.getTime());
		cal2.add(Calendar.DAY_OF_MONTH, 1);
		return new Calendar[] { cal1, cal2 };
	}

	public static Calendar[] getYesterdayInterval() {
		final Calendar[] cal = getTodayInterval();
		cal[0].add(Calendar.DAY_OF_MONTH, -1);
		cal[1].add(Calendar.DAY_OF_MONTH, -1);
		return cal;
	}

	public static long to24Hour() {
		final Calendar calendar = getZeroPoint();
		calendar.add(Calendar.DATE, 1);
		return (calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000;
	}

	public static String toDifferenceDate(final Date from) {
		if (from == null) {
			return "";
		}
		return toDifferenceDate(System.currentTimeMillis() - from.getTime());
	}

	/**
	 * 把dur转为具体化的时间,比如1小时23分
	 * 
	 * @param dur
	 * @return
	 */
	public static String toDifferenceDate(long dur) {
		dur = dur / 1000;
		final String s = $m("DateUtils.0");
		final String m = $m("DateUtils.1");
		final String h = $m("DateUtils.2");
		final String d = $m("DateUtils.3");
		final String p = "";
		final StringBuilder sb = new StringBuilder();
		if (dur < 60) {
			sb.append(dur).append(p).append(s);
		} else if (dur < HOUR_PERIOD) {
			final long ii = dur / 60;
			final long jj = dur % 60;
			sb.append(ii).append(p).append(m).append(p).append(jj).append(p).append(s);
		} else if (dur < DAY_PERIOD) {
			final long ii = dur / HOUR_PERIOD;
			final long jj = dur % HOUR_PERIOD / 60;
			sb.append(ii).append(p).append(h).append(p).append(jj).append(p).append(m);
		} else {
			final long ii = dur / DAY_PERIOD;
			final long jj = dur % DAY_PERIOD / HOUR_PERIOD;
			sb.append(ii).append(p).append(d).append(p).append(jj).append(p).append(h);
		}
		return sb.toString();
	}

	public static String getRelativeDate(final Date date) {
		return getRelativeDate(date, null);
	}

	/**
	 * 获取相对时间,比如1天前,刚刚
	 * 
	 * @param date
	 * @return
	 */
	public static String getRelativeDate(final Date date, final NumberConvert nc) {
		final long tstamp = date.getTime();
		final long t0 = System.currentTimeMillis();
		final long dt = t0 - tstamp;
		final long secs = dt / 1000L;
		long mins = secs / 60L;
		long hours = mins / 60L;
		final long days = hours / 24L;
		final StringBuilder sb = new StringBuilder();
		if (days != 0L) {
			sb.append(nc == null ? days : nc.convert(days)).append($m("DateUtils.3"));
		} else if ((hours -= days * 24L) != 0L) {
			sb.append(nc == null ? hours : nc.convert(hours)).append($m("DateUtils.2"));
		} else if ((mins -= (days * 24L + hours) * 60L) != 0L) {
			sb.append(nc == null ? mins : nc.convert(mins)).append($m("DateUtils.1"));
		}
		if (days != 0L || hours != 0L || mins != 0L) {
			sb.append($m("DateUtils.4"));
		} else {
			sb.append($m("DateUtils.5"));
		}
		return sb.toString();
	}

	public static interface NumberConvert {

		Object convert(Number n);
	}

	public static String getDateCategory(final Date date) {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		final Date nDate = cal.getTime();

		if (date.after(cal.getTime())) {
			return $m("DateUtils.6");
		}
		cal.add(Calendar.DATE, -1);
		if (date.after(cal.getTime())) {
			return $m("DateUtils.7");
		}
		cal.setTime(nDate);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		if (date.after(cal.getTime())) {
			return $m("DateUtils.8");
		}
		cal.setTime(nDate);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		if (date.after(cal.getTime())) {
			return $m("DateUtils.9");
		}
		cal.setTime(nDate);
		cal.set(Calendar.DAY_OF_YEAR, 1);
		if (date.after(cal.getTime())) {
			return $m("DateUtils.10");
		}
		return $m("DateUtils.11");
	}

	public static int dateToTimestamp(final Date time) {
		final Timestamp ts = new Timestamp(time.getTime());
		return (int) ((ts.getTime()) / 1000);
	}
}
