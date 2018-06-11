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
	},

	day {
		@Override
		public String toString() {
			return $m("ETimePeriod.day");
		}
	},

	day2 {
		@Override
		public String toString() {
			return $m("ETimePeriod.day2");
		}
	},

	week {
		@Override
		public String toString() {
			return $m("ETimePeriod.week");
		}
	},

	month {
		@Override
		public String toString() {
			return $m("ETimePeriod.month");
		}
	},

	month3 {
		@Override
		public String toString() {
			return $m("ETimePeriod.month3");
		}
	},

	year {
		@Override
		public String toString() {
			return $m("ETimePeriod.year");
		}
	},

	custom {
		@Override
		public String toString() {
			return $m("ETimePeriod.custom");
		}
	}
}
