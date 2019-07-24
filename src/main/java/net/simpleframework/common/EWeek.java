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

		@Override
		public int intValue() {
			return 0;
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

		@Override
		public int intValue() {
			return 1;
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

		@Override
		public int intValue() {
			return 2;
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

		@Override
		public int intValue() {
			return 3;
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

		@Override
		public int intValue() {
			return 4;
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

		@Override
		public int intValue() {
			return 5;
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

		@Override
		public int intValue() {
			return 6;
		}
	};

	public abstract int week();

	public abstract int intValue();

	public static EWeek getEWeek(final int week) {
		for (final EWeek oWeek : values()) {
			if (oWeek.week() == week) {
				return oWeek;
			}
		}
		return null;
	}
}
