package net.simpleframework.common;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.simpleframework.common.ClassUtils.IScanResourcesCallback;
import net.simpleframework.common.logger.Log;
import net.simpleframework.common.logger.LogFactory;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class I18n {
	// private static Properties charsetProperties = new Properties();

	// static {
	// charsetProperties.setProperty("en", "ISO-8859-1");
	// charsetProperties.setProperty("zh_CN", "GBK");
	// charsetProperties.setProperty("zh_TW", "Big5");
	// }

	// public static String getCharset(final Locale locale) {
	// String charset = charsetProperties.getProperty(locale.toString());
	// if (charset == null) {
	// charset = charsetProperties.getProperty(locale.getLanguage());
	// }
	// if (charset == null) {
	// charset = Charset.defaultCharset().name();
	// }
	// return charset;
	// }

	// public static String getCharset() {
	// return getCharset(getLocale());
	// }

	private static HashSet<String> basenames = new HashSet<String>();
	static {
		addBasename(I18n.class);
	}

	public static void addBasename(final Class<?> pClazz) {
		basenames.add(pClazz.getPackage().getName() + ".message");
	}

	public static void addBasename(final String name) {
		basenames.add(name);
	}

	public static IScanResourcesCallback getBasenamesCallback() {
		return new IScanResourcesCallback() {
			@Override
			public void doResources(final String filepath, final boolean isDirectory) {
				if (filepath.endsWith("message_" + getLocale() + ".properties")) {
					final int pos = filepath.lastIndexOf('/');
					if (pos > -1) {
						final String basename = filepath.substring(0, pos).replace('/', '.') + ".message";
						addBasename(basename);
					}
				}
			}
		};
	}

	public static Locale getLocale() {
		// final HttpSession httpSession = GetSession.getSession();
		return Locale.getDefault();
	}

	public static void setLocale(final Locale locale) {
	}

	/* i18n */
	public static String $m(final String code) {
		return $m(code, (Object[]) null);
	}

	public static String $m(final String code, final Object... args) {
		return $m(code, getLocale(), args);
	}

	public static String $m(final String code, final Locale locale, final Object... args) {
		String result = null;
		for (final String basename : basenames) {
			final ResourceBundle bundle = getResourceBundle(basename, locale);
			if (bundle != null) {
				result = getStringOrNull(bundle, code);
				if (result != null) {
					break;
				}
			}
		}
		if (result != null && args != null && args.length > 0) {
			final MessageFormat format = new MessageFormat(result, locale);
			result = format.format(args);
		}
		return result;
	}

	private static String getStringOrNull(final ResourceBundle bundle, final String key) {
		try {
			return bundle.getString(key);
		} catch (final MissingResourceException ex) {
			return null;
		}
	}

	private static final Map<String, Map<Locale, ResourceBundle>> cachedResourceBundles = new HashMap<String, Map<Locale, ResourceBundle>>();

	private static ResourceBundle getResourceBundle(final String basename, final Locale locale) {
		synchronized (cachedResourceBundles) {
			Map<Locale, ResourceBundle> localeMap = cachedResourceBundles.get(basename);
			if (localeMap != null) {
				final ResourceBundle bundle = localeMap.get(locale);
				if (bundle != null) {
					return bundle;
				}
			}
			try {
				final ResourceBundle bundle = ResourceBundle.getBundle(basename, locale);
				if (localeMap == null) {
					cachedResourceBundles.put(basename,
							localeMap = new HashMap<Locale, ResourceBundle>());
				}
				localeMap.put(locale, bundle);
				return bundle;
			} catch (final MissingResourceException ex) {
				return null;
			}
		}
	}

	private static final Pattern MESSAGE_PATTERN = Pattern
			.compile("[\\s\\S]*(\\#\\([\\w\\.]+\\))[\\s\\S]*");

	public static String replaceI18n(String template) {
		if (template == null) {
			return "";
		}
		while (true) {
			final Matcher matcher = MESSAGE_PATTERN.matcher(template);
			if (matcher.matches()) {
				final MatchResult result = matcher.toMatchResult();
				final String group = result.group(1);
				Object object = $m(group.substring(2, group.length() - 1));
				if (object == null) {
					object = "";
				}
				template = template.substring(0, result.start(1)) + object
						+ template.substring(result.end(1));
			} else {
				break;
			}
		}
		return template;
	}

	static Log log = LogFactory.getLogger(I18n.class);
}
