package net.simpleframework.common.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReceivedEmail extends CommonEmail {

	public static final int ANSWERED = 1;
	public static final int DELETED = 2;
	public static final int DRAFT = 4;
	public static final int FLAGGED = 8;
	public static final int RECENT = 16;
	public static final int SEEN = 32;
	public static final int USER = 0x80000000;

	// ---------------------------------------------------------------- number
	// and flag

	protected int messageNumber;

	public int getMessageNumber() {
		return messageNumber;
	}

	public void setMessageNumber(final int messageNumber) {
		this.messageNumber = messageNumber;
	}

	protected int flags;

	public int getFlags() {
		return flags;
	}

	public void setFlags(final int flags) {
		this.flags = flags;
	}

	public void addFlags(final int flag) {
		this.flags |= flag;
	}

	public void removeFlags(final int flag) {
		this.flags &= ~flag;
	}

	public boolean hasFlags(final int flags) {
		return (this.flags & flags) != 0;
	}

	// ---------------------------------------------------------------- date

	protected Date recvDate;

	public void setReceiveDate(final Date date) {
		recvDate = date;
	}

	public Date getReceiveDate() {
		return recvDate;
	}

	// ----------------------------------------------------------------
	// attachments

	protected List<EmailAttachment> attachments;

	public void addAttachment(final String filename, final String mimeType, final String contentId,
			final byte[] content) {
		if (attachments == null) {
			attachments = new ArrayList<EmailAttachment>();
		}
		final EmailAttachment emailAttachment = new ByteArrayAttachment(content, mimeType, filename,
				contentId);
		emailAttachment.setSize(content.length);
		attachments.add(emailAttachment);
	}

	public List<EmailAttachment> getAttachments() {
		return attachments;
	}
}