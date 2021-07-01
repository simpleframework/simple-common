package net.simpleframework.common.logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class LogFactory {

	static Map<Class<?>, Log> lCache;
	static {
		lCache = new ConcurrentHashMap<>();
	}
	
	private static LogCreator DEFAULT_CREATOR = new LogCreator();
	private static LogCreator _logCreator = DEFAULT_CREATOR;
	

	public static Log getLogger(final Class<?> beanClass) {
		Log log = lCache.get(beanClass);
		if (log == null) {
			lCache.put(beanClass, log = _logCreator.createLog(beanClass));
		}
		return log;
	}
	
	public static void setLogger(LogCreator logCreator) {
		_logCreator = logCreator;
	}
	
	public static class LogCreator {
		protected Log createLog(final Class<?> beanClass) {
			return new JdkLog(beanClass.getName());
		}
	}
}
