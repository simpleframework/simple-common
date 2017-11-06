package net.simpleframework.lib.org.jsoup.parser;

import java.util.Arrays;

import net.simpleframework.lib.org.jsoup.helper.StringUtil;
import net.simpleframework.lib.org.jsoup.helper.Validate;
import net.simpleframework.lib.org.jsoup.nodes.Entities;

/**
 * Readers the input stream into tokens.
 */
final class Tokeniser {
	static final char replacementChar = '\uFFFD'; // replaces null character
	private static final char[] notCharRefCharsSorted = new char[] { '\t', '\n', '\r', '\f', ' ',
			'<', '&' };

	static {
		Arrays.sort(notCharRefCharsSorted);
	}

	private final CharacterReader reader; // html input
	private final ParseErrorList errors; // errors found while tokenising

	private TokeniserState state = TokeniserState.Data; // current tokenisation
																			// state
	private Token emitPending; // the token we are about to emit on next read
	private boolean isEmitPending = false;
	private String charsString = null; // characters pending an emit. Will fall
													// to charsBuilder if more than one
	private final StringBuilder charsBuilder = new StringBuilder(1024); // buffers
																								// characters
																								// to
																								// output
																								// as
																								// one
																								// token,
																								// if
																								// more
																								// than
																								// one
																								// emit
																								// per
																								// read
	StringBuilder dataBuffer = new StringBuilder(1024); // buffers data looking
																			// for </script>

	Token.Tag tagPending; // tag we are building up
	Token.StartTag startPending = new Token.StartTag();
	Token.EndTag endPending = new Token.EndTag();
	Token.Character charPending = new Token.Character();
	Token.Doctype doctypePending = new Token.Doctype(); // doctype building up
	Token.Comment commentPending = new Token.Comment(); // comment building up
	private String lastStartTag; // the last start tag emitted, to test
											// appropriate end tag

	Tokeniser(final CharacterReader reader, final ParseErrorList errors) {
		this.reader = reader;
		this.errors = errors;
	}

	Token read() {
		while (!isEmitPending) {
			state.read(this, reader);
		}

		// if emit is pending, a non-character token was found: return any chars
		// in buffer, and leave token for next read:
		if (charsBuilder.length() > 0) {
			final String str = charsBuilder.toString();
			charsBuilder.delete(0, charsBuilder.length());
			charsString = null;
			return charPending.data(str);
		} else if (charsString != null) {
			final Token token = charPending.data(charsString);
			charsString = null;
			return token;
		} else {
			isEmitPending = false;
			return emitPending;
		}
	}

	void emit(final Token token) {
		Validate.isFalse(isEmitPending, "There is an unread token pending!");

		emitPending = token;
		isEmitPending = true;

		if (token.type == Token.TokenType.StartTag) {
			final Token.StartTag startTag = (Token.StartTag) token;
			lastStartTag = startTag.tagName;
		} else if (token.type == Token.TokenType.EndTag) {
			final Token.EndTag endTag = (Token.EndTag) token;
			if (endTag.attributes != null) {
				error("Attributes incorrectly present on end tag");
			}
		}
	}

	void emit(final String str) {
		// buffer strings up until last string token found, to emit only one token
		// for a run of character refs etc.
		// does not set isEmitPending; read checks that
		if (charsString == null) {
			charsString = str;
		} else {
			if (charsBuilder.length() == 0) { // switching to string builder as
															// more than one emit before read
				charsBuilder.append(charsString);
			}
			charsBuilder.append(str);
		}
	}

	void emit(final char[] chars) {
		emit(String.valueOf(chars));
	}

	void emit(final int[] codepoints) {
		emit(new String(codepoints, 0, codepoints.length));
	}

	void emit(final char c) {
		emit(String.valueOf(c));
	}

	TokeniserState getState() {
		return state;
	}

	void transition(final TokeniserState state) {
		this.state = state;
	}

	void advanceTransition(final TokeniserState state) {
		reader.advance();
		this.state = state;
	}

	final private int[] codepointHolder = new int[1]; // holder to not have to
																		// keep creating arrays
	final private int[] multipointHolder = new int[2];

	int[] consumeCharacterReference(final Character additionalAllowedCharacter,
			final boolean inAttribute) {
		if (reader.isEmpty()) {
			return null;
		}
		if (additionalAllowedCharacter != null && additionalAllowedCharacter == reader.current()) {
			return null;
		}
		if (reader.matchesAnySorted(notCharRefCharsSorted)) {
			return null;
		}

		final int[] codeRef = codepointHolder;
		reader.mark();
		if (reader.matchConsume("#")) { // numbered
			final boolean isHexMode = reader.matchConsumeIgnoreCase("X");
			final String numRef = isHexMode ? reader.consumeHexSequence()
					: reader.consumeDigitSequence();
			if (numRef.length() == 0) { // didn't match anything
				characterReferenceError("numeric reference with no numerals");
				reader.rewindToMark();
				return null;
			}
			if (!reader.matchConsume(";")) {
				characterReferenceError("missing semicolon"); // missing semi
			}
			int charval = -1;
			try {
				final int base = isHexMode ? 16 : 10;
				charval = Integer.valueOf(numRef, base);
			} catch (final NumberFormatException ignored) {
			} // skip
			if (charval == -1 || (charval >= 0xD800 && charval <= 0xDFFF) || charval > 0x10FFFF) {
				characterReferenceError("character outside of valid range");
				codeRef[0] = replacementChar;
				return codeRef;
			} else {
				// todo: implement number replacement table
				// todo: check for extra illegal unicode points as parse errors
				codeRef[0] = charval;
				return codeRef;
			}
		} else { // named
			// get as many letters as possible, and look for matching entities.
			final String nameRef = reader.consumeLetterThenDigitSequence();
			final boolean looksLegit = reader.matches(';');
			// found if a base named entity without a ;, or an extended entity with
			// the ;.
			final boolean found = (Entities.isBaseNamedEntity(nameRef)
					|| (Entities.isNamedEntity(nameRef) && looksLegit));

			if (!found) {
				reader.rewindToMark();
				if (looksLegit) {
					characterReferenceError(String.format("invalid named referenece '%s'", nameRef));
				}
				return null;
			}
			if (inAttribute && (reader.matchesLetter() || reader.matchesDigit()
					|| reader.matchesAny('=', '-', '_'))) {
				// don't want that to match
				reader.rewindToMark();
				return null;
			}
			if (!reader.matchConsume(";")) {
				characterReferenceError("missing semicolon"); // missing semi
			}
			final int numChars = Entities.codepointsForName(nameRef, multipointHolder);
			if (numChars == 1) {
				codeRef[0] = multipointHolder[0];
				return codeRef;
			} else if (numChars == 2) {
				return multipointHolder;
			} else {
				Validate.fail("Unexpected characters returned for " + nameRef);
				return multipointHolder;
			}
		}
	}

	Token.Tag createTagPending(final boolean start) {
		tagPending = start ? startPending.reset() : endPending.reset();
		return tagPending;
	}

	void emitTagPending() {
		tagPending.finaliseTag();
		emit(tagPending);
	}

	void createCommentPending() {
		commentPending.reset();
	}

	void emitCommentPending() {
		emit(commentPending);
	}

	void createDoctypePending() {
		doctypePending.reset();
	}

	void emitDoctypePending() {
		emit(doctypePending);
	}

	void createTempBuffer() {
		Token.reset(dataBuffer);
	}

	boolean isAppropriateEndTagToken() {
		return lastStartTag != null && tagPending.name().equalsIgnoreCase(lastStartTag);
	}

	String appropriateEndTagName() {
		return lastStartTag; // could be null
	}

	void error(final TokeniserState state) {
		if (errors.canAddError()) {
			errors.add(new ParseError(reader.pos(), "Unexpected character '%s' in input state [%s]",
					reader.current(), state));
		}
	}

	void eofError(final TokeniserState state) {
		if (errors.canAddError()) {
			errors.add(new ParseError(reader.pos(),
					"Unexpectedly reached end of file (EOF) in input state [%s]", state));
		}
	}

	private void characterReferenceError(final String message) {
		if (errors.canAddError()) {
			errors.add(new ParseError(reader.pos(), "Invalid character reference: %s", message));
		}
	}

	void error(final String errorMsg) {
		if (errors.canAddError()) {
			errors.add(new ParseError(reader.pos(), errorMsg));
		}
	}

	boolean currentNodeInHtmlNS() {
		// todo: implement namespaces correctly
		return true;
		// Element currentNode = currentNode();
		// return currentNode != null && currentNode.namespace().equals("HTML");
	}

	/**
	 * Utility method to consume reader and unescape entities found within.
	 * 
	 * @param inAttribute
	 *        if the text to be unescaped is in an attribute
	 * @return unescaped string from reader
	 */
	String unescapeEntities(final boolean inAttribute) {
		final StringBuilder builder = StringUtil.stringBuilder();
		while (!reader.isEmpty()) {
			builder.append(reader.consumeTo('&'));
			if (reader.matches('&')) {
				reader.consume();
				final int[] c = consumeCharacterReference(null, inAttribute);
				if (c == null || c.length == 0) {
					builder.append('&');
				} else {
					builder.appendCodePoint(c[0]);
					if (c.length == 2) {
						builder.appendCodePoint(c[1]);
					}
				}

			}
		}
		return builder.toString();
	}
}
