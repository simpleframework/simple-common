package net.simpleframework.lib.org.mvel2.templates.util.io;

import net.simpleframework.lib.org.mvel2.templates.util.TemplateOutputStream;
import net.simpleframework.lib.org.mvel2.util.StringAppender;

public class StringAppenderStream implements TemplateOutputStream {
	private final StringAppender appender;

	public StringAppenderStream(final StringAppender appender) {
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
