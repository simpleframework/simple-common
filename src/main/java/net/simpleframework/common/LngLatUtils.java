package net.simpleframework.common;

public abstract class LngLatUtils {

	static double PI = Math.PI;// 3.14159265
	static double RC = 6378137; // 赤道半径
	static double RJ = 6356725; // 极半径

	public static class LngLat {

		public double lng_deg, lng_min, lng_sec;
		public double lat_deg, lat_min, lat_sec;

		public double lng, lat;

		public double lng_rad, lat_rad;

		public double ec;
		public double ed;

		public LngLat(final double lng_deg, final double lng_min, final double lng_sec,
				final double lat_deg, final double lat_min, final double lat_sec) {
			this.lng_deg = lng_deg;
			this.lng_min = lng_min;
			this.lng_sec = lng_sec;
			this.lat_deg = lat_deg;
			this.lat_min = lat_min;
			this.lat_sec = lat_sec;

			lng = lng_deg + lng_min / 60 + lng_sec / 3600;
			lat = lat_deg + lat_min / 60 + lat_sec / 3600;

			lng_rad = lng * PI / 180.;
			lat_rad = lat * PI / 180.;

			ec = RJ + (RC - RJ) * (90. - lat) / 90.;
			ed = ec * Math.cos(lat_rad);
		}

		public LngLat(final double lng, final double lat) {
			lng_deg = (int) (lng);
			lng_min = (int) ((lng - lng_deg) * 60);
			lng_sec = (int) (lng - lng_deg - lng_min / 60.) * 3600;

			lat_deg = (int) lat;
			lat_min = (int) ((lat - lat_deg) * 60);
			lat_sec = (lat - lat_deg - lat_min / 60.) * 3600;

			this.lng = lng;
			this.lat = lat;

			lng_rad = lng * PI / 180.;
			lat_rad = lat * PI / 180.;

			ec = RJ + (RC - RJ) * (90. - lat) / 90.;
			ed = ec * Math.cos(lat_rad);
		}
	}

	public static class Distance {
		public double distance;

		public double angle;

		Distance(final double distance, final double angle) {
			this.distance = distance;
			this.angle = angle;
		}
	}

	public static class Around {
		public double lng_max, lng_min;

		public double lat_max, lat_min;

		Around(final double lng_max, final double lng_min, final double lat_max, final double lat_min) {
			this.lng_max = lng_max;
			this.lng_min = lng_min;
			this.lat_max = lat_max;
			this.lat_min = lat_min;
		}
	}

	/**
	 * 计算点A 和 点B的经纬度，求他们的距离和点B相对于点A的方位
	 * 
	 * @param A
	 * @param B
	 * @return
	 */
	public static Distance getDistance(final LngLat A, final LngLat B) {
		final double dx = (B.lng_rad - A.lng_rad) * A.ed;
		final double dy = (B.lat_rad - A.lat_rad) * A.ec;
		final double out = Math.sqrt(dx * dx + dy * dy);

		double angle = Math.atan(Math.abs(dx / dy)) * 180. / PI;
		// 判断象限
		final double dLo = B.lng - A.lng;
		final double dLa = B.lat - A.lat;
		if (dLo > 0 && dLa <= 0) {
			angle = (90. - angle) + 90.;
		} else if (dLo <= 0 && dLa < 0) {
			angle = angle + 180.;
		} else if (dLo < 0 && dLa >= 0) {
			angle = (90. - angle) + 270;
		}

		return new Distance(out, angle);
	}

	public static Distance getDistance(final double lng1, final double lat1, final double lng2,
			final double lat2) {
		return getDistance(new LngLat(lng1, lat1), new LngLat(lng2, lat2));
	}

	/**
	 * 已知点A经纬度，根据B点据A点的距离，和方位，求B点的经纬度
	 * 
	 * @param A
	 * @param distance
	 *        米
	 * @param angle
	 * @return
	 */
	public static LngLat getLnglatByDis(final LngLat A, final double distance, final double angle) {
		final double dx = distance * Math.sin(angle * PI / 180.);
		final double dy = distance * Math.cos(angle * PI / 180.);

		final double _lng = (dx / A.ed + A.lng_rad) * 180. / PI;
		final double _lat = (dy / A.ec + A.lat_rad) * 180. / PI;
		return new LngLat(_lng, _lat);
	}

	public static LngLat getLnglatByDis(final double lng, final double lat, final double distance,
			final double angle) {
		return getLnglatByDis(new LngLat(lng, lat), distance, angle);
	}

	public static Around getAround(final double lng, final double lat, final double distance) {
		return getAround(new LngLat(lng, lat), distance);
	}

	public static Around getAround(final LngLat A, final double distance) {
		final double lng_max = (A.lng_rad + distance / A.ed) * 180. / PI;
		final double lng_min = (A.lng_rad - distance / A.ed) * 180. / PI;
		final double lat_max = (A.lat_rad + distance / A.ec) * 180. / PI;
		final double lat_min = (A.lat_rad - distance / A.ec) * 180. / PI;
		return new Around(lng_max, lng_min, lat_max, lat_min);
	}
}
