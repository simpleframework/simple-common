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

package net.simpleframework.lib.org.mvel2.sh;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.arraycopy;
import static java.lang.System.getProperty;
import static java.util.ResourceBundle.getBundle;
import static net.simpleframework.lib.org.mvel2.MVEL.compileExpression;
import static net.simpleframework.lib.org.mvel2.MVEL.executeExpression;
import static net.simpleframework.lib.org.mvel2.util.PropertyTools.contains;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.simpleframework.lib.org.mvel2.MVELInterpretedRuntime;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.integration.impl.MapVariableResolverFactory;
import net.simpleframework.lib.org.mvel2.sh.command.basic.BasicCommandSet;
import net.simpleframework.lib.org.mvel2.sh.command.file.FileCommandSet;
import net.simpleframework.lib.org.mvel2.templates.TemplateRuntime;
import net.simpleframework.lib.org.mvel2.util.PropertyTools;
import net.simpleframework.lib.org.mvel2.util.StringAppender;

/**
 * A shell session.
 */
public class ShellSession {
	public static final String PROMPT_VAR = "$PROMPT";
	private static final String[] EMPTY = new String[0];

	private final Map<String, Command> commands = new HashMap<>();
	private final Map<String, Object> variables;
	private final Map<String, String> env;
	private Object ctxObject;

	ParserContext pCtx = new ParserContext();
	VariableResolverFactory lvrf;

	private int depth;
	private int cdepth;

	private boolean multi = false;
	private int multiIndentSize = 0;
	private final PrintStream out = System.out;
	private String prompt;
	private String commandBuffer;
	StringAppender inBuffer = new StringAppender();

	final BufferedReader readBuffer = new BufferedReader(new InputStreamReader(System.in));

	public ShellSession() {
		System.out.println("Starting session...");

		variables = new HashMap<>();
		env = new HashMap<>();

		commands.putAll(new BasicCommandSet().load());
		commands.putAll(new FileCommandSet().load());

		env.put(PROMPT_VAR, DefaultEnvironment.PROMPT);
		env.put("$OS_NAME", getProperty("os.name"));
		env.put("$OS_VERSION", getProperty("os.version"));
		env.put("$JAVA_VERSION", PropertyTools.getJavaVersion());
		env.put("$CWD", new File(".").getAbsolutePath());
		env.put("$COMMAND_PASSTRU", "false");
		env.put("$PRINTOUTPUT", "true");
		env.put("$ECHO", "false");
		env.put("$SHOW_TRACES", "true");
		env.put("$USE_OPTIMIZER_ALWAYS", "false");
		env.put("$PATH", "");

		try {
			final ResourceBundle bundle = getBundle(".mvelsh.properties");

			final Enumeration<String> enumer = bundle.getKeys();
			String key;
			while (enumer.hasMoreElements()) {
				env.put(key = enumer.nextElement(), bundle.getString(key));
			}
		} catch (final MissingResourceException e) {
			System.out.println("No config file found.  Loading default config.");

			if (!contains(getProperty("os.name").toLowerCase(), "windows")) {
				env.put("$PATH", "/bin:/usr/bin:/sbin:/usr/sbin");
			}

		}

		lvrf = new MapVariableResolverFactory(variables, new MapVariableResolverFactory(env));

	}

	public ShellSession(final String init) {
		this();
		exec(init);
	}

	private void _exec() {
		String[] inTokens;
		Object outputBuffer;

		final PrintStream sysPrintStream = System.out;
		File execFile;

		if ("true".equals(env.get("$ECHO"))) {
			out.println(">" + commandBuffer);
			out.flush();
		}

		inTokens = inBuffer.append(commandBuffer).toString().split("\\s");

		if (inTokens.length != 0 && commands.containsKey(inTokens[0])) {

			commandBuffer = null;

			String[] passParameters;
			if (inTokens.length > 1) {
				arraycopy(inTokens, 1, passParameters = new String[inTokens.length - 1], 0,
						passParameters.length);
			} else {
				passParameters = EMPTY;
			}

			try {
				commands.get(inTokens[0]).execute(this, passParameters);
			} catch (final CommandException e) {
				out.append("Error: ").append(e.getMessage()).append("\n");
			}
		} else {
			commandBuffer = null;

			try {
				if (shouldDefer(inBuffer)) {
					multi = true;
					return;
				} else {
					multi = false;
				}

				if (parseBoolean(env.get("$USE_OPTIMIZER_ALWAYS"))) {
					outputBuffer = executeExpression(compileExpression(inBuffer.toString()), ctxObject,
							lvrf);
				} else {
					final MVELInterpretedRuntime runtime = new MVELInterpretedRuntime(
							inBuffer.toString(), ctxObject, lvrf, pCtx);
					outputBuffer = runtime.parse();
				}
			} catch (final Exception e) {
				if ("true".equals(env.get("$COMMAND_PASSTHRU"))) {

					String[] paths;
					String s;
					if ((s = inTokens[0]).startsWith("./")) {
						s = new File(env.get("$CWD")).getAbsolutePath() + s.substring(s.indexOf('/'));

						paths = new String[] { s };
					} else {
						paths = env.get("$PATH").split("(:|;)");
					}

					boolean successfulExec = false;

					for (final String execPath : paths) {
						if ((execFile = new File(execPath + "/" + s)).exists() && execFile.isFile()) {
							successfulExec = true;

							final String[] execString = new String[inTokens.length];
							execString[0] = execFile.getAbsolutePath();

							System.arraycopy(inTokens, 1, execString, 1, inTokens.length - 1);

							try {
								final Process p = getRuntime().exec(execString);
								final OutputStream outStream = p.getOutputStream();

								final InputStream inStream = p.getInputStream();
								p.getErrorStream();

								final RunState runState = new RunState(this);

								final Thread pollingThread = new Thread(new Runnable() {
									@Override
									public void run() {
										final byte[] buf = new byte[25];
										int read;

										while (true) {
											try {
												while ((read = inStream.read(buf)) > 0) {
													for (int i = 0; i < read; i++) {
														sysPrintStream.print((char) buf[i]);
													}
													sysPrintStream.flush();
												}

												if (!runState.isRunning()) {
													break;
												}
											} catch (final Exception e) {
												break;
											}
										}

										sysPrintStream.flush();

										if (!multi) {
											multiIndentSize = (prompt = String.valueOf(
													TemplateRuntime.eval(env.get("$PROMPT"), variables)))
															.length();
											out.append(prompt);
										} else {
											out.append(">")
													.append(indent((multiIndentSize - 1) + (depth * 4)));
										}

									}
								});

								final Thread watchThread = new Thread(new Runnable() {
									@Override
									public void run() {

										final Thread runningThread = new Thread(new Runnable() {
											@Override
											public void run() {
												try {
													String read;
													while (runState.isRunning()) {
														while ((read = readBuffer.readLine()) != null) {
															if (runState.isRunning()) {
																for (final char c : read.toCharArray()) {
																	outStream.write((byte) c);
																}
															} else {
																runState.getSession().setCommandBuffer(read);
																break;
															}
														}
													}

													outStream.write((byte) '\n');
													outStream.flush();
												} catch (final Exception e2) {

												}
											}

										});

										runningThread.setPriority(Thread.MIN_PRIORITY);
										runningThread.start();

										try {
											p.waitFor();
										} catch (final InterruptedException e) {
											// nothing;
										}

										sysPrintStream.flush();
										runState.setRunning(false);

										try {
											runningThread.join();
										} catch (final InterruptedException e) {
											// nothing;�
										}
									}
								});

								pollingThread.setPriority(Thread.MIN_PRIORITY);
								pollingThread.start();

								watchThread.setPriority(Thread.MIN_PRIORITY);
								watchThread.start();
								watchThread.join();

								try {
									pollingThread.notify();
								} catch (final Exception ne) {

								}

							} catch (final Exception e2) {
								// fall through;
							}
						}
					}

					if (successfulExec) {
						inBuffer.reset();
						return;
					}
				}

				final ByteArrayOutputStream stackTraceCap = new ByteArrayOutputStream();
				final PrintStream capture = new PrintStream(stackTraceCap);

				e.printStackTrace(capture);
				capture.flush();

				env.put("$LAST_STACK_TRACE", new String(stackTraceCap.toByteArray()));
				if (parseBoolean(env.get("$SHOW_TRACE"))) {
					out.println(env.get("$LAST_STACK_TRACE"));
				} else {
					out.println(e.toString());
				}

				inBuffer.reset();

				return;
			}

			if (outputBuffer != null && "true".equals(env.get("$PRINTOUTPUT"))) {
				if (outputBuffer.getClass().isArray()) {
					out.println(Arrays.toString((Object[]) outputBuffer));
				} else {
					out.println(String.valueOf(outputBuffer));
				}
			}

		}

		inBuffer.reset();

	}

	// todo: fix this
	public void run() {
		final BufferedReader readBuffer = new BufferedReader(new InputStreamReader(System.in));

		try {
			// noinspection InfiniteLoopStatement
			while (true) {
				printPrompt();

				if (commandBuffer == null) {
					commandBuffer = readBuffer.readLine();
				}

				_exec();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			System.out.println("unexpected exception. exiting.");
		}

	}

	public void printPrompt() {
		if (!multi) {
			multiIndentSize = (prompt = String
					.valueOf(TemplateRuntime.eval(env.get("$PROMPT"), variables))).length();
			out.append(prompt);
		} else {
			out.append(">").append(indent((multiIndentSize - 1) + (depth * 4)));
		}
	}

	public boolean shouldDefer(final StringAppender inBuf) {
		final char[] buffer = new char[inBuf.length()];
		inBuf.getChars(0, inBuf.length(), buffer, 0);

		depth = cdepth = 0;
		for (int i = 0; i < buffer.length; i++) {
			switch (buffer[i]) {
			case '/':
				if (i + 1 < buffer.length && buffer[i + 1] == '*') {
					cdepth++;
				}
				break;
			case '*':
				if (i + 1 < buffer.length && buffer[i + 1] == '/') {
					cdepth--;
				}
				break;

			case '{':
				depth++;
				break;
			case '}':
				depth--;
				break;
			}
		}

		return depth + cdepth > 0;
	}

	public String indent(final int size) {
		final StringBuffer sbuf = new StringBuffer();
		for (int i = 0; i < size; i++) {
			sbuf.append(" ");
		}
		return sbuf.toString();
	}

	public Map<String, Command> getCommands() {
		return commands;
	}

	public Map<String, Object> getVariables() {
		return variables;
	}

	public Map<String, String> getEnv() {
		return env;
	}

	public Object getCtxObject() {
		return ctxObject;
	}

	public void setCtxObject(final Object ctxObject) {
		this.ctxObject = ctxObject;
	}

	public String getCommandBuffer() {
		return commandBuffer;
	}

	public void setCommandBuffer(final String commandBuffer) {
		this.commandBuffer = commandBuffer;
	}

	public void exec(final String command) {
		for (final String c : command.split("\n")) {
			inBuffer.append(c);
			_exec();
		}
	}

	public static final class RunState {
		private boolean running = true;
		private ShellSession session;

		public RunState(final ShellSession session) {
			this.session = session;
		}

		public ShellSession getSession() {
			return session;
		}

		public void setSession(final ShellSession session) {
			this.session = session;
		}

		public boolean isRunning() {
			return running;
		}

		public void setRunning(final boolean running) {
			this.running = running;
		}
	}
}
