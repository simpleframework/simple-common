package net.simpleframework.lib.org.mvel2.templates.util.io;

import java.io.IOException;
import java.io.OutputStream;

import net.simpleframework.lib.org.mvel2.templates.util.TemplateOutputStream;

public class StandardOutputStream implements TemplateOutputStream {
	private final OutputStream outputStream;

	public StandardOutputStream(final OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public TemplateOutputStream append(final CharSequence c) {
		try {
			for (int i = 0; i < c.length(); i++) {
				outputStream.write(c.charAt(i));
			}

			return this;
		} catch (final IOException e) {
			throw new RuntimeException("failed to write to stream", e);
		}
	}

	@Override
	public TemplateOutputStream append(final char[] c) {
		try {

			for (final char i : c) {
				outputStream.write(i);
			}
			return this;
		} catch (final IOException e) {
			throw new RuntimeException("failed to write to stream", e);
		}
	}

	@Override
	public String toString() {
		return null;
	}
}
