package net.simpleframework.common;

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
			.compile("(?<!\\d)(?:(?:1[34578][0-9]{9})|(?:861[3578][0-9]{9}))(?!\\d)");

	public static String replaceAllSMobile(final String cc) {
		final Matcher matcher = MOBILE_PATTERN.matcher(cc);
		final StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, toSMobile(matcher.group()));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}
