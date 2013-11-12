package net.simpleframework.common.mail;

import javax.activation.DataSource;

public class DataSourceAttachment extends EmailAttachment {

	protected final DataSource dataSource;

	public DataSourceAttachment(final DataSource dataSource, final String name,
			final String contentId) {
		super(name, contentId);
		this.dataSource = dataSource;
	}

	public DataSourceAttachment(final DataSource dataSource, final String name) {
		super(name, null);
		this.dataSource = dataSource;
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}
}
