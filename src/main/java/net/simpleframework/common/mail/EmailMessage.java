package net.simpleframework.common.mail;

import net.simpleframework.common.MimeTypes;

public class EmailMessage {

	private final String content;

	private final String mimeType;

	private final String encoding;

	public EmailMessage(final String content, final String mimeType, final String encoding) {
		this.content = content;
		this.mimeType = mimeType;
		this.encoding = encoding;
	}

	public EmailMessage(final String content, final String mimeType) {
		this(content, mimeType, "utf-8");
	}

	public EmailMessage(final String content) {
		this(content, MimeTypes.MIME_TEXT_PLAIN);
	}

	// ---------------------------------------------------------------- getters

	public String getContent() {
		return content;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getEncoding() {
		return encoding;
	}
}