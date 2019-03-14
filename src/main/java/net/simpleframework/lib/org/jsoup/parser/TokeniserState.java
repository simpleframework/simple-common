package net.simpleframework.lib.org.jsoup.parser;

import net.simpleframework.lib.org.jsoup.nodes.DocumentType;

/**
 * States and transition activations for the Tokeniser.
 */
enum TokeniserState {
	Data {
		// in data state, gather characters until a character reference or tag is
		// found
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			switch (r.current()) {
			case '&':
				t.advanceTransition(CharacterReferenceInData);
				break;
			case '<':
				t.advanceTransition(TagOpen);
				break;
			case nullChar:
				t.error(this); // NOT replacement character (oddly?)
				t.emit(r.consume());
				break;
			case eof:
				t.emit(new Token.EOF());
				break;
			default:
				final String data = r.consumeData();
				t.emit(data);
				break;
			}
		}
	},
	CharacterReferenceInData {
		// from & in data
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			readCharRef(t, Data);
		}
	},
	Rcdata {
		/// handles data in title, textarea etc
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			switch (r.current()) {
			case '&':
				t.advanceTransition(CharacterReferenceInRcdata);
				break;
			case '<':
				t.advanceTransition(RcdataLessthanSign);
				break;
			case nullChar:
				t.error(this);
				r.advance();
				t.emit(replacementChar);
				break;
			case eof:
				t.emit(new Token.EOF());
				break;
			default:
				final String data = r.consumeToAny('&', '<', nullChar);
				t.emit(data);
				break;
			}
		}
	},
	CharacterReferenceInRcdata {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			readCharRef(t, Rcdata);
		}
	},
	Rawtext {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			readData(t, r, this, RawtextLessthanSign);
		}
	},
	ScriptData {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			readData(t, r, this, ScriptDataLessthanSign);
		}
	},
	PLAINTEXT {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			switch (r.current()) {
			case nullChar:
				t.error(this);
				r.advance();
				t.emit(replacementChar);
				break;
			case eof:
				t.emit(new Token.EOF());
				break;
			default:
				final String data = r.consumeTo(nullChar);
				t.emit(data);
				break;
			}
		}
	},
	TagOpen {
		// from < in data
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			switch (r.current()) {
			case '!':
				t.advanceTransition(MarkupDeclarationOpen);
				break;
			case '/':
				t.advanceTransition(EndTagOpen);
				break;
			case '?':
				t.advanceTransition(BogusComment);
				break;
			default:
				if (r.matchesLetter()) {
					t.createTagPending(true);
					t.transition(TagName);
				} else {
					t.error(this);
					t.emit('<'); // char that got us here
					t.transition(Data);
				}
				break;
			}
		}
	},
	EndTagOpen {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.isEmpty()) {
				t.eofError(this);
				t.emit("</");
				t.transition(Data);
			} else if (r.matchesLetter()) {
				t.createTagPending(false);
				t.transition(TagName);
			} else if (r.matches('>')) {
				t.error(this);
				t.advanceTransition(Data);
			} else {
				t.error(this);
				t.advanceTransition(BogusComment);
			}
		}
	},
	TagName {
		// from < or </ in data, will have start or end tag pending
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			// previous TagOpen state did NOT consume, will have a letter char in
			// current
			// String tagName = r.consumeToAnySorted(tagCharsSorted).toLowerCase();
			final String tagName = r.consumeTagName();
			t.tagPending.appendTagName(tagName);

			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				t.transition(BeforeAttributeName);
				break;
			case '/':
				t.transition(SelfClosingStartTag);
				break;
			case '<': // NOTE: out of spec, but clear author intent
				t.error(this);
				r.unconsume();
				// intended fall through to next >
			case '>':
				t.emitTagPending();
				t.transition(Data);
				break;
			case nullChar: // replacement
				t.tagPending.appendTagName(replacementStr);
				break;
			case eof: // should emit pending tag?
				t.eofError(this);
				t.transition(Data);
				break;
			default: // buffer underrun
				t.tagPending.appendTagName(c);
			}
		}
	},
	RcdataLessthanSign {
		// from < in rcdata
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.matches('/')) {
				t.createTempBuffer();
				t.advanceTransition(RCDATAEndTagOpen);
			} else if (r.matchesLetter() && t.appropriateEndTagName() != null
					&& !r.containsIgnoreCase("</" + t.appropriateEndTagName())) {
				// diverge from spec: got a start tag, but there's no appropriate
				// end tag (</title>), so rather than
				// consuming to EOF; break out here
				t.tagPending = t.createTagPending(false).name(t.appropriateEndTagName());
				t.emitTagPending();
				r.unconsume(); // undo "<"
				t.transition(Data);
			} else {
				t.emit("<");
				t.transition(Rcdata);
			}
		}
	},
	RCDATAEndTagOpen {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.matchesLetter()) {
				t.createTagPending(false);
				t.tagPending.appendTagName(r.current());
				t.dataBuffer.append(r.current());
				t.advanceTransition(RCDATAEndTagName);
			} else {
				t.emit("</");
				t.transition(Rcdata);
			}
		}
	},
	RCDATAEndTagName {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.matchesLetter()) {
				final String name = r.consumeLetterSequence();
				t.tagPending.appendTagName(name);
				t.dataBuffer.append(name);
				return;
			}

			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				if (t.isAppropriateEndTagToken()) {
					t.transition(BeforeAttributeName);
				} else {
					anythingElse(t, r);
				}
				break;
			case '/':
				if (t.isAppropriateEndTagToken()) {
					t.transition(SelfClosingStartTag);
				} else {
					anythingElse(t, r);
				}
				break;
			case '>':
				if (t.isAppropriateEndTagToken()) {
					t.emitTagPending();
					t.transition(Data);
				} else {
					anythingElse(t, r);
				}
				break;
			default:
				anythingElse(t, r);
			}
		}

		private void anythingElse(final Tokeniser t, final CharacterReader r) {
			t.emit("</" + t.dataBuffer.toString());
			r.unconsume();
			t.transition(Rcdata);
		}
	},
	RawtextLessthanSign {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.matches('/')) {
				t.createTempBuffer();
				t.advanceTransition(RawtextEndTagOpen);
			} else {
				t.emit('<');
				t.transition(Rawtext);
			}
		}
	},
	RawtextEndTagOpen {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			readEndTag(t, r, RawtextEndTagName, Rawtext);
		}
	},
	RawtextEndTagName {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			handleDataEndTag(t, r, Rawtext);
		}
	},
	ScriptDataLessthanSign {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			switch (r.consume()) {
			case '/':
				t.createTempBuffer();
				t.transition(ScriptDataEndTagOpen);
				break;
			case '!':
				t.emit("<!");
				t.transition(ScriptDataEscapeStart);
				break;
			default:
				t.emit("<");
				r.unconsume();
				t.transition(ScriptData);
			}
		}
	},
	ScriptDataEndTagOpen {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			readEndTag(t, r, ScriptDataEndTagName, ScriptData);
		}
	},
	ScriptDataEndTagName {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			handleDataEndTag(t, r, ScriptData);
		}
	},
	ScriptDataEscapeStart {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.matches('-')) {
				t.emit('-');
				t.advanceTransition(ScriptDataEscapeStartDash);
			} else {
				t.transition(ScriptData);
			}
		}
	},
	ScriptDataEscapeStartDash {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.matches('-')) {
				t.emit('-');
				t.advanceTransition(ScriptDataEscapedDashDash);
			} else {
				t.transition(ScriptData);
			}
		}
	},
	ScriptDataEscaped {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.isEmpty()) {
				t.eofError(this);
				t.transition(Data);
				return;
			}

			switch (r.current()) {
			case '-':
				t.emit('-');
				t.advanceTransition(ScriptDataEscapedDash);
				break;
			case '<':
				t.advanceTransition(ScriptDataEscapedLessthanSign);
				break;
			case nullChar:
				t.error(this);
				r.advance();
				t.emit(replacementChar);
				break;
			default:
				final String data = r.consumeToAny('-', '<', nullChar);
				t.emit(data);
			}
		}
	},
	ScriptDataEscapedDash {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.isEmpty()) {
				t.eofError(this);
				t.transition(Data);
				return;
			}

			final char c = r.consume();
			switch (c) {
			case '-':
				t.emit(c);
				t.transition(ScriptDataEscapedDashDash);
				break;
			case '<':
				t.transition(ScriptDataEscapedLessthanSign);
				break;
			case nullChar:
				t.error(this);
				t.emit(replacementChar);
				t.transition(ScriptDataEscaped);
				break;
			default:
				t.emit(c);
				t.transition(ScriptDataEscaped);
			}
		}
	},
	ScriptDataEscapedDashDash {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.isEmpty()) {
				t.eofError(this);
				t.transition(Data);
				return;
			}

			final char c = r.consume();
			switch (c) {
			case '-':
				t.emit(c);
				break;
			case '<':
				t.transition(ScriptDataEscapedLessthanSign);
				break;
			case '>':
				t.emit(c);
				t.transition(ScriptData);
				break;
			case nullChar:
				t.error(this);
				t.emit(replacementChar);
				t.transition(ScriptDataEscaped);
				break;
			default:
				t.emit(c);
				t.transition(ScriptDataEscaped);
			}
		}
	},
	ScriptDataEscapedLessthanSign {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.matchesLetter()) {
				t.createTempBuffer();
				t.dataBuffer.append(r.current());
				t.emit("<" + r.current());
				t.advanceTransition(ScriptDataDoubleEscapeStart);
			} else if (r.matches('/')) {
				t.createTempBuffer();
				t.advanceTransition(ScriptDataEscapedEndTagOpen);
			} else {
				t.emit('<');
				t.transition(ScriptDataEscaped);
			}
		}
	},
	ScriptDataEscapedEndTagOpen {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.matchesLetter()) {
				t.createTagPending(false);
				t.tagPending.appendTagName(r.current());
				t.dataBuffer.append(r.current());
				t.advanceTransition(ScriptDataEscapedEndTagName);
			} else {
				t.emit("</");
				t.transition(ScriptDataEscaped);
			}
		}
	},
	ScriptDataEscapedEndTagName {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			handleDataEndTag(t, r, ScriptDataEscaped);
		}
	},
	ScriptDataDoubleEscapeStart {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			handleDataDoubleEscapeTag(t, r, ScriptDataDoubleEscaped, ScriptDataEscaped);
		}
	},
	ScriptDataDoubleEscaped {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.current();
			switch (c) {
			case '-':
				t.emit(c);
				t.advanceTransition(ScriptDataDoubleEscapedDash);
				break;
			case '<':
				t.emit(c);
				t.advanceTransition(ScriptDataDoubleEscapedLessthanSign);
				break;
			case nullChar:
				t.error(this);
				r.advance();
				t.emit(replacementChar);
				break;
			case eof:
				t.eofError(this);
				t.transition(Data);
				break;
			default:
				final String data = r.consumeToAny('-', '<', nullChar);
				t.emit(data);
			}
		}
	},
	ScriptDataDoubleEscapedDash {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '-':
				t.emit(c);
				t.transition(ScriptDataDoubleEscapedDashDash);
				break;
			case '<':
				t.emit(c);
				t.transition(ScriptDataDoubleEscapedLessthanSign);
				break;
			case nullChar:
				t.error(this);
				t.emit(replacementChar);
				t.transition(ScriptDataDoubleEscaped);
				break;
			case eof:
				t.eofError(this);
				t.transition(Data);
				break;
			default:
				t.emit(c);
				t.transition(ScriptDataDoubleEscaped);
			}
		}
	},
	ScriptDataDoubleEscapedDashDash {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '-':
				t.emit(c);
				break;
			case '<':
				t.emit(c);
				t.transition(ScriptDataDoubleEscapedLessthanSign);
				break;
			case '>':
				t.emit(c);
				t.transition(ScriptData);
				break;
			case nullChar:
				t.error(this);
				t.emit(replacementChar);
				t.transition(ScriptDataDoubleEscaped);
				break;
			case eof:
				t.eofError(this);
				t.transition(Data);
				break;
			default:
				t.emit(c);
				t.transition(ScriptDataDoubleEscaped);
			}
		}
	},
	ScriptDataDoubleEscapedLessthanSign {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.matches('/')) {
				t.emit('/');
				t.createTempBuffer();
				t.advanceTransition(ScriptDataDoubleEscapeEnd);
			} else {
				t.transition(ScriptDataDoubleEscaped);
			}
		}
	},
	ScriptDataDoubleEscapeEnd {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			handleDataDoubleEscapeTag(t, r, ScriptDataEscaped, ScriptDataDoubleEscaped);
		}
	},
	BeforeAttributeName {
		// from tagname <xxx
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				break; // ignore whitespace
			case '/':
				t.transition(SelfClosingStartTag);
				break;
			case '<': // NOTE: out of spec, but clear (spec has this as a part of
							// the attribute name)
				t.error(this);
				r.unconsume();
				// intended fall through as if >
			case '>':
				t.emitTagPending();
				t.transition(Data);
				break;
			case nullChar:
				t.error(this);
				t.tagPending.newAttribute();
				r.unconsume();
				t.transition(AttributeName);
				break;
			case eof:
				t.eofError(this);
				t.transition(Data);
				break;
			case '"':
			case '\'':
			case '=':
				t.error(this);
				t.tagPending.newAttribute();
				t.tagPending.appendAttributeName(c);
				t.transition(AttributeName);
				break;
			default: // A-Z, anything else
				t.tagPending.newAttribute();
				r.unconsume();
				t.transition(AttributeName);
			}
		}
	},
	AttributeName {
		// from before attribute name
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final String name = r.consumeToAnySorted(attributeNameCharsSorted);
			t.tagPending.appendAttributeName(name);

			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				t.transition(AfterAttributeName);
				break;
			case '/':
				t.transition(SelfClosingStartTag);
				break;
			case '=':
				t.transition(BeforeAttributeValue);
				break;
			case '>':
				t.emitTagPending();
				t.transition(Data);
				break;
			case nullChar:
				t.error(this);
				t.tagPending.appendAttributeName(replacementChar);
				break;
			case eof:
				t.eofError(this);
				t.transition(Data);
				break;
			case '"':
			case '\'':
			case '<':
				t.error(this);
				t.tagPending.appendAttributeName(c);
				break;
			default: // buffer underrun
				t.tagPending.appendAttributeName(c);
			}
		}
	},
	AfterAttributeName {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				// ignore
				break;
			case '/':
				t.transition(SelfClosingStartTag);
				break;
			case '=':
				t.transition(BeforeAttributeValue);
				break;
			case '>':
				t.emitTagPending();
				t.transition(Data);
				break;
			case nullChar:
				t.error(this);
				t.tagPending.appendAttributeName(replacementChar);
				t.transition(AttributeName);
				break;
			case eof:
				t.eofError(this);
				t.transition(Data);
				break;
			case '"':
			case '\'':
			case '<':
				t.error(this);
				t.tagPending.newAttribute();
				t.tagPending.appendAttributeName(c);
				t.transition(AttributeName);
				break;
			default: // A-Z, anything else
				t.tagPending.newAttribute();
				r.unconsume();
				t.transition(AttributeName);
			}
		}
	},
	BeforeAttributeValue {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				// ignore
				break;
			case '"':
				t.transition(AttributeValue_doubleQuoted);
				break;
			case '&':
				r.unconsume();
				t.transition(AttributeValue_unquoted);
				break;
			case '\'':
				t.transition(AttributeValue_singleQuoted);
				break;
			case nullChar:
				t.error(this);
				t.tagPending.appendAttributeValue(replacementChar);
				t.transition(AttributeValue_unquoted);
				break;
			case eof:
				t.eofError(this);
				t.emitTagPending();
				t.transition(Data);
				break;
			case '>':
				t.error(this);
				t.emitTagPending();
				t.transition(Data);
				break;
			case '<':
			case '=':
			case '`':
				t.error(this);
				t.tagPending.appendAttributeValue(c);
				t.transition(AttributeValue_unquoted);
				break;
			default:
				r.unconsume();
				t.transition(AttributeValue_unquoted);
			}
		}
	},
	AttributeValue_doubleQuoted {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final String value = r.consumeToAny(attributeDoubleValueCharsSorted);
			if (value.length() > 0) {
				t.tagPending.appendAttributeValue(value);
			} else {
				t.tagPending.setEmptyAttributeValue();
			}

			final char c = r.consume();
			switch (c) {
			case '"':
				t.transition(AfterAttributeValue_quoted);
				break;
			case '&':
				final int[] ref = t.consumeCharacterReference('"', true);
				if (ref != null) {
					t.tagPending.appendAttributeValue(ref);
				} else {
					t.tagPending.appendAttributeValue('&');
				}
				break;
			case nullChar:
				t.error(this);
				t.tagPending.appendAttributeValue(replacementChar);
				break;
			case eof:
				t.eofError(this);
				t.transition(Data);
				break;
			default: // hit end of buffer in first read, still in attribute
				t.tagPending.appendAttributeValue(c);
			}
		}
	},
	AttributeValue_singleQuoted {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final String value = r.consumeToAny(attributeSingleValueCharsSorted);
			if (value.length() > 0) {
				t.tagPending.appendAttributeValue(value);
			} else {
				t.tagPending.setEmptyAttributeValue();
			}

			final char c = r.consume();
			switch (c) {
			case '\'':
				t.transition(AfterAttributeValue_quoted);
				break;
			case '&':
				final int[] ref = t.consumeCharacterReference('\'', true);
				if (ref != null) {
					t.tagPending.appendAttributeValue(ref);
				} else {
					t.tagPending.appendAttributeValue('&');
				}
				break;
			case nullChar:
				t.error(this);
				t.tagPending.appendAttributeValue(replacementChar);
				break;
			case eof:
				t.eofError(this);
				t.transition(Data);
				break;
			default: // hit end of buffer in first read, still in attribute
				t.tagPending.appendAttributeValue(c);
			}
		}
	},
	AttributeValue_unquoted {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final String value = r.consumeToAnySorted(attributeValueUnquoted);
			if (value.length() > 0) {
				t.tagPending.appendAttributeValue(value);
			}

			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				t.transition(BeforeAttributeName);
				break;
			case '&':
				final int[] ref = t.consumeCharacterReference('>', true);
				if (ref != null) {
					t.tagPending.appendAttributeValue(ref);
				} else {
					t.tagPending.appendAttributeValue('&');
				}
				break;
			case '>':
				t.emitTagPending();
				t.transition(Data);
				break;
			case nullChar:
				t.error(this);
				t.tagPending.appendAttributeValue(replacementChar);
				break;
			case eof:
				t.eofError(this);
				t.transition(Data);
				break;
			case '"':
			case '\'':
			case '<':
			case '=':
			case '`':
				t.error(this);
				t.tagPending.appendAttributeValue(c);
				break;
			default: // hit end of buffer in first read, still in attribute
				t.tagPending.appendAttributeValue(c);
			}

		}
	},
	// CharacterReferenceInAttributeValue state handled inline
	AfterAttributeValue_quoted {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				t.transition(BeforeAttributeName);
				break;
			case '/':
				t.transition(SelfClosingStartTag);
				break;
			case '>':
				t.emitTagPending();
				t.transition(Data);
				break;
			case eof:
				t.eofError(this);
				t.transition(Data);
				break;
			default:
				t.error(this);
				r.unconsume();
				t.transition(BeforeAttributeName);
			}

		}
	},
	SelfClosingStartTag {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '>':
				t.tagPending.selfClosing = true;
				t.emitTagPending();
				t.transition(Data);
				break;
			case eof:
				t.eofError(this);
				t.transition(Data);
				break;
			default:
				t.error(this);
				r.unconsume();
				t.transition(BeforeAttributeName);
			}
		}
	},
	BogusComment {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			// todo: handle bogus comment starting from eof. when does that
			// trigger?
			// rewind to capture character that lead us here
			r.unconsume();
			final Token.Comment comment = new Token.Comment();
			comment.bogus = true;
			comment.data.append(r.consumeTo('>'));
			// todo: replace nullChar with replaceChar
			t.emit(comment);
			t.advanceTransition(Data);
		}
	},
	MarkupDeclarationOpen {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.matchConsume("--")) {
				t.createCommentPending();
				t.transition(CommentStart);
			} else if (r.matchConsumeIgnoreCase("DOCTYPE")) {
				t.transition(Doctype);
			} else if (r.matchConsume("[CDATA[")) {
				// todo: should actually check current namepspace, and only non-html
				// allows cdata. until namespace
				// is implemented properly, keep handling as cdata
				// } else if (!t.currentNodeInHtmlNS() && r.matchConsume("[CDATA["))
				// {
				t.createTempBuffer();
				t.transition(CdataSection);
			} else {
				t.error(this);
				t.advanceTransition(BogusComment); // advance so this character gets
																// in bogus comment data's
																// rewind
			}
		}
	},
	CommentStart {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '-':
				t.transition(CommentStartDash);
				break;
			case nullChar:
				t.error(this);
				t.commentPending.data.append(replacementChar);
				t.transition(Comment);
				break;
			case '>':
				t.error(this);
				t.emitCommentPending();
				t.transition(Data);
				break;
			case eof:
				t.eofError(this);
				t.emitCommentPending();
				t.transition(Data);
				break;
			default:
				t.commentPending.data.append(c);
				t.transition(Comment);
			}
		}
	},
	CommentStartDash {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '-':
				t.transition(CommentStartDash);
				break;
			case nullChar:
				t.error(this);
				t.commentPending.data.append(replacementChar);
				t.transition(Comment);
				break;
			case '>':
				t.error(this);
				t.emitCommentPending();
				t.transition(Data);
				break;
			case eof:
				t.eofError(this);
				t.emitCommentPending();
				t.transition(Data);
				break;
			default:
				t.commentPending.data.append(c);
				t.transition(Comment);
			}
		}
	},
	Comment {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.current();
			switch (c) {
			case '-':
				t.advanceTransition(CommentEndDash);
				break;
			case nullChar:
				t.error(this);
				r.advance();
				t.commentPending.data.append(replacementChar);
				break;
			case eof:
				t.eofError(this);
				t.emitCommentPending();
				t.transition(Data);
				break;
			default:
				t.commentPending.data.append(r.consumeToAny('-', nullChar));
			}
		}
	},
	CommentEndDash {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '-':
				t.transition(CommentEnd);
				break;
			case nullChar:
				t.error(this);
				t.commentPending.data.append('-').append(replacementChar);
				t.transition(Comment);
				break;
			case eof:
				t.eofError(this);
				t.emitCommentPending();
				t.transition(Data);
				break;
			default:
				t.commentPending.data.append('-').append(c);
				t.transition(Comment);
			}
		}
	},
	CommentEnd {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '>':
				t.emitCommentPending();
				t.transition(Data);
				break;
			case nullChar:
				t.error(this);
				t.commentPending.data.append("--").append(replacementChar);
				t.transition(Comment);
				break;
			case '!':
				t.error(this);
				t.transition(CommentEndBang);
				break;
			case '-':
				t.error(this);
				t.commentPending.data.append('-');
				break;
			case eof:
				t.eofError(this);
				t.emitCommentPending();
				t.transition(Data);
				break;
			default:
				t.error(this);
				t.commentPending.data.append("--").append(c);
				t.transition(Comment);
			}
		}
	},
	CommentEndBang {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '-':
				t.commentPending.data.append("--!");
				t.transition(CommentEndDash);
				break;
			case '>':
				t.emitCommentPending();
				t.transition(Data);
				break;
			case nullChar:
				t.error(this);
				t.commentPending.data.append("--!").append(replacementChar);
				t.transition(Comment);
				break;
			case eof:
				t.eofError(this);
				t.emitCommentPending();
				t.transition(Data);
				break;
			default:
				t.commentPending.data.append("--!").append(c);
				t.transition(Comment);
			}
		}
	},
	Doctype {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				t.transition(BeforeDoctypeName);
				break;
			case eof:
				t.eofError(this);
				// note: fall through to > case
			case '>': // catch invalid <!DOCTYPE>
				t.error(this);
				t.createDoctypePending();
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.error(this);
				t.transition(BeforeDoctypeName);
			}
		}
	},
	BeforeDoctypeName {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.matchesLetter()) {
				t.createDoctypePending();
				t.transition(DoctypeName);
				return;
			}
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				break; // ignore whitespace
			case nullChar:
				t.error(this);
				t.createDoctypePending();
				t.doctypePending.name.append(replacementChar);
				t.transition(DoctypeName);
				break;
			case eof:
				t.eofError(this);
				t.createDoctypePending();
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.createDoctypePending();
				t.doctypePending.name.append(c);
				t.transition(DoctypeName);
			}
		}
	},
	DoctypeName {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.matchesLetter()) {
				final String name = r.consumeLetterSequence();
				t.doctypePending.name.append(name);
				return;
			}
			final char c = r.consume();
			switch (c) {
			case '>':
				t.emitDoctypePending();
				t.transition(Data);
				break;
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				t.transition(AfterDoctypeName);
				break;
			case nullChar:
				t.error(this);
				t.doctypePending.name.append(replacementChar);
				break;
			case eof:
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.doctypePending.name.append(c);
			}
		}
	},
	AfterDoctypeName {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			if (r.isEmpty()) {
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				return;
			}
			if (r.matchesAny('\t', '\n', '\r', '\f', ' ')) {
				r.advance(); // ignore whitespace
			} else if (r.matches('>')) {
				t.emitDoctypePending();
				t.advanceTransition(Data);
			} else if (r.matchConsumeIgnoreCase(DocumentType.PUBLIC_KEY)) {
				t.doctypePending.pubSysKey = DocumentType.PUBLIC_KEY;
				t.transition(AfterDoctypePublicKeyword);
			} else if (r.matchConsumeIgnoreCase(DocumentType.SYSTEM_KEY)) {
				t.doctypePending.pubSysKey = DocumentType.SYSTEM_KEY;
				t.transition(AfterDoctypeSystemKeyword);
			} else {
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.advanceTransition(BogusDoctype);
			}

		}
	},
	AfterDoctypePublicKeyword {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				t.transition(BeforeDoctypePublicIdentifier);
				break;
			case '"':
				t.error(this);
				// set public id to empty string
				t.transition(DoctypePublicIdentifier_doubleQuoted);
				break;
			case '\'':
				t.error(this);
				// set public id to empty string
				t.transition(DoctypePublicIdentifier_singleQuoted);
				break;
			case '>':
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			case eof:
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.transition(BogusDoctype);
			}
		}
	},
	BeforeDoctypePublicIdentifier {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				break;
			case '"':
				// set public id to empty string
				t.transition(DoctypePublicIdentifier_doubleQuoted);
				break;
			case '\'':
				// set public id to empty string
				t.transition(DoctypePublicIdentifier_singleQuoted);
				break;
			case '>':
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			case eof:
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.transition(BogusDoctype);
			}
		}
	},
	DoctypePublicIdentifier_doubleQuoted {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '"':
				t.transition(AfterDoctypePublicIdentifier);
				break;
			case nullChar:
				t.error(this);
				t.doctypePending.publicIdentifier.append(replacementChar);
				break;
			case '>':
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			case eof:
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.doctypePending.publicIdentifier.append(c);
			}
		}
	},
	DoctypePublicIdentifier_singleQuoted {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\'':
				t.transition(AfterDoctypePublicIdentifier);
				break;
			case nullChar:
				t.error(this);
				t.doctypePending.publicIdentifier.append(replacementChar);
				break;
			case '>':
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			case eof:
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.doctypePending.publicIdentifier.append(c);
			}
		}
	},
	AfterDoctypePublicIdentifier {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				t.transition(BetweenDoctypePublicAndSystemIdentifiers);
				break;
			case '>':
				t.emitDoctypePending();
				t.transition(Data);
				break;
			case '"':
				t.error(this);
				// system id empty
				t.transition(DoctypeSystemIdentifier_doubleQuoted);
				break;
			case '\'':
				t.error(this);
				// system id empty
				t.transition(DoctypeSystemIdentifier_singleQuoted);
				break;
			case eof:
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.transition(BogusDoctype);
			}
		}
	},
	BetweenDoctypePublicAndSystemIdentifiers {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				break;
			case '>':
				t.emitDoctypePending();
				t.transition(Data);
				break;
			case '"':
				t.error(this);
				// system id empty
				t.transition(DoctypeSystemIdentifier_doubleQuoted);
				break;
			case '\'':
				t.error(this);
				// system id empty
				t.transition(DoctypeSystemIdentifier_singleQuoted);
				break;
			case eof:
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.transition(BogusDoctype);
			}
		}
	},
	AfterDoctypeSystemKeyword {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				t.transition(BeforeDoctypeSystemIdentifier);
				break;
			case '>':
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			case '"':
				t.error(this);
				// system id empty
				t.transition(DoctypeSystemIdentifier_doubleQuoted);
				break;
			case '\'':
				t.error(this);
				// system id empty
				t.transition(DoctypeSystemIdentifier_singleQuoted);
				break;
			case eof:
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
			}
		}
	},
	BeforeDoctypeSystemIdentifier {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				break;
			case '"':
				// set system id to empty string
				t.transition(DoctypeSystemIdentifier_doubleQuoted);
				break;
			case '\'':
				// set public id to empty string
				t.transition(DoctypeSystemIdentifier_singleQuoted);
				break;
			case '>':
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			case eof:
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.transition(BogusDoctype);
			}
		}
	},
	DoctypeSystemIdentifier_doubleQuoted {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '"':
				t.transition(AfterDoctypeSystemIdentifier);
				break;
			case nullChar:
				t.error(this);
				t.doctypePending.systemIdentifier.append(replacementChar);
				break;
			case '>':
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			case eof:
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.doctypePending.systemIdentifier.append(c);
			}
		}
	},
	DoctypeSystemIdentifier_singleQuoted {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\'':
				t.transition(AfterDoctypeSystemIdentifier);
				break;
			case nullChar:
				t.error(this);
				t.doctypePending.systemIdentifier.append(replacementChar);
				break;
			case '>':
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			case eof:
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.doctypePending.systemIdentifier.append(c);
			}
		}
	},
	AfterDoctypeSystemIdentifier {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				break;
			case '>':
				t.emitDoctypePending();
				t.transition(Data);
				break;
			case eof:
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				t.error(this);
				t.transition(BogusDoctype);
				// NOT force quirks
			}
		}
	},
	BogusDoctype {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final char c = r.consume();
			switch (c) {
			case '>':
				t.emitDoctypePending();
				t.transition(Data);
				break;
			case eof:
				t.emitDoctypePending();
				t.transition(Data);
				break;
			default:
				// ignore char
				break;
			}
		}
	},
	CdataSection {
		@Override
		void read(final Tokeniser t, final CharacterReader r) {
			final String data = r.consumeTo("]]>");
			t.dataBuffer.append(data);
			if (r.matchConsume("]]>") || r.isEmpty()) {
				t.emit(new Token.CData(t.dataBuffer.toString()));
				t.transition(Data);
			} // otherwise, buffer underrun, stay in data section
		}
	};

	abstract void read(Tokeniser t, CharacterReader r);

	static final char nullChar = '\u0000';
	// char searches. must be sorted, used in inSorted. MUST update
	// TokenisetStateTest if more arrays are added.
	static final char[] attributeSingleValueCharsSorted = new char[] { nullChar, '&', '\'' };
	static final char[] attributeDoubleValueCharsSorted = new char[] { nullChar, '"', '&' };
	static final char[] attributeNameCharsSorted = new char[] { nullChar, '\t', '\n', '\f', '\r',
			' ', '"', '\'', '/', '<', '=', '>' };
	static final char[] attributeValueUnquoted = new char[] { nullChar, '\t', '\n', '\f', '\r', ' ',
			'"', '&', '\'', '<', '=', '>', '`' };

	private static final char replacementChar = Tokeniser.replacementChar;
	private static final String replacementStr = String.valueOf(Tokeniser.replacementChar);
	private static final char eof = CharacterReader.EOF;

	/**
	 * Handles RawtextEndTagName, ScriptDataEndTagName, and
	 * ScriptDataEscapedEndTagName. Same body impl, just
	 * different else exit transitions.
	 */
	private static void handleDataEndTag(final Tokeniser t, final CharacterReader r,
			final TokeniserState elseTransition) {
		if (r.matchesLetter()) {
			final String name = r.consumeLetterSequence();
			t.tagPending.appendTagName(name);
			t.dataBuffer.append(name);
			return;
		}

		boolean needsExitTransition = false;
		if (t.isAppropriateEndTagToken() && !r.isEmpty()) {
			final char c = r.consume();
			switch (c) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				t.transition(BeforeAttributeName);
				break;
			case '/':
				t.transition(SelfClosingStartTag);
				break;
			case '>':
				t.emitTagPending();
				t.transition(Data);
				break;
			default:
				t.dataBuffer.append(c);
				needsExitTransition = true;
			}
		} else {
			needsExitTransition = true;
		}

		if (needsExitTransition) {
			t.emit("</" + t.dataBuffer.toString());
			t.transition(elseTransition);
		}
	}

	private static void readData(final Tokeniser t, final CharacterReader r,
			final TokeniserState current, final TokeniserState advance) {
		switch (r.current()) {
		case '<':
			t.advanceTransition(advance);
			break;
		case nullChar:
			t.error(current);
			r.advance();
			t.emit(replacementChar);
			break;
		case eof:
			t.emit(new Token.EOF());
			break;
		default:
			final String data = r.consumeToAny('<', nullChar); // todo - why hunt
																				// for
			// null here? Just
			// consumeTo'<'?
			t.emit(data);
			break;
		}
	}

	private static void readCharRef(final Tokeniser t, final TokeniserState advance) {
		final int[] c = t.consumeCharacterReference(null, false);
		if (c == null) {
			t.emit('&');
		} else {
			t.emit(c);
		}
		t.transition(advance);
	}

	private static void readEndTag(final Tokeniser t, final CharacterReader r,
			final TokeniserState a, final TokeniserState b) {
		if (r.matchesLetter()) {
			t.createTagPending(false);
			t.transition(a);
		} else {
			t.emit("</");
			t.transition(b);
		}
	}

	private static void handleDataDoubleEscapeTag(final Tokeniser t, final CharacterReader r,
			final TokeniserState primary, final TokeniserState fallback) {
		if (r.matchesLetter()) {
			final String name = r.consumeLetterSequence();
			t.dataBuffer.append(name);
			t.emit(name);
			return;
		}

		final char c = r.consume();
		switch (c) {
		case '\t':
		case '\n':
		case '\r':
		case '\f':
		case ' ':
		case '/':
		case '>':
			if (t.dataBuffer.toString().equals("script")) {
				t.transition(primary);
			} else {
				t.transition(fallback);
			}
			t.emit(c);
			break;
		default:
			r.unconsume();
			t.transition(fallback);
		}
	}
}
