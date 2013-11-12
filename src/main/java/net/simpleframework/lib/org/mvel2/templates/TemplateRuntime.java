/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.simpleframework.lib.org.mvel2.templates;

import static net.simpleframework.lib.org.mvel2.templates.TemplateCompiler.compileTemplate;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.ImmutableDefaultFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.MapVariableResolverFactory;
import net.simpleframework.lib.org.mvel2.templates.res.Node;
import net.simpleframework.lib.org.mvel2.templates.util.TemplateOutputStream;
import net.simpleframework.lib.org.mvel2.templates.util.TemplateTools;
import net.simpleframework.lib.org.mvel2.templates.util.io.StandardOutputStream;
import net.simpleframework.lib.org.mvel2.templates.util.io.StringAppenderStream;
import net.simpleframework.lib.org.mvel2.templates.util.io.StringBuilderStream;
import net.simpleframework.lib.org.mvel2.util.ExecutionStack;
import net.simpleframework.lib.org.mvel2.util.StringAppender;

/**
 * This is the root of the template runtime, and contains various utility
 * methods for executing templates.
 */
public class TemplateRuntime {
	private char[] template;
	private TemplateRegistry namedTemplateRegistry;
	private Node rootNode;
	private final String baseDir;
	private ExecutionStack relPath;

	public TemplateRuntime(final char[] template, final TemplateRegistry namedTemplateRegistry,
			final Node rootNode, final String baseDir) {
		this.template = template;
		this.namedTemplateRegistry = namedTemplateRegistry;
		this.rootNode = rootNode;
		this.baseDir = baseDir;
	}

	public static Object eval(final File file, final Object ctx, final VariableResolverFactory vars,
			final TemplateRegistry registry) {
		return execute(compileTemplate(TemplateTools.readInFile(file)), ctx, vars, registry);
	}

	public static Object eval(final InputStream instream) {
		return eval(instream, null, new ImmutableDefaultFactory(), null);
	}

	public static Object eval(final InputStream instream, final Object ctx) {
		return eval(instream, ctx, new ImmutableDefaultFactory(), null);
	}

	public static Object eval(final InputStream instream, final Object ctx,
			final VariableResolverFactory vars) {
		return eval(instream, ctx, vars);
	}

	public static Object eval(final InputStream instream, final Object ctx, final Map vars) {
		return eval(instream, ctx, new MapVariableResolverFactory(vars), null);
	}

	public static Object eval(final InputStream instream, final Object ctx, final Map vars,
			final TemplateRegistry registry) {
		return execute(compileTemplate(TemplateTools.readStream(instream)), ctx,
				new MapVariableResolverFactory(vars), registry);
	}

	public static Object eval(final InputStream instream, final Object ctx,
			final VariableResolverFactory vars, final TemplateRegistry registry) {
		return execute(compileTemplate(TemplateTools.readStream(instream)), ctx, vars, registry);
	}

	public static void eval(final InputStream instream, final Object ctx,
			final VariableResolverFactory vars, final TemplateRegistry register,
			final OutputStream stream) {
		execute(compileTemplate(TemplateTools.readStream(instream)), ctx, vars, register, stream);
	}

	public static Object eval(final String template, final Map vars) {
		return execute(compileTemplate(template), null, new MapVariableResolverFactory(vars));
	}

	public static void eval(final String template, final Map vars, final OutputStream stream) {
		execute(compileTemplate(template), null, new MapVariableResolverFactory(vars), null, stream);
	}

	public static Object eval(final String template, final Object ctx) {
		return execute(compileTemplate(template), ctx);
	}

	public static Object eval(final String template, final Object ctx, final Map vars) {
		return execute(compileTemplate(template), ctx, new MapVariableResolverFactory(vars));
	}

	public static void eval(final String template, final Object ctx, final Map vars,
			final OutputStream stream) {
		execute(compileTemplate(template), ctx, new MapVariableResolverFactory(vars), null, stream);
	}

	public static Object eval(final String template, final Object ctx,
			final VariableResolverFactory vars) {
		return execute(compileTemplate(template), ctx, vars);
	}

	public static void eval(final String template, final Object ctx,
			final VariableResolverFactory vars, final TemplateOutputStream stream) {
		execute(compileTemplate(template), ctx, vars, null, stream);
	}

	public static void eval(final String template, final Object ctx,
			final VariableResolverFactory vars, final OutputStream stream) {
		execute(compileTemplate(template), ctx, vars, null, stream);
	}

	public static Object eval(final String template, final Map vars, final TemplateRegistry registry) {
		return execute(compileTemplate(template), null, new MapVariableResolverFactory(vars),
				registry);
	}

	public static void eval(final String template, final Map vars, final TemplateRegistry registry,
			final TemplateOutputStream stream) {
		execute(compileTemplate(template), null, new MapVariableResolverFactory(vars), registry,
				stream);
	}

	public static void eval(final String template, final Map vars, final TemplateRegistry registry,
			final OutputStream stream) {
		execute(compileTemplate(template), null, new MapVariableResolverFactory(vars), registry,
				stream);
	}

	public static Object eval(final String template, final Object ctx, final Map vars,
			final TemplateRegistry registry) {
		return execute(compileTemplate(template), ctx, new MapVariableResolverFactory(vars), registry);
	}

	public static void eval(final String template, final Object ctx, final Map vars,
			final TemplateRegistry registry, final OutputStream stream) {
		execute(compileTemplate(template), ctx, new MapVariableResolverFactory(vars), registry,
				stream);
	}

	public static Object eval(final String template, final Object ctx,
			final VariableResolverFactory vars, final TemplateRegistry registry) {
		return execute(compileTemplate(template), ctx, vars, registry);
	}

	public static void eval(final String template, final Object ctx,
			final VariableResolverFactory vars, final TemplateRegistry registry,
			final OutputStream stream) {
		execute(compileTemplate(template), ctx, vars, registry, stream);
	}

	public static void eval(final String template, final Object ctx,
			final VariableResolverFactory vars, final TemplateRegistry registry,
			final TemplateOutputStream stream) {
		execute(compileTemplate(template), ctx, vars, registry, stream);
	}

	public static Object execute(final CompiledTemplate compiled) {
		return execute(compiled.getRoot(), compiled.getTemplate(), new StringAppender(), null,
				new ImmutableDefaultFactory(), null);
	}

	public static void execute(final CompiledTemplate compiled, final OutputStream stream) {
		execute(compiled.getRoot(), compiled.getTemplate(), new StandardOutputStream(stream), null,
				new ImmutableDefaultFactory(), null);
	}

	public static Object execute(final CompiledTemplate compiled, final Object context) {
		return execute(compiled.getRoot(), compiled.getTemplate(), new StringAppender(), context,
				new ImmutableDefaultFactory(), null);
	}

	public static void execute(final CompiledTemplate compiled, final Object context,
			final OutputStream stream) {
		execute(compiled.getRoot(), compiled.getTemplate(), new StandardOutputStream(stream),
				context, new ImmutableDefaultFactory(), null);
	}

	public static Object execute(final CompiledTemplate compiled, final Map vars) {
		return execute(compiled.getRoot(), compiled.getTemplate(), new StringBuilder(), null,
				new MapVariableResolverFactory(vars), null);
	}

	public static void execute(final CompiledTemplate compiled, final Map vars,
			final OutputStream stream) {
		execute(compiled.getRoot(), compiled.getTemplate(), new StandardOutputStream(stream), null,
				new MapVariableResolverFactory(vars), null);
	}

	public static Object execute(final CompiledTemplate compiled, final Object context,
			final Map vars) {
		return execute(compiled.getRoot(), compiled.getTemplate(), new StringBuilder(), context,
				new MapVariableResolverFactory(vars), null);
	}

	public static void execute(final CompiledTemplate compiled, final Object context,
			final Map vars, final OutputStream stream) {
		execute(compiled.getRoot(), compiled.getTemplate(), new StandardOutputStream(stream),
				context, new MapVariableResolverFactory(vars), null);
	}

	public static Object execute(final CompiledTemplate compiled, final Object context,
			final TemplateRegistry registry) {
		return execute(compiled.getRoot(), compiled.getTemplate(), new StringBuilder(), context,
				null, registry);
	}

	public static void execute(final CompiledTemplate compiled, final Object context,
			final TemplateRegistry registry, final OutputStream stream) {
		execute(compiled.getRoot(), compiled.getTemplate(), new StandardOutputStream(stream),
				context, null, registry);
	}

	public static Object execute(final CompiledTemplate compiled, final Object context,
			final Map vars, final TemplateRegistry registry) {
		return execute(compiled.getRoot(), compiled.getTemplate(), new StringBuilder(), context,
				new MapVariableResolverFactory(vars), registry);
	}

	public static void execute(final CompiledTemplate compiled, final Object context,
			final Map vars, final TemplateRegistry registry, final OutputStream stream) {
		execute(compiled.getRoot(), compiled.getTemplate(), new StandardOutputStream(stream),
				context, new MapVariableResolverFactory(vars), registry);
	}

	public static Object execute(final CompiledTemplate compiled, final Object context,
			final VariableResolverFactory factory) {
		return execute(compiled.getRoot(), compiled.getTemplate(), new StringBuilder(), context,
				factory, null);
	}

	public static Object execute(final CompiledTemplate compiled, final Object context,
			final VariableResolverFactory factory, final TemplateRegistry registry) {
		return execute(compiled.getRoot(), compiled.getTemplate(), new StringBuilder(), context,
				factory, registry);
	}

	public static Object execute(final CompiledTemplate compiled, final Object context,
			final VariableResolverFactory factory, final String baseDir) {
		return execute(compiled.getRoot(), compiled.getTemplate(), new StringBuilder(), context,
				factory, null, baseDir);
	}

	public static Object execute(final CompiledTemplate compiled, final Object context,
			final VariableResolverFactory factory, final TemplateRegistry registry,
			final String baseDir) {
		return execute(compiled.getRoot(), compiled.getTemplate(), new StringBuilder(), context,
				factory, registry, baseDir);
	}

	public static void execute(final CompiledTemplate compiled, final Object context,
			final VariableResolverFactory factory, final OutputStream stream) {
		execute(compiled.getRoot(), compiled.getTemplate(), new StandardOutputStream(stream),
				context, factory, null);
	}

	public static void execute(final CompiledTemplate compiled, final Object context,
			final VariableResolverFactory factory, final OutputStream stream, final String baseDir) {
		execute(compiled.getRoot(), compiled.getTemplate(), new StandardOutputStream(stream),
				context, factory, null, baseDir);
	}

	public static Object execute(final CompiledTemplate compiled, final Object context,
			final VariableResolverFactory factory, final TemplateRegistry registry,
			final OutputStream stream) {
		return execute(compiled.getRoot(), compiled.getTemplate(), new StandardOutputStream(stream),
				context, factory, registry);
	}

	public static Object execute(final CompiledTemplate compiled, final Object context,
			final VariableResolverFactory factory, final TemplateRegistry registry,
			final TemplateOutputStream stream) {
		return execute(compiled.getRoot(), compiled.getTemplate(), stream, context, factory, registry);
	}

	public static Object execute(final CompiledTemplate compiled, final Object context,
			final VariableResolverFactory factory, final TemplateRegistry registry,
			final TemplateOutputStream stream, final String basedir) {
		return execute(compiled.getRoot(), compiled.getTemplate(), stream, context, factory,
				registry, basedir);
	}

	public static Object execute(final Node root, final char[] template,
			final StringAppender appender, final Object context,
			final VariableResolverFactory factory, final TemplateRegistry registry) {

		return new TemplateRuntime(template, registry, root, ".").execute(appender, context, factory);
	}

	public Object execute(final StringBuilder appender, final Object context,
			final VariableResolverFactory factory) {
		return execute(new StringBuilderStream(appender), context, factory);
	}

	public static Object execute(final Node root, final char[] template,
			final StringBuilder appender, final Object context, final VariableResolverFactory factory,
			final TemplateRegistry registry) {

		return new TemplateRuntime(template, registry, root, ".").execute(appender, context, factory);
	}

	public static Object execute(final Node root, final char[] template,
			final StringBuilder appender, final Object context, final VariableResolverFactory factory,
			final TemplateRegistry registry, final String baseDir) {

		return new TemplateRuntime(template, registry, root, baseDir).execute(appender, context,
				factory);
	}

	public static Object execute(final Node root, final char[] template,
			final TemplateOutputStream appender, final Object context,
			final VariableResolverFactory factory, final TemplateRegistry registry) {

		return new TemplateRuntime(template, registry, root, ".").execute(appender, context, factory);
	}

	public static Object execute(final Node root, final char[] template,
			final TemplateOutputStream appender, final Object context,
			final VariableResolverFactory factory, final TemplateRegistry registry,
			final String baseDir) {

		return new TemplateRuntime(template, registry, root, baseDir).execute(appender, context,
				factory);
	}

	public Object execute(final StringAppender appender, final Object context,
			final VariableResolverFactory factory) {
		return execute(new StringAppenderStream(appender), context, factory);
	}

	public Object execute(final TemplateOutputStream stream, final Object context,
			final VariableResolverFactory factory) {
		return rootNode.eval(this, stream, context, factory);
	}

	public Node getRootNode() {
		return rootNode;
	}

	public void setRootNode(final Node rootNode) {
		this.rootNode = rootNode;
	}

	public char[] getTemplate() {
		return template;
	}

	public void setTemplate(final char[] template) {
		this.template = template;
	}

	public TemplateRegistry getNamedTemplateRegistry() {
		return namedTemplateRegistry;
	}

	public void setNamedTemplateRegistry(final TemplateRegistry namedTemplateRegistry) {
		this.namedTemplateRegistry = namedTemplateRegistry;
	}

	public ExecutionStack getRelPath() {
		if (relPath == null) {
			relPath = new ExecutionStack();
			relPath.push(baseDir);
		}
		return relPath;
	}
}
