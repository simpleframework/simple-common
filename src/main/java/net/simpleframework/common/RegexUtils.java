package net.simpleframework.common;

import java.util.regex.Pattern;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class RegexUtils {

	private final static boolean match(final String text, final String reg) {
		if (StringUtils.isBlank(text) || StringUtils.isBlank(reg)) {
			return false;
		}
		return Pattern.compile(reg).matcher(text).matches();
	}

	public final static boolean isEmail(final String str) {
		return match(str, "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
	}

	/**
	 * 电话号码验证
	 * 
	 * @param text
	 * @return
	 */
	public final static boolean isPhone(final String text) {
		return match(text, "^(\\d{3,4}-?)?\\d{7,9}$");
	}

	/**
	 * 手机号码验证
	 * 
	 * @param text
	 * @return
	 */
	public final static boolean isMobile(final String text) {
		if (text.length() != 11) {
			return false;
		}
		return match(text, "^((1[3456789][0-9]{1})+\\d{8})$");
	}

	/**
	 * 身份证号码验证
	 * 
	 * @param text
	 * @return
	 */
	public final static boolean isIdCardNo(final String text) {
		return match(text, "^(\\d{6})()?(\\d{4})(\\d{2})(\\d{2})(\\d{3})(\\w)$");
	}

	/**
	 * 邮政编码验证
	 * 
	 * @param text
	 * @return
	 */
	public final static boolean isZipCode(final String text) {
		return match(text, "^[0-9]{6}$");
	}

	/**
	 * 匹配汉字
	 * 
	 * @param text
	 * @return
	 */
	public final static boolean isChinese(final String text) {
		return match(text, "^[\u4e00-\u9fa5]+$");
	}

	/**
	 * 判断中文字符(包括汉字和符号)
	 * 
	 * @param text
	 * @return
	 */
	public final static boolean isChineseChar(final String text) {
		return match(text, "^[\u0391-\uFFE5]+$");
	}
}
