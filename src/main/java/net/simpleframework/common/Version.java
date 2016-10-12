package net.simpleframework.common;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */

public final class Version implements Serializable, Comparable<Version> {

	private static final long serialVersionUID = -5739492907539237198L;

	public static Version getVersion(final String version)
			throws NumberFormatException, IllegalArgumentException {
		final StringTokenizer tokenizer = new StringTokenizer(version, ".");
		final String[] levels = new String[tokenizer.countTokens()];
		for (int i = 0; i < levels.length; i++) {
			levels[i] = tokenizer.nextToken();
		}

		if ((0 == levels.length) || (3 < levels.length)) {
			throw new IllegalArgumentException("Malformed version string " + version);
		}

		final int major = Integer.parseInt(levels[0]);

		int minor = 0;
		if (1 < levels.length) {
			minor = Integer.parseInt(levels[1]);
		}

		int micro = 0;
		if (2 < levels.length) {
			micro = Integer.parseInt(levels[2]);
		}

		return new Version(major, minor, micro);
	}

	private int major;

	private int minor;

	private int micro;

	public Version(final int major, final int minor, final int micro) {
		this.major = major;
		this.minor = minor;
		this.micro = micro;
	}

	@Override
	public int compareTo(final Version o) {
		if (equals(o)) {
			return 0;
		} else if (complies(o)) {
			return 1;
		} else {
			return -1;
		}
	}

	public boolean complies(final Version other) {
		if (major > other.major) {
			return true;
		} else if (major == other.major) {
			if (minor > other.minor) {
				return true;
			} else if (minor == other.minor) {
				if (micro > other.micro) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof Version) {
			return equals((Version) other);
		} else {
			return false;
		}
	}

	public boolean equals(final Version other) {
		if (major != other.major) {
			return false;
		} else if (minor != other.minor) {
			return false;
		} else if (micro != other.micro) {
			return false;
		} else {
			return true;
		}
	}

	public int getMajor() {
		return major;
	}

	public int incMajor() {
		return ++major;
	}

	public int getMinor() {
		return minor;
	}

	public int incMinor() {
		return ++minor;
	}

	public int getMicro() {
		return micro;
	}

	public int incMicro() {
		return ++micro;
	}

	@Override
	public String toString() {
		return major + "." + minor + "." + micro;
	}
}
