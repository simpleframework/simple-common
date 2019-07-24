package net.simpleframework.common;

import static net.simpleframework.common.I18n.$m;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public enum ETimePeriod {
	none {
		@Override
		public String toString() {
			return $m("ETimePeriod.none");
		}

		@Override
		public int intValue() {
			return 0;
		}
	},

	day {
		@Override
		public String toString() {
			return $m("ETimePeriod.day");
		}

		@Override
		public int intValue() {
			return 1;
		}
	},

	day2 {
		@Override
		public String toString() {
			return $m("ETimePeriod.day2");
		}

		@Override
		public int intValue() {
			return 2;
		}
	},

	week {
		@Override
		public String toString() {
			return $m("ETimePeriod.week");
		}

		@Override
		public int intValue() {
			return 3;
		}
	},

	month {
		@Override
		public String toString() {
			return $m("ETimePeriod.month");
		}

		@Override
		public int intValue() {
			return 4;
		}
	},

	month3 {
		@Override
		public String toString() {
			return $m("ETimePeriod.month3");
		}

		@Override
		public int intValue() {
			return 5;
		}
	},

	year {
		@Override
		public String toString() {
			return $m("ETimePeriod.year");
		}

		@Override
		public int intValue() {
			return 6;
		}
	},

	custom {
		@Override
		public String toString() {
			return $m("ETimePeriod.custom");
		}

		@Override
		public int intValue() {
			return 7;
		}
	};

	public abstract int intValue();
}
