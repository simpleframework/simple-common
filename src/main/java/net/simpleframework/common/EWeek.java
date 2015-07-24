package net.simpleframework.common;

public enum EWeek {
	Mon {
		@Override
		public String toString() {
			return "星期一";
		}
	},
	Tue {
		@Override
		public String toString() {
			return "星期二";
		}
	},
	Wed {
		@Override
		public String toString() {
			return "星期三";
		}
	},
	Thu {
		@Override
		public String toString() {
			return "星期四";
		}
	},
	Fri {
		@Override
		public String toString() {
			return "星期五";
		}
	},
	Sat {
		@Override
		public String toString() {
			return "星期六";
		}
	},
	Sun {
		@Override
		public String toString() {
			return "星期日";
		}
	};
}
