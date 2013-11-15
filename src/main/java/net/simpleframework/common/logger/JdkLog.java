package net.simpleframework.common.logger;

import java.util.logging.Level;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class JdkLog extends LogImpl {

	// ConsoleHandler consoleHandler = new ConsoleHandler();
	// {
	// consoleHandler.setLevel(Level.INFO);
	// }

	private final java.util.logging.Logger _log;

	public JdkLog(final String name) {
		_log = java.util.logging.Logger.getLogger(name);
		// _log.addHandler(consoleHandler);
	}

	@Override
	public void debug(final String message, final Object... args) {
		_log.log(Level.FINE, format(message, args));
	}

	@Override
	public void debug(final Throwable e, final String message, final Object... args) {
		_log.log(Level.FINE, format(message, args), e);
	}

	@Override
	public void trace(final String message, final Object... args) {
		_log.log(Level.FINER, format(message, args));
	}

	@Override
	public void trace(final Throwable e, final String message, final Object... args) {
		_log.log(Level.FINER, format(message, args), e);
	}

	@Override
	public void info(final String message, final Object... args) {
		_log.log(Level.INFO, format(message, args));
	}

	@Override
	public void info(final Throwable e, final String message, final Object... args) {
		_log.log(Level.INFO, format(message, args), e);
	}

	@Override
	public void warn(final String message, final Object... args) {
		_log.log(Level.WARNING, format(message, args));
	}

	@Override
	public void warn(final Throwable e, final String message, final Object... args) {
		_log.log(Level.WARNING, format(message, args), e);
	}

	@Override
	public void error(final String message, final Object... args) {
		_log.log(Level.SEVERE, format(message, args));
	}

	@Override
	public void error(final Throwable e, final String message, final Object... args) {
		_log.log(Level.SEVERE, format(message, args), e);
	}
}
