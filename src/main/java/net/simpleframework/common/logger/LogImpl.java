package net.simpleframework.common.logger;

import static net.simpleframework.common.I18n.$m;

import net.simpleframework.common.StringUtils;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class LogImpl implements Log {

	protected String format(final String message, final Object... args) {
		String msg = message;
		if (!StringUtils.hasText(msg)) {
			msg = $m("LogImpl.0");
		}
		return args.length > 0 ? String.format(msg, args) : msg;
	}

	@Override
	public void debug(final Throwable e) {
		debug(e, null);
	}

	@Override
	public void trace(final Throwable e) {
		trace(e, null);
	}

	@Override
	public void info(final Throwable e) {
		info(e, null);
	}

	@Override
	public void warn(final Throwable e) {
		warn(e, null);
	}

	@Override
	public void error(final Throwable e) {
		error(e, null);
	}
}
