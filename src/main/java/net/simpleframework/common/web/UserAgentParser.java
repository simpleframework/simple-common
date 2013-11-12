package net.simpleframework.common.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.simpleframework.common.th.ParserException;

public class UserAgentParser implements Serializable {

	private static final String SESSION_PARSER_KEY = "@UserAgentParser";

	public static UserAgentParser get(final HttpServletRequest httpRequest) {
		final HttpSession httpSession = httpRequest.getSession();
		UserAgentParser parser = (UserAgentParser) httpSession.getAttribute(SESSION_PARSER_KEY);
		if (parser == null) {
			httpSession.setAttribute(SESSION_PARSER_KEY,
					parser = new UserAgentParser(httpRequest.getHeader("User-Agent")));
		}
		return parser;
	}

	private static Pattern pattern = Pattern
			.compile("([^/\\s]*)(/([^\\s]*))?(\\s*\\[[a-zA-Z][a-zA-Z]\\])?"
					+ "\\s*(\\((([^()]|(\\([^()]*\\)))*)\\))?\\s*");

	private String browserName, browserVersion, browserOperatingSystem;

	private final List<UserAgentDetails> parsedBrowsers = new ArrayList<UserAgentDetails>();

	public UserAgentParser(final String userAgentString) {
		final Matcher matcher = pattern.matcher(userAgentString);

		while (matcher.find()) {
			final String nextBrowserName = matcher.group(1);
			final String nextBrowserVersion = matcher.group(3);
			String nextBrowserComments = null;
			if (matcher.groupCount() >= 6) {
				nextBrowserComments = matcher.group(6);
			}
			parsedBrowsers.add(new UserAgentDetails(nextBrowserName, nextBrowserVersion,
					nextBrowserComments));
		}

		if (parsedBrowsers.size() > 0) {
			processBrowserDetails();
		} else {
			throw ParserException.of("Unable to parse user agent string: " + userAgentString);
		}

	}

	private void processBrowserDetails() {
		final String[] browserNameAndVersion = extractBrowserNameAndVersion();
		browserName = browserNameAndVersion[0];
		browserVersion = browserNameAndVersion[1];

		browserOperatingSystem = extractOperatingSystem(parsedBrowsers.get(0).getBrowserComments());
	}

	private final String[] knownBrowsers = new String[] { "firefox", "netscape", "chrome", "safari",
			"camino", "mosaic", "opera", "galeon" };

	private String[] extractBrowserNameAndVersion() {
		for (final UserAgentDetails nextBrowser : parsedBrowsers) {
			for (final String nextKnown : knownBrowsers) {
				if (nextBrowser.getBrowserName().toLowerCase().startsWith(nextKnown)) {
					return new String[] { nextBrowser.getBrowserName(), nextBrowser.getBrowserVersion() };
				}
				// TODO might need special case here for Opera's dodgy version
			}
		}

		final UserAgentDetails firstAgent = parsedBrowsers.get(0);
		if (firstAgent.getBrowserName().toLowerCase().startsWith("mozilla")) {
			if (firstAgent.getBrowserComments() != null) {
				final String[] comments = firstAgent.getBrowserComments().split(";");
				if (comments.length > 2 && comments[0].toLowerCase().startsWith("compatible")) {
					final String realBrowserWithVersion = comments[1].trim();
					final int firstSpace = realBrowserWithVersion.indexOf(' ');
					final int firstSlash = realBrowserWithVersion.indexOf('/');
					if ((firstSlash > -1 && firstSpace > -1) || (firstSlash > -1 && firstSpace == -1)) {
						// we have slash and space, or just a slash,
						// so let's choose slash for the split
						return new String[] { realBrowserWithVersion.substring(0, firstSlash),
								realBrowserWithVersion.substring(firstSlash + 1) };
					} else if (firstSpace > -1) {
						return new String[] { realBrowserWithVersion.substring(0, firstSpace),
								realBrowserWithVersion.substring(firstSpace + 1) };
					} else {
						// out of ideas for version, or no version supplied
						return new String[] { realBrowserWithVersion, null };
					}
				}
			}

			if (new Float(firstAgent.getBrowserVersion()) < 5.0) {
				return new String[] { "Netscape", firstAgent.getBrowserVersion() };
			} else {
				return new String[] { "Mozilla", firstAgent.getBrowserComments().split(";")[0].trim() };
			}
		} else {
			return new String[] { firstAgent.getBrowserName(), firstAgent.getBrowserVersion() };
		}
	}

	private final String[] knownOS = new String[] { "win", "linux", "mac", "freebsd", "netbsd",
			"openbsd", "sunos", "amiga", "beos", "irix", "os/2", "warp", "iphone", "ipad" };

	private String extractOperatingSystem(final String comments) {
		if (comments == null) {
			return null;
		}
		final List<String> osDetails = new ArrayList<String>();
		final String[] parts = comments.split(";");
		for (final String comment : parts) {
			final String lowerComment = comment.toLowerCase().trim();
			for (final String os : knownOS) {
				if (lowerComment.startsWith(os)) {
					osDetails.add(comment.trim());
				}
			}
		}
		switch (osDetails.size()) {
		case 0: {
			return null;
		}
		case 1: {
			return osDetails.get(0);
		}
		default: {
			return osDetails.get(0); // need to parse more stuff here
		}
		}
	}

	public String getBrowserName() {
		return browserName;
	}

	public String getBrowserVersion() {
		return browserVersion;
	}

	public float getBrowserFloatVersion() {
		try {
			return Float.valueOf(getBrowserVersion());
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	public boolean isIE() {
		return "MSIE".equals(getBrowserName());
	}

	public boolean isGecko() {
		return isEngine("Gecko");
	}

	public boolean isPresto() {
		return isEngine("Presto");
	}

	public boolean isWebKit() {
		return isEngine("AppleWebKit");
	}

	private boolean isEngine(final String name) {
		for (final UserAgentDetails nextBrowser : parsedBrowsers) {
			if (name.equals(nextBrowser.browserName)) {
				return true;
			}
		}
		return false;
	}

	public String getBrowserOperatingSystem() {
		return browserOperatingSystem;
	}

	public static class UserAgentDetails {

		private final String browserName, browserVersion, browserComments;

		UserAgentDetails(final String browserName, final String browserVersion,
				final String browserComments) {
			this.browserName = browserName;
			this.browserVersion = browserVersion;
			this.browserComments = browserComments;
		}

		public String getBrowserComments() {
			return browserComments;
		}

		public String getBrowserName() {
			return browserName;
		}

		public String getBrowserVersion() {
			return browserVersion;
		}
	}

	private static final long serialVersionUID = 3954444736249693671L;
}
