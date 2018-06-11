package net.simpleframework.common;

import java.util.Calendar;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public enum EWeek {
	Mon {
		@Override
		public String toString() {
			return "星期一";
		}

		@Override
		public int week() {
			return Calendar.MONDAY;
		}
	},
	Tue {
		@Override
		public String toString() {
			return "星期二";
		}

		@Override
		public int week() {
			return Calendar.TUESDAY;
		}
	},
	Wed {
		@Override
		public String toString() {
			return "星期三";
		}

		@Override
		public int week() {
			return Calendar.WEDNESDAY;
		}
	},
	Thu {
		@Override
		public String toString() {
			return "星期四";
		}

		@Override
		public int week() {
			return Calendar.THURSDAY;
		}
	},
	Fri {
		@Override
		public String toString() {
			return "星期五";
		}

		@Override
		public int week() {
			return Calendar.FRIDAY;
		}
	},
	Sat {
		@Override
		public String toString() {
			return "星期六";
		}

		@Override
		public int week() {
			return Calendar.SATURDAY;
		}
	},
	Sun {
		@Override
		public String toString() {
			return "星期日";
		}

		@Override
		public int week() {
			return Calendar.SUNDAY;
		}
	};

	public abstract int week();

	public static EWeek getEWeek(final int week) {
		for (final EWeek oWeek : values()) {
			if (oWeek.week() == week) {
				return oWeek;
			}
		}
		return null;
	}
}
