/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.simpleframework.lib.org.mvel2.templates.res;

import static net.simpleframework.lib.org.mvel2.templates.util.TemplateTools.captureToEOS;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.simpleframework.lib.org.mvel2.MVEL;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.templates.TemplateError;
import net.simpleframework.lib.org.mvel2.templates.TemplateRuntime;
import net.simpleframework.lib.org.mvel2.templates.util.TemplateOutputStream;

public class IncludeNode extends Node {
	// private char[] includeExpression;
	// private char[] preExpression;

	int includeStart;
	int includeOffset;

	int preStart;
	int preOffset;

	public IncludeNode(final int begin, final String name, final char[] template, final int start,
			final int end) {
		this.begin = begin;
		this.name = name;
		this.contents = template;
		this.cStart = start;
		this.cEnd = end - 1;
		this.end = end;
		// this.contents = subset(template, this.cStart = start, (this.end =
		// this.cEnd = end) - start - 1);

		int mark = captureToEOS(contents, 0);
		includeStart = cStart;
		includeOffset = mark - cStart;
		preStart = ++mark;
		preOffset = cEnd - mark;

		// this.includeExpression = subset(contents, 0, mark =
		// captureToEOS(contents, 0));
		// if (mark != contents.length) this.preExpression = subset(contents,
		// ++mark, contents.length - mark);
	}

	@Override
	public Object eval(final TemplateRuntime runtime, final TemplateOutputStream appender,
			final Object ctx, final VariableResolverFactory factory) {
		final String file = MVEL.eval(contents, includeStart, includeOffset, ctx, factory,
				String.class);

		if (preOffset != 0) {
			MVEL.eval(contents, preStart, preOffset, ctx, factory);
		}

		if (next != null) {
			return next.eval(runtime, appender.append(String.valueOf(TemplateRuntime.eval(
					readInFile(runtime, file), ctx, factory))), ctx, factory);
		} else {
			return appender.append(String.valueOf(MVEL.eval(readInFile(runtime, file), ctx, factory)));
		}
	}

	@Override
	public boolean demarcate(final Node terminatingNode, final char[] template) {
		return false;
	}

	public static String readInFile(final TemplateRuntime runtime, final String fileName) {
		final File file = new File(String.valueOf(runtime.getRelPath().peek()) + "/" + fileName);

		try {
			final FileInputStream instream = new FileInputStream(file);
			final BufferedInputStream bufstream = new BufferedInputStream(instream);

			runtime.getRelPath().push(file.getParent());

			final byte[] buf = new byte[10];
			int read;
			int i;

			final StringBuilder appender = new StringBuilder();

			while ((read = bufstream.read(buf)) != -1) {
				for (i = 0; i < read; i++) {
					appender.append((char) buf[i]);
				}
			}

			bufstream.close();
			instream.close();

			runtime.getRelPath().pop();

			return appender.toString();

		} catch (final FileNotFoundException e) {
			throw new TemplateError("cannot include template '" + fileName + "': file not found.");
		} catch (final IOException e) {
			throw new TemplateError("unknown I/O exception while including '" + fileName
					+ "' (stacktrace nested)", e);
		}
	}
}
