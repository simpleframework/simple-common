package net.simpleframework.common;

public abstract class LngLatUtils {

	private final static double DEF_PI = 3.14159265359; // PI
	private final static double DEF_PI180 = 0.01745329252; // PI/180.0
	private final static double DEF_R = 6370693.5; // radius of earth

	/**
	 * 根据圆心、半径算出经纬度范围
	 * 
	 * @param lat
	 *        圆心经度
	 * @param lng
	 *        圆心纬度
	 * @param r
	 *        半径（米）
	 * @return double[4] 南侧经度，北侧经度，西侧纬度，东侧纬度
	 */
	public static double[] getRange(final double lng, final double lat, final double r) {
		final double[] range = new double[4];
		// 角度转换为弧度
		final double ns = lat * DEF_PI180;
		final double sinNs = Math.sin(ns);
		final double cosNs = Math.cos(ns);
		final double cosTmp = Math.cos(r / DEF_R);

		// 经度的差值
		double a = (cosTmp - sinNs * sinNs) / (cosNs * cosNs);
		if (a > 1.0) {
			a = 1.0;
		} else if (a < -1.0) {
			a = -1.0;
		}
		final double lngDif = Math.acos(a) / DEF_PI180;
		// 保存经度
		range[0] = lng - lngDif;
		range[1] = lng + lngDif;

		final double m = 0 - 2 * cosTmp * sinNs;
		final double n = cosTmp * cosTmp - cosNs * cosNs;
		final double o1 = (0 - m - Math.sqrt(m * m - 4 * (n))) / 2;
		final double o2 = (0 - m + Math.sqrt(m * m - 4 * (n))) / 2;
		// 纬度
		final double lat1 = 180 / DEF_PI * Math.asin(o1);
		final double lat2 = 180 / DEF_PI * Math.asin(o2);
		// 保存
		range[2] = lat1;
		range[3] = lat2;

		return range;
	}

	/**
	 * 计算地球面上两上坐标点之间距离
	 * 
	 * @param lng1
	 *        位置1经度
	 * @param lat1
	 *        位置1纬度
	 * @param lng2
	 *        位置2经度
	 * @param lat2
	 *        位置2纬度
	 */
	public static double getLngLatDistance(final double lng1, final double lat1, final double lng2,
			final double lat2) {
		double ew1, ns1, ew2, ns2;
		double distance;
		// 角度转换为弧度
		ew1 = lng1 * DEF_PI180;
		ns1 = lat1 * DEF_PI180;
		ew2 = lng2 * DEF_PI180;
		ns2 = lat2 * DEF_PI180;
		// 求大圆劣弧与球心所夹的角(弧度)
		distance = Math.sin(ns1) * Math.sin(ns2)
				+ Math.cos(ns1) * Math.cos(ns2) * Math.cos(ew1 - ew2);

		// 调整到[-1..1]范围内，避免溢出
		if (distance > 1.0) {
			distance = 1.0;
		} else if (distance < -1.0) {
			distance = -1.0;
		}
		// 求大圆劣弧长度
		distance = DEF_R * Math.acos(distance);
		return distance;
	}
}
