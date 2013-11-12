package net.simpleframework.lib.org.mvel2.util;

import static net.simpleframework.lib.org.mvel2.util.ParseTools.balancedCaptureWithLineAccounting;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.isIdentifierPart;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subCompileExpression;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.ast.ASTNode;
import net.simpleframework.lib.org.mvel2.ast.EndOfStatement;
import net.simpleframework.lib.org.mvel2.ast.Proto;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;

public class ProtoParser {
	private final char[] expr;
	private final ParserContext pCtx;
	private final int endOffset;

	private int cursor;
	private final String protoName;

	String tk1 = null;
	String tk2 = null;

	private Class type;
	private String name;
	private String deferredName;

	private boolean interpreted = false;

	private final ExecutionStack splitAccumulator;

	private static ThreadLocal<Queue<DeferredTypeResolve>> deferred = new ThreadLocal<Queue<DeferredTypeResolve>>();

	public ProtoParser(final char[] expr, final int offset, final int offsetEnd,
			final String protoName, final ParserContext pCtx, final int fields,
			final ExecutionStack splitAccumulator) {
		this.expr = expr;

		this.cursor = offset;
		this.endOffset = offsetEnd;

		this.protoName = protoName;
		this.pCtx = pCtx;

		this.interpreted = (ASTNode.COMPILE_IMMEDIATE & fields) == 0;

		this.splitAccumulator = splitAccumulator;
	}

	public Proto parse() {
		final Proto proto = new Proto(protoName, pCtx);

		Mainloop: while (cursor < endOffset) {
			cursor = ParseTools.skipWhitespace(expr, cursor);

			int start = cursor;

			if (tk2 == null) {
				while (cursor < endOffset && isIdentifierPart(expr[cursor])) {
					cursor++;
				}

				if (cursor > start) {
					tk1 = new String(expr, start, cursor - start);

					if ("def".equals(tk1) || "function".equals(tk1)) {
						cursor++;
						cursor = ParseTools.skipWhitespace(expr, cursor);
						start = cursor;
						while (cursor < endOffset && isIdentifierPart(expr[cursor])) {
							cursor++;
						}

						if (start == cursor) {
							throw new CompileException(
									"attempt to declare an anonymous function as a prototype member", expr,
									start);
						}

						final FunctionParser parser = new FunctionParser(new String(expr, start, cursor
								- start), cursor, endOffset, expr, 0, pCtx, null);

						proto.declareReceiver(parser.getName(), parser.parse());
						cursor = parser.getCursor() + 1;

						tk1 = null;
						continue;
					}
				}

				cursor = ParseTools.skipWhitespace(expr, cursor);
			}

			if (cursor > endOffset) {
				throw new CompileException("unexpected end of statement in proto declaration: "
						+ protoName, expr, start);
			}

			switch (expr[cursor]) {
			case ';':
				cursor++;
				calculateDecl();

				if (interpreted && type == DeferredTypeResolve.class) {
					/**
					 * If this type could not be immediately resolved, it may be a
					 * look-ahead case, so we defer resolution of the type until
					 * later and place it in the wait queue.
					 */
					enqueueReceiverForLateResolution(deferredName,
							proto.declareReceiver(name, Proto.ReceiverType.DEFERRED, null), null);
				} else {
					proto.declareReceiver(name, type, null);
				}
				break;

			case '=':
				cursor++;
				cursor = ParseTools.skipWhitespace(expr, cursor);
				start = cursor;

				Loop: while (cursor < endOffset) {
					switch (expr[cursor]) {
					case '{':
					case '[':
					case '(':
					case '\'':
					case '"':
						cursor = balancedCaptureWithLineAccounting(expr, cursor, endOffset, expr[cursor],
								pCtx);
						break;

					case ';':
						break Loop;
					}
					cursor++;
				}

				calculateDecl();

				final String initString = new String(expr, start, cursor++ - start);

				if (interpreted && type == DeferredTypeResolve.class) {
					enqueueReceiverForLateResolution(deferredName,
							proto.declareReceiver(name, Proto.ReceiverType.DEFERRED, null), initString);
				} else {
					proto.declareReceiver(name, type,
							(ExecutableStatement) subCompileExpression(initString, pCtx));
				}
				break;

			default:
				start = cursor;
				while (cursor < endOffset && isIdentifierPart(expr[cursor])) {
					cursor++;
				}
				if (cursor > start) {
					tk2 = new String(expr, start, cursor - start);
				}
			}
		}

		cursor++;

		/**
		 * Check if the function is manually terminated.
		 */
		if (splitAccumulator != null && ParseTools.isStatementNotManuallyTerminated(expr, cursor)) {
			/**
			 * Add an EndOfStatement to the split accumulator in the parser.
			 */
			splitAccumulator.add(new EndOfStatement(pCtx));
		}

		return proto;
	}

	private void calculateDecl() {
		if (tk2 != null) {
			try {
				if (pCtx.hasProtoImport(tk1)) {
					type = Proto.class;

				} else {
					type = ParseTools.findClass(null, tk1, pCtx);
				}
				name = tk2;

			} catch (final ClassNotFoundException e) {
				if (interpreted) {
					type = DeferredTypeResolve.class;
					deferredName = tk1;
					name = tk2;
				} else {
					throw new CompileException("could not resolve class: " + tk1, expr, cursor, e);
				}
			}
		} else {
			type = Object.class;
			name = tk1;
		}

		tk1 = null;
		tk2 = null;
	}

	private interface DeferredTypeResolve {
		public boolean isWaitingFor(Proto proto);

		public String getName();

	}

	private void enqueueReceiverForLateResolution(final String name, final Proto.Receiver receiver,
			final String initializer) {
		Queue<DeferredTypeResolve> recv = deferred.get();
		if (recv == null) {
			deferred.set(recv = new LinkedList<DeferredTypeResolve>());
		}

		recv.add(new DeferredTypeResolve() {
			@Override
			public boolean isWaitingFor(final Proto proto) {
				if (name.equals(proto.getName())) {
					receiver.setType(Proto.ReceiverType.PROPERTY);
					receiver.setInitValue((ExecutableStatement) subCompileExpression(initializer, pCtx));
					return true;
				}
				return false;
			}

			@Override
			public String getName() {
				return name;
			}
		});
	}

	public static void notifyForLateResolution(final Proto proto) {
		if (deferred.get() != null) {
			final Queue<DeferredTypeResolve> recv = deferred.get();
			final Set<DeferredTypeResolve> remove = new HashSet<DeferredTypeResolve>();
			for (final DeferredTypeResolve r : recv) {
				if (r.isWaitingFor(proto)) {
					remove.add(r);
				}
			}

			for (final DeferredTypeResolve r : remove) {
				recv.remove(r);
			}
		}
	}

	public int getCursor() {
		return cursor;
	}

	/**
	 * This is such a horrible hack, but it's more performant than any other
	 * horrible hack I can think of right now.
	 * 
	 * @param expr
	 * @param cursor
	 * @param pCtx
	 */
	public static void checkForPossibleUnresolvedViolations(final char[] expr, int cursor,
			final ParserContext pCtx) {
		if (isUnresolvedWaiting()) {
			final LinkedHashMap<String, Object> imports = (LinkedHashMap<String, Object>) pCtx
					.getParserConfiguration().getImports();

			final Object o = imports.values().toArray()[imports.size() - 1];

			if (o instanceof Proto) {
				final Proto proto = (Proto) o;

				final int last = proto.getCursorEnd();
				cursor--;

				/**
				 * We walk backwards to ensure that the last valid statement was a
				 * proto declaration.
				 */

				while (cursor > last && ParseTools.isWhitespace(expr[cursor])) {
					cursor--;
				}
				while (cursor > last && ParseTools.isIdentifierPart(expr[cursor])) {
					cursor--;
				}
				while (cursor > last && (ParseTools.isWhitespace(expr[cursor]) || expr[cursor] == ';')) {
					cursor--;
				}

				if (cursor != last) {
					throw new CompileException(
							"unresolved reference (possible illegal forward-reference?): "
									+ ProtoParser.getNextUnresolvedWaiting(), expr, proto.getCursorStart());
				}
			}
		}
	}

	public static boolean isUnresolvedWaiting() {
		return deferred.get() != null && !deferred.get().isEmpty();
	}

	public static String getNextUnresolvedWaiting() {
		if (deferred.get() != null && !deferred.get().isEmpty()) {
			return deferred.get().poll().getName();
		}
		return null;
	}

}
