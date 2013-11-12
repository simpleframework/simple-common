package net.simpleframework.lib.org.mvel2.templates.util.io;

import net.simpleframework.lib.org.mvel2.templates.util.TemplateOutputStream;

public class StringBuilderStream implements TemplateOutputStream {
	private final StringBuilder appender;

	public StringBuilderStream(final StringBuilder appender) {
		this.appender = appender;
	}

	@Override
	public TemplateOutputStream append(final CharSequence c) {
		appender.append(c);
		return this;
	}

	@Override
	public TemplateOutputStream append(final char[] c) {
		appender.append(c);
		return this;
	}

	@Override
	public String toString() {
		return appender.toString();
	}
}
