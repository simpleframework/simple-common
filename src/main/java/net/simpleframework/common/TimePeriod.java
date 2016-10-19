package net.simpleframework.common;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class TimePeriod implements Serializable {

	public static TimePeriod day = new TimePeriod(ETimePeriod.day);
	public static TimePeriod day2 = new TimePeriod(ETimePeriod.day2);
	public static TimePeriod week = new TimePeriod(ETimePeriod.week);
	public static TimePeriod month = new TimePeriod(ETimePeriod.month);
	public static TimePeriod year = new TimePeriod(ETimePeriod.year);

	public static TimePeriod yesterday() {
		final Calendar[] cal = DateUtils.getYesterdayInterval();
		return new TimePeriod(cal[0].getTime(), cal[1].getTime());
	}

	private ETimePeriod timePeriod;

	private String dateFormat = "yyyy-MM-dd";

	private Date from, to;

	public TimePeriod(final String timePeriod) {
		if (StringUtils.hasText(timePeriod)) {
			try {
				this.timePeriod = ETimePeriod.valueOf(timePeriod);
			} catch (final Exception e) {
			}
			if (this.timePeriod == null) {
				final String[] arr = StringUtils.split(timePeriod, ";");
				if (arr.length > 0) {
					this.timePeriod = ETimePeriod.custom;
					final String dateFormat = getDateFormat();
					from = Convert.toDate(arr[0], dateFormat);
					if (arr.length > 1) {
						to = Convert.toDate(arr[1], dateFormat);
						// 修正错误
						if (from != null && to != null && from.after(to)) {
							final Date t = from;
							from = to;
							to = t;
						}
					}

					if (from == null && to == null) {
						this.timePeriod = ETimePeriod.none;
					}
				}
			}
		}
	}

	public TimePeriod(final Date from, final Date to) {
		setTimePeriod(ETimePeriod.custom).setFrom(from).setTo(to);
	}

	public TimePeriod(final ETimePeriod timePeriod) {
		this.timePeriod = timePeriod;
	}

	public ETimePeriod getTimePeriod() {
		return timePeriod == null ? ETimePeriod.none : timePeriod;
	}

	public TimePeriod setTimePeriod(final ETimePeriod timePeriod) {
		this.timePeriod = timePeriod;
		return this;
	}

	public Date getFrom() {
		return from;
	}

	public TimePeriod setFrom(final Date from) {
		this.from = from;
		return this;
	}

	public Date getTo() {
		return to;
	}

	public TimePeriod setTo(final Date to) {
		this.to = to;
		return this;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public TimePeriod setDateFormat(final String dateFormat) {
		this.dateFormat = dateFormat;
		return this;
	}

	public boolean isAll() {
		final ETimePeriod tp = getTimePeriod();
		return tp == ETimePeriod.none
				|| (tp == ETimePeriod.custom && getFrom() == null && getTo() == null);
	}

	public Date getTime() {
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		final ETimePeriod td = getTimePeriod();
		if (td == ETimePeriod.day2) {
			cal.add(Calendar.DATE, -1);
		} else if (td == ETimePeriod.week) {
			cal.add(Calendar.DATE, -6);
		} else if (td == ETimePeriod.month) {
			cal.add(Calendar.MONTH, -1);
		} else if (td == ETimePeriod.month3) {
			cal.add(Calendar.MONTH, -4);
		} else if (td == ETimePeriod.year) {
			cal.add(Calendar.YEAR, -1);
		}
		return cal.getTime();
	}

	@Override
	public String toString() {
		final ETimePeriod tp = getTimePeriod();
		if (tp == ETimePeriod.custom) {
			final StringBuilder sb = new StringBuilder();
			final Date from = getFrom();
			final Date to = getTo();
			if (from != null || to != null) {
				final String dateFormat = getDateFormat();
				if (from != null) {
					if (to == null) {
						sb.append("&rsaquo; ");
					}
					sb.append("[").append(Convert.toDateString(from, dateFormat)).append("]");
				}
				if (to != null) {
					sb.append(from != null ? " - " : "&lsaquo; ");
					sb.append("[").append(Convert.toDateString(to, dateFormat)).append("]");
				}
			} else {
				sb.append(tp);
			}
			return sb.toString();
		} else {
			return tp.toString();
		}
	}

	private static final long serialVersionUID = -1099636030241525257L;
}
