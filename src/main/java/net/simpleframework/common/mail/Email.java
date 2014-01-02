package net.simpleframework.common.mail;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;

import net.simpleframework.common.MimeTypes;

public class Email extends CommonEmail {

	public static Email of(final String to) {
		return new Email().to(to);
	}

	// ---------------------------------------------------------------- from, to,

	public Email from(final String from) {
		setFrom(from);
		return this;
	}

	public Email to(final String to) {
		setTo(to);
		return this;
	}

	public Email to(final String... tos) {
		setTo(tos);
		return this;
	}

	public Email replyTo(final String replyTo) {
		setReplyTo(replyTo);
		return this;
	}

	public Email replyTo(final String... replyTos) {
		setReplyTo(replyTos);
		return this;
	}

	public Email cc(final String cc) {
		setCc(cc);
		return this;
	}

	public Email cc(final String... ccs) {
		setCc(ccs);
		return this;
	}

	public Email bcc(final String bcc) {
		setBcc(bcc);
		return this;
	}

	public Email bcc(final String... bccs) {
		setBcc(bccs);
		return this;
	}

	// ---------------------------------------------------------------- subject

	public Email subject(final String subject) {
		setSubject(subject);
		return this;
	}

	// ---------------------------------------------------------------- message

	public Email addText(final String text) {
		messages.add(new EmailMessage(text));
		return this;
	}

	public Email addText(final String text, final String encoding) {
		messages.add(new EmailMessage(text, MimeTypes.MIME_TEXT_PLAIN, encoding));
		return this;
	}

	public Email addHtml(final String message) {
		messages.add(new EmailMessage(message, MimeTypes.MIME_TEXT_HTML));
		return this;
	}

	public Email addHtml(final String message, final String encoding) {
		messages.add(new EmailMessage(message, MimeTypes.MIME_TEXT_HTML, encoding));
		return this;
	}

	public Email message(final String text, final String mimeType, final String encoding) {
		addMessage(text, mimeType, encoding);
		return this;
	}

	public Email message(final String text, final String mimeType) {
		addMessage(text, mimeType);
		return this;
	}

	// attachments

	protected LinkedList<EmailAttachment> attachments;

	public LinkedList<EmailAttachment> getAttachments() {
		if (attachments == null) {
			return null;
		}
		return attachments;
	}

	public Email attach(final EmailAttachment emailAttachment) {
		if (attachments == null) {
			attachments = new LinkedList<EmailAttachment>();
		}
		attachments.add(emailAttachment);
		return this;
	}

	public Email attachBytes(final byte[] bytes, final String contentType, final String name) {
		attach(new ByteArrayAttachment(bytes, contentType, name));
		return this;
	}

	public Email attachFile(final String fileName) {
		attach(new FileAttachment(new File(fileName)));
		return this;
	}

	public Email attachFile(final File file) {
		attach(new FileAttachment(file));
		return this;
	}

	public Email embedFile(final String fileName, final String contentId) {
		final File f = new File(fileName);
		attach(new FileAttachment(f, f.getName(), contentId));
		return this;
	}

	public Email embedFile(final File file, final String contentId) {
		attach(new FileAttachment(file, file.getName(), contentId));
		return this;
	}

	public Email embedFile(final String fileName) {
		attach(new FileAttachment(new File(fileName), true));
		return this;
	}

	public Email embedFile(final File file) {
		attach(new FileAttachment(file, true));
		return this;
	}

	// ---------------------------------------------------------------- headers

	public Email header(final String name, final String value) {
		setHeader(name, value);
		return this;
	}

	public Email priority(final int priority) {
		super.setPriority(priority);
		return this;
	}

	// ---------------------------------------------------------------- date

	public Email setCurrentSentDate() {
		sentDate = new Date();
		return this;
	}

	public Email sentOn(final Date date) {
		setSentDate(date);
		return this;
	}

	// ---------------------------------------------------------------- toString

	@Override
	public String toString() {
		return "Email{'" + from + "\', subject='" + subject + "\'}";
	}
}
