package net.simpleframework.ado;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public enum EFilterRelation {
	equal {

		@Override
		public String toString() {
			return "=";
		}
	},

	not_equal {

		@Override
		public String toString() {
			return "<>";
		}
	},

	gt {

		@Override
		public String toString() {
			return ">";
		}
	},

	gt_equal {

		@Override
		public String toString() {
			return ">=";
		}
	},

	lt {

		@Override
		public String toString() {
			return "<";
		}
	},

	lt_equal {

		@Override
		public String toString() {
			return "<=";
		}
	},

	like,

	isNull {

		@Override
		public String toString() {
			return "is null";
		}
	},

	isNotNull {

		@Override
		public String toString() {
			return "is not null";
		}
	};

	public static EFilterRelation get(final String key) {
		for (final EFilterRelation relation : EFilterRelation.values()) {
			if (relation.toString().equals(key)) {
				return relation;
			}
		}
		return EFilterRelation.equal;
	}
}
