package net.simpleframework.common.mail;

import java.io.File;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

public class FileAttachment extends EmailAttachment {

	protected final File file;

	public FileAttachment(final File file, final String name, final String contentId) {
		super(name, contentId);
		this.file = file;
	}

	public FileAttachment(final File file, final boolean inline) {
		super(file.getName(), inline ? file.getName() : null);
		this.file = file;
	}

	public FileAttachment(final File file) {
		this(file, false);
	}

	public File getFile() {
		return file;
	}

	@Override
	public DataSource getDataSource() {
		return new FileDataSource(file);
	}
}