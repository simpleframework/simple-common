package net.simpleframework.common.logger;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public interface Log {

	void debug(String message, Object... args);

	void debug(Throwable e, String message, Object... args);

	void debug(Throwable e);

	void trace(String message, Object... args);

	void trace(Throwable e, String message, Object... args);

	void trace(Throwable e);

	void info(String message, Object... args);

	void info(Throwable e, String message, Object... args);

	void info(Throwable e);

	void warn(String message, Object... args);

	void warn(Throwable e, String message, Object... args);

	void warn(Throwable e);

	void error(String message, Object... args);

	void error(Throwable e, String message, Object... args);

	void error(Throwable e);
}
