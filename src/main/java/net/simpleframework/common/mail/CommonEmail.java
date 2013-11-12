package net.simpleframework.common.mail;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public abstract class CommonEmail {

	public static final String X_PRIORITY = "X-Priority";

	public static final int PRIORITY_HIGHEST = 1;
	public static final int PRIORITY_HIGH = 2;
	public static final int PRIORITY_NORMAL = 3;
	public static final int PRIORITY_LOW = 4;
	public static final int PRIORITY_LOWEST = 5;

	// ---------------------------------------------------------------- from

	protected String from;

	public void setFrom(final String from) {
		this.from = from;
	}

	public String getFrom() {
		return from;
	}

	// ---------------------------------------------------------------- to

	protected String[] to = new String[0];

	public void setTo(String... tos) {
		if (tos == null) {
			tos = new String[0];
		}
		to = tos;
	}

	public String[] getTo() {
		return to;
	}

	// ---------------------------------------------------------------- reply-to

	protected String[] replyTo = new String[0];

	public void setReplyTo(String... replyTo) {
		if (replyTo == null) {
			replyTo = new String[0];
		}
		this.replyTo = replyTo;
	}

	public String[] getReplyTo() {
		return replyTo;
	}

	// ---------------------------------------------------------------- cc

	protected String[] cc = new String[0];

	public void setCc(String... ccs) {
		if (ccs == null) {
			ccs = new String[0];
		}
		cc = ccs;
	}

	public String[] getCc() {
		return cc;
	}

	// ---------------------------------------------------------------- bcc

	protected String[] bcc = new String[0];

	public void setBcc(String... bccs) {
		if (bccs == null) {
			bccs = new String[0];
		}
		bcc = bccs;
	}

	public String[] getBcc() {
		return bcc;
	}

	// ---------------------------------------------------------------- subject

	protected String subject;

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return this.subject;
	}

	// ---------------------------------------------------------------- message

	protected LinkedList<EmailMessage> messages = new LinkedList<EmailMessage>();

	public LinkedList<EmailMessage> getAllMessages() {
		return messages;
	}

	public void addMessage(final EmailMessage emailMessage) {
		messages.add(emailMessage);
	}

	public void addMessage(final String text, final String mimeType, final String encoding) {
		messages.add(new EmailMessage(text, mimeType, encoding));
	}

	public void addMessage(final String text, final String mimeType) {
		messages.add(new EmailMessage(text, mimeType));
	}

	// ---------------------------------------------------------------- headers

	protected Map<String, String> headers;

	protected Map<String, String> getAllHeaders() {
		return headers;
	}

	public void setHeader(final String name, final String value) {
		if (headers == null) {
			headers = new HashMap<String, String>();
		}
		headers.put(name, value);
	}

	public String getHeader(final String name) {
		if (headers == null) {
			return null;
		}
		return headers.get(name);
	}

	public void setPriority(final int priority) {
		setHeader(X_PRIORITY, String.valueOf(priority));
	}

	public int getPriority() {
		if (headers == null) {
			return -1;
		}
		try {
			return Integer.parseInt(headers.get(X_PRIORITY));
		} catch (final NumberFormatException ignore) {
			return -1;
		}
	}

	// ---------------------------------------------------------------- date

	protected Date sentDate;

	public void setSentDate(final Date date) {
		sentDate = date;
	}

	public Date getSentDate() {
		return sentDate;
	}
}
