package net.simpleframework.common;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class MobileUtils {
	public static String toSMobile(final String mobile) {
		if (RegexUtils.isMobile(mobile)) {
			final int l = mobile.length();
			return StringUtils.replace(mobile, mobile.substring(l - 8, l - 4), "****");
		} else {
			return mobile;
		}
	}

	static final Pattern MOBILE_PATTERN = Pattern
			.compile("(?:(\\(\\+?86\\))(1[3456789][0-9]{1})+\\d{8})|"
					+ "(?:86-?(1[3456789][0-9]{1})+\\d{8})|(?:(1[3456789][0-9]{1})+\\d{8})");

	static final Pattern PHONE_PATTERN = Pattern
			.compile("(?:(\\(\\+?86\\))(0[0-9]{2,3}\\-?)?([2-9][0-9]{6,7})+(\\-[0-9]{1,4})?)|"
					+ "(?:(86-?)?(0[0-9]{2,3}\\-?)?([2-9][0-9]{6,7})+(\\-[0-9]{1,4})?)");

	// static final Pattern MOBILE_PATTERN = Pattern
	// .compile("(?<!\\d)(?:(?:1[34578][0-9]{9})|(?:861[3578][0-9]{9}))(?!\\d)");

	public static String replaceAllSMobile(final String cc) {
		final Matcher matcher = MOBILE_PATTERN.matcher(cc);
		final StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, toSMobile(matcher.group()));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public static void getPhoneNumIntoSet(final String cc, final Set<String> set) {
		final Matcher matcher = Pattern
				.compile("(?:" + MOBILE_PATTERN.pattern() + "|" + PHONE_PATTERN.pattern() + ")")
				.matcher(cc);
		while (matcher.find()) {
			set.add(matcher.group());
		}
	}
}
