package net.simpleframework.common.mail;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

public class ByteArrayAttachment extends EmailAttachment {

	protected final byte[] content;
	protected final InputStream inputStream;
	protected final String contentType;

	public ByteArrayAttachment(final byte[] content, final String contentType, final String name,
			final String contentId) {
		super(name, contentId);
		this.content = content;
		this.inputStream = null;
		this.contentType = contentType;
	}

	public ByteArrayAttachment(final byte[] content, final String contentType, final String name) {
		super(name, null);
		this.content = content;
		this.inputStream = null;
		this.contentType = contentType;
	}

	public ByteArrayAttachment(final InputStream inputStream, final String contentType,
			final String name, final String contentId) {
		super(name, contentId);
		this.content = null;
		this.inputStream = inputStream;
		this.contentType = contentType;
	}

	public ByteArrayAttachment(final InputStream inputStream, final String contentType,
			final String name) {
		super(name, null);
		this.content = null;
		this.inputStream = inputStream;
		this.contentType = contentType;
	}

	@Override
	public DataSource getDataSource() {
		if (inputStream != null) {
			try {
				return new ByteArrayDataSource(inputStream, contentType);
			} catch (final IOException ioex) {
				throw new MailException(ioex);
			}
		}
		if (content != null) {
			return new ByteArrayDataSource(content, contentType);
		}
		throw new MailException("No data source", null);
	}
}