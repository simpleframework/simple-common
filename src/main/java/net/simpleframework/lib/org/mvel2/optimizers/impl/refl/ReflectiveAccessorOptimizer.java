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
package net.simpleframework.lib.org.mvel2.optimizers.impl.refl;

import static java.lang.Integer.parseInt;
import static java.lang.Thread.currentThread;
import static java.lang.reflect.Array.getLength;
import static net.simpleframework.lib.org.mvel2.DataConversion.canConvert;
import static net.simpleframework.lib.org.mvel2.DataConversion.convert;
import static net.simpleframework.lib.org.mvel2.MVEL.eval;
import static net.simpleframework.lib.org.mvel2.ast.TypeDescriptor.getClassReference;
import static net.simpleframework.lib.org.mvel2.integration.GlobalListenerFactory.hasSetListeners;
import static net.simpleframework.lib.org.mvel2.integration.GlobalListenerFactory.notifyGetListeners;
import static net.simpleframework.lib.org.mvel2.integration.GlobalListenerFactory.notifySetListeners;
import static net.simpleframework.lib.org.mvel2.integration.PropertyHandlerFactory.getNullMethodHandler;
import static net.simpleframework.lib.org.mvel2.integration.PropertyHandlerFactory.getNullPropertyHandler;
import static net.simpleframework.lib.org.mvel2.integration.PropertyHandlerFactory.getPropertyHandler;
import static net.simpleframework.lib.org.mvel2.integration.PropertyHandlerFactory.hasNullMethodHandler;
import static net.simpleframework.lib.org.mvel2.integration.PropertyHandlerFactory.hasNullPropertyHandler;
import static net.simpleframework.lib.org.mvel2.integration.PropertyHandlerFactory.hasPropertyHandler;
import static net.simpleframework.lib.org.mvel2.util.CompilerTools.expectType;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.EMPTY_OBJ_ARR;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.balancedCapture;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.balancedCaptureWithLineAccounting;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.captureContructorAndResidual;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.determineActualTargetMethod;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.findClass;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.getBaseComponentType;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.getBestCandidate;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.getBestConstructorCandidate;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.getSubComponentType;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.getWidenedTarget;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.parseMethodOrConstructor;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.parseParameterList;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.repeatChar;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subCompileExpression;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subset;
import static net.simpleframework.lib.org.mvel2.util.PropertyTools.getFieldOrAccessor;
import static net.simpleframework.lib.org.mvel2.util.PropertyTools.getFieldOrWriteAccessor;
import static net.simpleframework.lib.org.mvel2.util.ReflectionUtil.toNonPrimitiveType;
import static net.simpleframework.lib.org.mvel2.util.Varargs.normalizeArgsForVarArgs;
import static net.simpleframework.lib.org.mvel2.util.Varargs.paramTypeVarArgsSafe;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.MVEL;
import net.simpleframework.lib.org.mvel2.OptimizationFailure;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.PropertyAccessException;
import net.simpleframework.lib.org.mvel2.ast.FunctionInstance;
import net.simpleframework.lib.org.mvel2.ast.TypeDescriptor;
import net.simpleframework.lib.org.mvel2.compiler.Accessor;
import net.simpleframework.lib.org.mvel2.compiler.AccessorNode;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableLiteral;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.compiler.PropertyVerifier;
import net.simpleframework.lib.org.mvel2.integration.GlobalListenerFactory;
import net.simpleframework.lib.org.mvel2.integration.PropertyHandler;
import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.optimizers.AbstractOptimizer;
import net.simpleframework.lib.org.mvel2.optimizers.AccessorOptimizer;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.collection.ArrayCreator;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.collection.ExprValueAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.collection.ListCreator;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.collection.MapCreator;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.ArrayAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.ArrayAccessorNest;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.ArrayLength;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.ConstructorAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.DynamicFieldAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.DynamicFunctionAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.FieldAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.FieldAccessorNH;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.FunctionAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.GetterAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.GetterAccessorNH;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.IndexedCharSeqAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.IndexedCharSeqAccessorNest;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.IndexedVariableAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.ListAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.ListAccessorNest;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.MapAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.MapAccessorNest;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.MethodAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.MethodAccessorNH;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.Notify;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.NullSafe;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.PropertyHandlerAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.SetterAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.StaticReferenceAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.StaticVarAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.StaticVarAccessorNH;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.ThisValueAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.Union;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.VariableAccessor;
import net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes.WithAccessor;
import net.simpleframework.lib.org.mvel2.util.ArrayTools;
import net.simpleframework.lib.org.mvel2.util.ErrorUtil;
import net.simpleframework.lib.org.mvel2.util.MethodStub;
import net.simpleframework.lib.org.mvel2.util.NullType;
import net.simpleframework.lib.org.mvel2.util.ParseTools;
import net.simpleframework.lib.org.mvel2.util.PropertyTools;
import net.simpleframework.lib.org.mvel2.util.StringAppender;

public class ReflectiveAccessorOptimizer extends AbstractOptimizer implements AccessorOptimizer {
	private AccessorNode rootNode;
	private AccessorNode currNode;

	private Object ctx;
	private Object thisRef;
	private Object val;

	private VariableResolverFactory variableFactory;

	private static final int DONE = -1;

	private static final Object[] EMPTYARG = new Object[0];
	private static final Class[] EMPTYCLS = new Class[0];

	private boolean first = true;

	private Class ingressType;
	private Class returnType;

	public ReflectiveAccessorOptimizer() {
	}

	@Override
	public void init() {
	}

	private ReflectiveAccessorOptimizer(final ParserContext pCtx, final char[] property,
			final int start, final int offset, final Object ctx, final Object thisRef,
			final VariableResolverFactory variableFactory) {
		this.pCtx = pCtx;
		this.expr = property;
		this.start = start;
		this.length = property != null ? offset : start;
		this.end = start + length;
		this.ctx = ctx;
		this.variableFactory = variableFactory;
		this.thisRef = thisRef;
	}

	@Override
	public Accessor optimizeAccessor(final ParserContext pCtx, final char[] property,
			final int start, final int offset, final Object ctx, final Object thisRef,
			final VariableResolverFactory factory, final boolean root, final Class ingressType) {
		this.rootNode = this.currNode = null;
		this.expr = property;
		this.start = start;
		this.end = start + offset;
		this.length = end - start;

		this.first = true;
		this.ctx = ctx;
		this.thisRef = thisRef;
		this.variableFactory = factory;
		this.ingressType = ingressType;

		this.pCtx = pCtx;

		return compileGetChain();
	}

	@Override
	public Accessor optimizeSetAccessor(final ParserContext pCtx, char[] property, final int start,
			final int offset, Object ctx, final Object thisRef, final VariableResolverFactory factory,
			final boolean rootThisRef, final Object value, final Class ingressType) {
		this.rootNode = this.currNode = null;
		this.expr = property;
		this.start = start;
		this.first = true;

		this.length = start + offset;
		this.ctx = ctx;
		this.thisRef = thisRef;
		this.variableFactory = factory;
		this.ingressType = ingressType;

		char[] root = null;

		int split = findLastUnion();

		final PropertyVerifier verifier = new PropertyVerifier(property, this.pCtx = pCtx);

		if (split != -1) {
			root = subset(property, 0, split++);
			// todo: must use the property verifier.
			property = subset(property, split, property.length - split);
		}

		if (root != null) {
			this.length = end = (this.expr = root).length;

			compileGetChain();
			ctx = this.val;
		}

		if (ctx == null) {
			throw new PropertyAccessException("could not access property: "
					+ new String(property, this.start, Math.min(length, property.length))
					+ "; parent is null: " + new String(expr), expr, this.start, pCtx);
		}

		try {
			this.length = end = (this.expr = property).length;
			int st;
			this.cursor = st = 0;

			skipWhitespace();

			if (collection) {
				st = cursor;

				if (cursor == end) {
					throw new PropertyAccessException("unterminated '['", expr, this.start, pCtx);
				}

				if (scanTo(']')) {
					throw new PropertyAccessException("unterminated '['", expr, this.start, pCtx);
				}

				final String ex = new String(property, st, cursor - st);

				if (ctx instanceof Map) {
					if (MVEL.COMPILER_OPT_ALLOW_OVERRIDE_ALL_PROPHANDLING
							&& hasPropertyHandler(Map.class)) {
						propHandlerSet(ex, ctx, Map.class, value);
					} else {
						// noinspection unchecked
						((Map) ctx).put(eval(ex, ctx, variableFactory),
								convert(value, returnType = verifier.analyze()));

						addAccessorNode(new MapAccessorNest(ex, returnType));
					}

					return rootNode;
				} else if (ctx instanceof List) {
					if (MVEL.COMPILER_OPT_ALLOW_OVERRIDE_ALL_PROPHANDLING
							&& hasPropertyHandler(List.class)) {
						propHandlerSet(ex, ctx, List.class, value);
					} else {
						// noinspection unchecked
						((List) ctx).set(eval(ex, ctx, variableFactory, Integer.class),
								convert(value, returnType = verifier.analyze()));

						addAccessorNode(new ListAccessorNest(ex, returnType));
					}

					return rootNode;
				} else if (MVEL.COMPILER_OPT_ALLOW_OVERRIDE_ALL_PROPHANDLING
						&& hasPropertyHandler(ctx.getClass())) {
					propHandlerSet(ex, ctx, ctx.getClass(), value);
					return rootNode;
				} else if (ctx.getClass().isArray()) {
					if (MVEL.COMPILER_OPT_ALLOW_OVERRIDE_ALL_PROPHANDLING
							&& hasPropertyHandler(Array.class)) {
						propHandlerSet(ex, ctx, Array.class, value);
					} else {
						// noinspection unchecked
						Array.set(ctx, eval(ex, ctx, variableFactory, Integer.class),
								convert(value, getBaseComponentType(ctx.getClass())));
						addAccessorNode(new ArrayAccessorNest(ex));
					}
					return rootNode;
				} else {
					throw new PropertyAccessException("cannot bind to collection property: "
							+ new String(property) + ": not a recognized collection type: "
							+ ctx.getClass(), expr, this.st, pCtx);
				}
			} else if (MVEL.COMPILER_OPT_ALLOW_OVERRIDE_ALL_PROPHANDLING
					&& hasPropertyHandler(ctx.getClass())) {
				propHandlerSet(new String(property), ctx, ctx.getClass(), value);
				return rootNode;
			}

			final String tk = new String(property, 0, length).trim();

			if (hasSetListeners()) {
				notifySetListeners(ctx, tk, variableFactory, value);
				addAccessorNode(new Notify(tk));
			}

			final Member member = getFieldOrWriteAccessor(ctx.getClass(), tk, value == null ? null
					: ingressType);

			if (member instanceof Field) {
				final Field fld = (Field) member;

				if (value != null && !fld.getType().isAssignableFrom(value.getClass())) {
					if (!canConvert(fld.getType(), value.getClass())) {
						throw new CompileException("cannot convert type: " + value.getClass() + ": to "
								+ fld.getType(), this.expr, this.start);
					}

					fld.set(ctx, convert(value, fld.getType()));
					addAccessorNode(new DynamicFieldAccessor(fld));
				} else if (value == null && fld.getType().isPrimitive()) {
					fld.set(ctx, PropertyTools.getPrimitiveInitialValue(fld.getType()));
					addAccessorNode(new FieldAccessor(fld));
				} else {
					fld.set(ctx, value);
					addAccessorNode(new FieldAccessor(fld));
				}
			} else if (member != null) {
				final Method meth = (Method) member;

				if (value != null && !meth.getParameterTypes()[0].isAssignableFrom(value.getClass())) {
					if (!canConvert(meth.getParameterTypes()[0], value.getClass())) {
						throw new CompileException("cannot convert type: " + value.getClass() + ": to "
								+ meth.getParameterTypes()[0], this.expr, this.start);
					}

					meth.invoke(ctx, convert(value, meth.getParameterTypes()[0]));
				} else if (value == null && meth.getParameterTypes()[0].isPrimitive()) {
					meth.invoke(ctx, PropertyTools.getPrimitiveInitialValue(meth.getParameterTypes()[0]));
				} else {
					meth.invoke(ctx, value);
				}

				addAccessorNode(new SetterAccessor(meth));
			} else if (ctx instanceof Map) {
				// noinspection unchecked
				((Map) ctx).put(tk, value);

				addAccessorNode(new MapAccessor(tk));
			} else {
				throw new PropertyAccessException("could not access property (" + tk + ") in: "
						+ ingressType.getName(), this.expr, this.start, pCtx);
			}
		} catch (final InvocationTargetException e) {
			throw new PropertyAccessException("could not access property: " + new String(property),
					this.expr, st, e, pCtx);
		} catch (final IllegalAccessException e) {
			throw new PropertyAccessException("could not access property: " + new String(property),
					this.expr, st, e, pCtx);
		} catch (final IllegalArgumentException e) {
			throw new PropertyAccessException("error binding property: " + new String(property)
					+ " (value <<" + value + ">>::"
					+ (value == null ? "null" : value.getClass().getCanonicalName()) + ")", this.expr,
					st, e, pCtx);
		}

		return rootNode;
	}

	private Accessor compileGetChain() {
		Object curr = ctx;
		cursor = start;

		try {
			if (!MVEL.COMPILER_OPT_ALLOW_OVERRIDE_ALL_PROPHANDLING) {
				while (cursor < end) {
					switch (nextSubToken()) {
					case BEAN:
						curr = getBeanProperty(curr, capture());
						break;
					case METH:
						curr = getMethod(curr, capture());
						break;
					case COL:
						curr = getCollectionProperty(curr, capture());
						break;
					case WITH:
						curr = getWithProperty(curr);
						break;
					case DONE:
						break;
					}

					first = false;
					if (curr != null) {
						returnType = curr.getClass();
					}
					if (cursor < end) {
						if (nullSafe) {
							final int os = expr[cursor] == '.' ? 1 : 0;
							addAccessorNode(new NullSafe(expr, cursor + os, length - cursor - os, pCtx));
							if (curr == null) {
								break;
							}
						}
						if (curr == null) {
							throw new NullPointerException();
						}
					}
					staticAccess = false;
				}

			} else {
				while (cursor < end) {
					switch (nextSubToken()) {
					case BEAN:
						curr = getBeanPropertyAO(curr, capture());
						break;
					case METH:
						curr = getMethod(curr, capture());
						break;
					case COL:
						curr = getCollectionPropertyAO(curr, capture());
						break;
					case WITH:
						curr = getWithProperty(curr);
						break;
					case DONE:
						break;
					}

					first = false;
					if (curr != null) {
						returnType = curr.getClass();
					}
					if (cursor < end) {
						if (nullSafe) {
							final int os = expr[cursor] == '.' ? 1 : 0;
							addAccessorNode(new NullSafe(expr, cursor + os, length - cursor - os, pCtx));
							if (curr == null) {
								break;
							}
						}
						if (curr == null) {
							throw new NullPointerException();
						}
					}
					staticAccess = false;
				}
			}

			val = curr;
			return rootNode;
		} catch (final InvocationTargetException e) {
			if (MVEL.INVOKED_METHOD_EXCEPTIONS_BUBBLE) {
				if (e.getTargetException() instanceof RuntimeException) {
					throw (RuntimeException) e.getTargetException();
				} else {
					throw new RuntimeException(e);
				}
			}

			throw new PropertyAccessException(new String(expr, start, length) + ": "
					+ e.getTargetException().getMessage(), this.expr, this.st, e, pCtx);
		} catch (final IllegalAccessException e) {
			throw new PropertyAccessException(new String(expr, start, length) + ": " + e.getMessage(),
					this.expr, this.st, e, pCtx);
		} catch (final IndexOutOfBoundsException e) {
			throw new PropertyAccessException(new String(expr, start, length)
					+ ": array index out of bounds.", this.expr, this.st, e, pCtx);
		} catch (final CompileException e) {
			throw e;
		} catch (final NullPointerException e) {
			throw new PropertyAccessException("null pointer: " + new String(expr, start, length),
					this.expr, this.st, e, pCtx);
		} catch (final Exception e) {
			e.printStackTrace();
			throw new CompileException(e.getMessage(), this.expr, st, e);
		}
	}

	private void addAccessorNode(final AccessorNode an) {
		if (rootNode == null) {
			rootNode = currNode = an;
		} else {
			currNode = currNode.setNextNode(an);
		}
	}

	private Object getWithProperty(final Object ctx) {
		currType = null;
		final String root = start == cursor ? null : new String(expr, start, cursor - 1).trim();

		final int st = cursor + 1;
		cursor = balancedCaptureWithLineAccounting(expr, cursor, end, '{', pCtx);

		final WithAccessor wa = new WithAccessor(pCtx, root, expr, st, cursor++ - st, ingressType);
		addAccessorNode(wa);
		return wa.getValue(ctx, thisRef, variableFactory);
	}

	private Object getBeanPropertyAO(final Object ctx, final String property) throws Exception {

		if (GlobalListenerFactory.hasGetListeners()) {
			notifyGetListeners(ctx, property, variableFactory);
			addAccessorNode(new Notify(property));
		}

		if (ctx != null && hasPropertyHandler(ctx.getClass())) {
			return propHandler(property, ctx, ctx.getClass());
		}

		return getBeanProperty(ctx, property);
	}

	private Object getBeanProperty(Object ctx, final String property) throws Exception {
		if ((pCtx == null ? currType : pCtx.getVarOrInputTypeOrNull(property)) == Object.class
				&& !pCtx.isStrongTyping()) {
			currType = null;
		}

		if (first) {
			if ("this".equals(property)) {
				addAccessorNode(new ThisValueAccessor());
				return this.thisRef;
			} else if (variableFactory != null && variableFactory.isResolveable(property)) {

				if (variableFactory.isIndexedFactory() && variableFactory.isTarget(property)) {
					int idx;
					addAccessorNode(new IndexedVariableAccessor(
							idx = variableFactory.variableIndexOf(property)));

					final VariableResolver vr = variableFactory.getIndexedVariableResolver(idx);
					if (vr == null) {
						variableFactory.setIndexedVariableResolver(idx,
								variableFactory.getVariableResolver(property));
					}

					return variableFactory.getIndexedVariableResolver(idx).getValue();
				} else {
					addAccessorNode(new VariableAccessor(property));

					return variableFactory.getVariableResolver(property).getValue();
				}
			}
		}

		boolean classRef = false;

		Class<?> cls;
		if (ctx instanceof Class) {
			if (MVEL.COMPILER_OPT_SUPPORT_JAVA_STYLE_CLASS_LITERALS && "class".equals(property)) {
				return ctx;
			}

			cls = (Class<?>) ctx;

			classRef = true;
		} else if (ctx != null) {
			cls = ctx.getClass();
		} else {
			cls = currType;
		}

		if (hasPropertyHandler(cls)) {
			final PropertyHandlerAccessor acc = new PropertyHandlerAccessor(property, cls,
					getPropertyHandler(cls));
			addAccessorNode(acc);
			return acc.getValue(ctx, thisRef, variableFactory);
		}

		Member member = cls != null ? getFieldOrAccessor(cls, property) : null;

		if (member != null && classRef && (member.getModifiers() & Modifier.STATIC) == 0) {
			member = null;
		}

		Object o;

		if (member instanceof Method) {
			try {
				o = ctx != null ? ((Method) member).invoke(ctx, EMPTYARG) : null;

				if (hasNullPropertyHandler()) {
					addAccessorNode(new GetterAccessorNH((Method) member, getNullPropertyHandler()));
					if (o == null) {
						o = getNullPropertyHandler().getProperty(member.getName(), ctx, variableFactory);
					}
				} else {
					addAccessorNode(new GetterAccessor((Method) member));
				}
			} catch (final IllegalAccessException e) {
				final Method iFaceMeth = determineActualTargetMethod((Method) member);

				if (iFaceMeth == null) {
					throw new PropertyAccessException("could not access field: " + cls.getName() + "."
							+ property, this.expr, this.start, pCtx);
				}

				o = iFaceMeth.invoke(ctx, EMPTYARG);

				if (hasNullPropertyHandler()) {
					addAccessorNode(new GetterAccessorNH((Method) member, getNullMethodHandler()));
					if (o == null) {
						o = getNullMethodHandler().getProperty(member.getName(), ctx, variableFactory);
					}
				} else {
					addAccessorNode(new GetterAccessor(iFaceMeth));
				}
			} catch (final IllegalArgumentException e) {
				if (member.getDeclaringClass().equals(ctx)) {
					try {
						final Class c = Class.forName(member.getDeclaringClass().getName() + "$"
								+ property);

						throw new CompileException("name collision between innerclass: "
								+ c.getCanonicalName() + "; and bean accessor: " + property + " ("
								+ member.toString() + ")", expr, tkStart);
					} catch (final ClassNotFoundException e2) {
						// fallthru
					}
				}
				throw e;
			}
			currType = toNonPrimitiveType(((Method) member).getReturnType());
			return o;
		} else if (member != null) {
			final Field f = (Field) member;

			if ((f.getModifiers() & Modifier.STATIC) != 0) {
				o = f.get(null);

				if (hasNullPropertyHandler()) {
					addAccessorNode(new StaticVarAccessorNH((Field) member, getNullMethodHandler()));
					if (o == null) {
						o = getNullMethodHandler().getProperty(member.getName(), ctx, variableFactory);
					}
				} else {
					addAccessorNode(new StaticVarAccessor((Field) member));
				}
			} else {
				o = ctx != null ? f.get(ctx) : null;
				if (hasNullPropertyHandler()) {
					addAccessorNode(new FieldAccessorNH((Field) member, getNullMethodHandler()));
					if (o == null) {
						o = getNullMethodHandler().getProperty(member.getName(), ctx, variableFactory);
					}
				} else {
					addAccessorNode(new FieldAccessor((Field) member));
				}
			}
			currType = toNonPrimitiveType(f.getType());
			return o;
		} else if (ctx instanceof Map && (((Map) ctx).containsKey(property) || nullSafe)) {
			addAccessorNode(new MapAccessor(property));
			return ((Map) ctx).get(property);
		} else if (ctx != null && "length".equals(property) && ctx.getClass().isArray()) {
			addAccessorNode(new ArrayLength());
			return getLength(ctx);
		} else if (LITERALS.containsKey(property)) {
			addAccessorNode(new StaticReferenceAccessor(ctx = LITERALS.get(property)));
			return ctx;
		} else {
			final Object tryStaticMethodRef = tryStaticAccess();
			staticAccess = true;
			if (tryStaticMethodRef != null) {
				if (tryStaticMethodRef instanceof Class) {
					addAccessorNode(new StaticReferenceAccessor(tryStaticMethodRef));
					return tryStaticMethodRef;
				} else if (tryStaticMethodRef instanceof Field) {
					addAccessorNode(new StaticVarAccessor((Field) tryStaticMethodRef));
					return ((Field) tryStaticMethodRef).get(null);
				} else {
					addAccessorNode(new StaticReferenceAccessor(tryStaticMethodRef));
					return tryStaticMethodRef;
				}
			} else if (ctx instanceof Class) {
				final Class c = (Class) ctx;
				for (final Method m : c.getMethods()) {
					if (property.equals(m.getName())) {
						if (pCtx != null && pCtx.getParserConfiguration() != null ? pCtx
								.getParserConfiguration().isAllowNakedMethCall()
								: MVEL.COMPILER_OPT_ALLOW_NAKED_METH_CALL) {
							o = m.invoke(null, EMPTY_OBJ_ARR);
							if (hasNullMethodHandler()) {
								addAccessorNode(new MethodAccessorNH(m, new ExecutableStatement[0],
										getNullMethodHandler()));
								if (o == null) {
									o = getNullMethodHandler()
											.getProperty(m.getName(), ctx, variableFactory);
								}
							} else {
								addAccessorNode(new MethodAccessor(m, new ExecutableStatement[0]));
							}
							return o;
						} else {
							addAccessorNode(new StaticReferenceAccessor(m));
							return m;
						}
					}
				}

				try {
					final Class subClass = findClass(variableFactory, c.getName() + "$" + property, pCtx);
					addAccessorNode(new StaticReferenceAccessor(subClass));
					return subClass;
				} catch (final ClassNotFoundException cnfe) {
					// fall through.
				}
			} else if (pCtx != null && pCtx.getParserConfiguration() != null ? pCtx
					.getParserConfiguration().isAllowNakedMethCall()
					: MVEL.COMPILER_OPT_ALLOW_NAKED_METH_CALL) {
				return getMethod(ctx, property);
			}

			// if it is not already using this as context try to read the property
			// value from this
			if (ctx != this.thisRef && this.thisRef != null) {
				addAccessorNode(new ThisValueAccessor());
				return getBeanProperty(this.thisRef, property);
			}

			if (ctx == null) {
				throw new PropertyAccessException("unresolvable property or identifier: " + property,
						expr, start, pCtx);
			} else {
				throw new PropertyAccessException("could not access: " + property + "; in class: "
						+ ctx.getClass().getName(), expr, start, pCtx);
			}
		}
	}

	/**
	 * Handle accessing a property embedded in a collections, map, or array
	 *
	 * @param ctx
	 *        -
	 * @param prop
	 *        -
	 * @return -
	 * @throws Exception
	 *         -
	 */
	private Object getCollectionProperty(Object ctx, final String prop) throws Exception {
		if (prop.length() > 0) {
			ctx = getBeanProperty(ctx, prop);
		}

		currType = null;
		if (ctx == null) {
			return null;
		}

		final int start = ++cursor;

		skipWhitespace();

		if (cursor == end) {
			throw new CompileException("unterminated '['", this.expr, this.start);
		}

		String item;

		if (scanTo(']')) {
			throw new CompileException("unterminated '['", this.expr, this.start);
		}

		item = new String(expr, start, cursor - start);

		boolean itemSubExpr = true;

		Object idx = null;

		try {
			idx = parseInt(item);
			itemSubExpr = false;
		} catch (final Exception e) {
			// not a number;
		}

		ExecutableStatement itemStmt = null;
		if (itemSubExpr) {
			try {
				idx = (itemStmt = (ExecutableStatement) subCompileExpression(item.toCharArray(), pCtx))
						.getValue(ctx, thisRef, variableFactory);
			} catch (final CompileException e) {
				e.setExpr(this.expr);
				e.setCursor(start);
				throw e;
			}
		}

		++cursor;

		if (ctx instanceof Map) {
			if (itemSubExpr) {
				addAccessorNode(new MapAccessorNest(itemStmt, null));
			} else {
				addAccessorNode(new MapAccessor(parseInt(item)));
			}

			return ((Map) ctx).get(idx);
		} else if (ctx instanceof List) {
			if (itemSubExpr) {
				addAccessorNode(new ListAccessorNest(itemStmt, null));
			} else {
				addAccessorNode(new ListAccessor(parseInt(item)));
			}

			return ((List) ctx).get((Integer) idx);
		} else if (ctx.getClass().isArray()) {
			if (itemSubExpr) {
				addAccessorNode(new ArrayAccessorNest(itemStmt));
			} else {
				addAccessorNode(new ArrayAccessor(parseInt(item)));
			}

			return Array.get(ctx, (Integer) idx);
		} else if (ctx instanceof CharSequence) {
			if (itemSubExpr) {
				addAccessorNode(new IndexedCharSeqAccessorNest(itemStmt));
			} else {
				addAccessorNode(new IndexedCharSeqAccessor(parseInt(item)));
			}

			return ((CharSequence) ctx).charAt((Integer) idx);
		} else {
			final TypeDescriptor tDescr = new TypeDescriptor(expr, this.start, length, 0);
			if (tDescr.isArray()) {
				final Class cls = getClassReference((Class) ctx, tDescr, variableFactory, pCtx);
				rootNode = new StaticReferenceAccessor(cls);
				return cls;
			}

			throw new CompileException("illegal use of []: unknown type: " + ctx.getClass().getName(),
					this.expr, this.start);
		}
	}

	private Object getCollectionPropertyAO(Object ctx, final String prop) throws Exception {
		if (prop.length() > 0) {
			ctx = getBeanPropertyAO(ctx, prop);
		}

		currType = null;
		if (ctx == null) {
			return null;
		}

		final int _start = ++cursor;

		skipWhitespace();

		if (cursor == end) {
			throw new CompileException("unterminated '['", this.expr, this.start);
		}

		String item;

		if (scanTo(']')) {
			throw new CompileException("unterminated '['", this.expr, this.start);
		}

		item = new String(expr, _start, cursor - _start);

		boolean itemSubExpr = true;

		Object idx = null;

		try {
			idx = parseInt(item);
			itemSubExpr = false;
		} catch (final Exception e) {
			// not a number;
		}

		ExecutableStatement itemStmt = null;
		if (itemSubExpr) {
			idx = (itemStmt = (ExecutableStatement) subCompileExpression(item.toCharArray(), pCtx))
					.getValue(ctx, thisRef, variableFactory);
		}

		++cursor;

		if (ctx instanceof Map) {
			if (hasPropertyHandler(Map.class)) {
				return propHandler(item, ctx, Map.class);
			} else {
				if (itemSubExpr) {
					addAccessorNode(new MapAccessorNest(itemStmt, null));
				} else {
					addAccessorNode(new MapAccessor(parseInt(item)));
				}

				return ((Map) ctx).get(idx);
			}
		} else if (ctx instanceof List) {
			if (hasPropertyHandler(List.class)) {
				return propHandler(item, ctx, List.class);
			} else {
				if (itemSubExpr) {
					addAccessorNode(new ListAccessorNest(itemStmt, null));
				} else {
					addAccessorNode(new ListAccessor(parseInt(item)));
				}

				return ((List) ctx).get((Integer) idx);
			}
		} else if (ctx.getClass().isArray()) {
			if (hasPropertyHandler(Array.class)) {
				return propHandler(item, ctx, Array.class);
			} else {
				if (itemSubExpr) {
					addAccessorNode(new ArrayAccessorNest(itemStmt));
				} else {
					addAccessorNode(new ArrayAccessor(parseInt(item)));
				}

				return Array.get(ctx, (Integer) idx);
			}
		} else if (ctx instanceof CharSequence) {
			if (hasPropertyHandler(CharSequence.class)) {
				return propHandler(item, ctx, CharSequence.class);
			} else {
				if (itemSubExpr) {
					addAccessorNode(new IndexedCharSeqAccessorNest(itemStmt));
				} else {
					addAccessorNode(new IndexedCharSeqAccessor(parseInt(item)));
				}

				return ((CharSequence) ctx).charAt((Integer) idx);
			}
		} else {
			final TypeDescriptor tDescr = new TypeDescriptor(expr, this.start, end - this.start, 0);
			if (tDescr.isArray()) {
				final Class cls = getClassReference((Class) ctx, tDescr, variableFactory, pCtx);
				rootNode = new StaticReferenceAccessor(cls);
				return cls;
			}

			throw new CompileException("illegal use of []: unknown type: " + ctx.getClass().getName(),
					this.expr, this.st);
		}
	}

	/**
	 * Find an appropriate method, execute it, and return it's response.
	 *
	 * @param ctx
	 *        -
	 * @param name
	 *        -
	 * @return -
	 * @throws Exception
	 *         -
	 */
	@SuppressWarnings({ "unchecked" })
	private Object getMethod(final Object ctx, final String name) throws Exception {
		final int st = cursor;
		final String tk = cursor != end && expr[cursor] == '('
				&& ((cursor = balancedCapture(expr, cursor, '(')) - st) > 1 ? new String(expr, st + 1,
				cursor - st - 1) : "";
		cursor++;

		Object[] args;
		Class[] argTypes;
		ExecutableStatement[] es;

		if (tk.length() == 0) {
			args = ParseTools.EMPTY_OBJ_ARR;
			argTypes = ParseTools.EMPTY_CLS_ARR;
			es = null;
		} else {
			final List<char[]> subtokens = parseParameterList(tk.toCharArray(), 0, -1);
			es = new ExecutableStatement[subtokens.size()];
			args = new Object[subtokens.size()];
			argTypes = new Class[subtokens.size()];

			for (int i = 0; i < subtokens.size(); i++) {
				try {
					args[i] = (es[i] = (ExecutableStatement) subCompileExpression(subtokens.get(i), pCtx))
							.getValue(this.thisRef, thisRef, variableFactory);
				} catch (final CompileException e) {
					throw ErrorUtil.rewriteIfNeeded(e, this.expr, this.start);
				}

				if (es[i].isExplicitCast()) {
					argTypes[i] = es[i].getKnownEgressType();
				}
			}

			if (pCtx.isStrictTypeEnforcement()) {
				for (int i = 0; i < args.length; i++) {
					argTypes[i] = es[i].getKnownEgressType();
					if (es[i] instanceof ExecutableLiteral
							&& ((ExecutableLiteral) es[i]).getLiteral() == null) {
						argTypes[i] = NullType.class;
					}
				}
			} else {
				for (int i = 0; i < args.length; i++) {
					if (argTypes[i] != null) {
						continue;
					}

					if (es[i].getKnownEgressType() == Object.class) {
						argTypes[i] = args[i] == null ? null : args[i].getClass();
					} else {
						argTypes[i] = es[i].getKnownEgressType();
					}
				}
			}
		}

		return getMethod(ctx, name, args, argTypes, es);
	}

	@SuppressWarnings({ "unchecked" })
	private Object getMethod(Object ctx, String name, final Object[] args, final Class[] argTypes,
			final ExecutableStatement[] es) throws Exception {
		if (first && variableFactory != null && variableFactory.isResolveable(name)) {
			final Object ptr = variableFactory.getVariableResolver(name).getValue();
			if (ptr instanceof Method) {
				ctx = ((Method) ptr).getDeclaringClass();
				name = ((Method) ptr).getName();
			} else if (ptr instanceof MethodStub) {
				ctx = ((MethodStub) ptr).getClassReference();
				name = ((MethodStub) ptr).getMethodName();
			} else if (ptr instanceof FunctionInstance) {
				final FunctionInstance func = (FunctionInstance) ptr;
				if (!name.equals(func.getFunction().getName())) {
					getBeanProperty(ctx, name);
					addAccessorNode(new DynamicFunctionAccessor(es));
				} else {
					addAccessorNode(new FunctionAccessor(func, es));
				}
				return func.call(ctx, thisRef, variableFactory, args);
			} else {
				throw new OptimizationFailure(
						"attempt to optimize a method call for a reference that does not point to a method: "
								+ name + " (reference is type: "
								+ (ctx != null ? ctx.getClass().getName() : null) + ")");
			}

			first = false;
		}

		if (ctx == null && currType == null) {
			throw new PropertyAccessException("null pointer or function not found: " + name,
					this.expr, this.start, pCtx);
		}

		boolean classTarget = false;
		Class<?> cls = currType != null ? currType
				: ((classTarget = ctx instanceof Class) ? (Class<?>) ctx : ctx.getClass());
		currType = null;

		Method m;
		Class[] parameterTypes = null;

		/**
		 * If we have not cached the method then we need to go ahead and try to
		 * resolve it.
		 */

		/**
		 * Try to find an instance method from the class target.
		 */
		if ((m = getBestCandidate(argTypes, name, cls, cls.getMethods(), false, classTarget)) != null) {
			parameterTypes = m.getParameterTypes();
		}

		if (m == null && classTarget) {
			/**
			 * If we didn't find anything, maybe we're looking for the actual
			 * java.lang.Class methods.
			 */
			if ((m = getBestCandidate(argTypes, name, cls, Class.class.getMethods(), false)) != null) {
				parameterTypes = m.getParameterTypes();
			}
		}

		// If we didn't find anything and the declared class is different from the
		// actual one try also with the actual one
		if (m == null && ctx != null && cls != ctx.getClass() && !(ctx instanceof Class)) {
			cls = ctx.getClass();
			if ((m = getBestCandidate(argTypes, name, cls, cls.getMethods(), false, classTarget)) != null) {
				parameterTypes = m.getParameterTypes();
			}
		}

		if (m == null) {
			final StringAppender errorBuild = new StringAppender();
			if ("size".equals(name) && args.length == 0 && cls.isArray()) {
				addAccessorNode(new ArrayLength());
				return getLength(ctx);
			}

			// if it is not already using this as context try to access the method
			// this
			if (ctx != this.thisRef && this.thisRef != null) {
				addAccessorNode(new ThisValueAccessor());
				return getMethod(this.thisRef, name, args, argTypes, es);
			}

			for (int i = 0; i < args.length; i++) {
				errorBuild.append(args[i] != null ? args[i].getClass().getName() : null);
				if (i < args.length - 1) {
					errorBuild.append(", ");
				}
			}

			throw new PropertyAccessException("unable to resolve method: " + cls.getName() + "."
					+ name + "(" + errorBuild.toString() + ") [arglength=" + args.length + "]",
					this.expr, this.st, pCtx);
		}

		if (es != null) {
			ExecutableStatement cExpr;
			for (int i = 0; i < es.length; i++) {
				cExpr = es[i];
				if (cExpr.getKnownIngressType() == null) {
					cExpr.setKnownIngressType(paramTypeVarArgsSafe(parameterTypes, i, m.isVarArgs()));
					cExpr.computeTypeConversionRule();
				}
				if (!cExpr.isConvertableIngressEgress()) {
					args[i] = convert(args[i], paramTypeVarArgsSafe(parameterTypes, i, m.isVarArgs()));
				}
			}
		} else {
			/**
			 * Coerce any types if required.
			 */
			for (int i = 0; i < args.length; i++) {
				args[i] = convert(args[i], paramTypeVarArgsSafe(parameterTypes, i, m.isVarArgs()));
			}
		}

		final Method method = getWidenedTarget(cls, m);
		Object o = ctx != null ? method.invoke(ctx,
				normalizeArgsForVarArgs(parameterTypes, args, m.isVarArgs())) : null;

		if (hasNullMethodHandler()) {
			addAccessorNode(new MethodAccessorNH(method, es, getNullMethodHandler()));
			if (o == null) {
				o = getNullMethodHandler().getProperty(m.getName(), ctx, variableFactory);
			}
		} else {
			addAccessorNode(new MethodAccessor(method, es));
		}

		/**
		 * return the response.
		 */
		currType = toNonPrimitiveType(method.getReturnType());
		return o;
	}

	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) throws Exception {
		return rootNode.getValue(ctx, elCtx, variableFactory);
	}

	private Accessor _getAccessor(final Object o, Class type) {
		if (o instanceof List) {
			final Accessor[] a = new Accessor[((List) o).size()];
			int i = 0;

			for (final Object item : (List) o) {
				a[i++] = _getAccessor(item, type);
			}

			returnType = List.class;

			return new ListCreator(a);
		} else if (o instanceof Map) {
			final Accessor[] k = new Accessor[((Map) o).size()];
			final Accessor[] v = new Accessor[k.length];
			int i = 0;

			for (final Object item : ((Map) o).keySet()) {
				k[i] = _getAccessor(item, type); // key
				v[i++] = _getAccessor(((Map) o).get(item), type); // value
			}

			returnType = Map.class;

			return new MapCreator(k, v);
		} else if (o instanceof Object[]) {
			final Accessor[] a = new Accessor[((Object[]) o).length];
			int i = 0;
			int dim = 0;

			if (type != null) {
				final String nm = type.getName();
				while (nm.charAt(dim) == '[') {
					dim++;
				}
			} else {
				type = Object[].class;
				dim = 1;
			}

			try {
				final Class base = getBaseComponentType(type);
				final Class cls = dim > 1 ? findClass(null,
						repeatChar('[', dim - 1) + "L" + base.getName() + ";", pCtx) : type;

				for (final Object item : (Object[]) o) {
					expectType(pCtx, a[i++] = _getAccessor(item, cls), base, true);
				}

				return new ArrayCreator(a, getSubComponentType(type));
			} catch (final ClassNotFoundException e) {
				throw new RuntimeException("this error should never throw:"
						+ getBaseComponentType(type).getName(), e);
			}
		} else {
			if (returnType == null) {
				returnType = Object.class;
			}
			if (type.isArray()) {
				return new ExprValueAccessor((String) o, type, ctx, variableFactory, pCtx);
			} else {
				return new ExprValueAccessor((String) o, Object.class, ctx, variableFactory, pCtx);
			}
		}
	}

	@Override
	public Accessor optimizeCollection(final ParserContext pCtx, final Object o, final Class type,
			final char[] property, final int start, final int offset, final Object ctx,
			final Object thisRef, final VariableResolverFactory factory) {
		this.start = this.cursor = start;
		this.length = start + offset;
		this.returnType = type;
		this.ctx = ctx;
		this.variableFactory = factory;
		this.pCtx = pCtx;

		final Accessor root = _getAccessor(o, returnType);

		if (property != null && length > start) {
			return new Union(pCtx, root, property, cursor, offset);
		} else {
			return root;
		}
	}

	@Override
	public Accessor optimizeObjectCreation(final ParserContext pCtx, final char[] property,
			final int start, final int offset, final Object ctx, final Object thisRef,
			final VariableResolverFactory factory) {
		this.length = start + offset;
		this.cursor = this.start = start;
		this.pCtx = pCtx;

		try {
			return compileConstructor(property, ctx, factory);
		} catch (final CompileException e) {
			throw ErrorUtil.rewriteIfNeeded(e, property, this.start);
		} catch (final ClassNotFoundException e) {
			throw new CompileException("could not resolve class: " + e.getMessage(), property,
					this.start, e);
		} catch (final Exception e) {
			throw new CompileException("could not create constructor: " + e.getMessage(), property,
					this.start, e);
		}
	}

	private void setRootNode(final AccessorNode rootNode) {
		this.rootNode = this.currNode = rootNode;
	}

	private AccessorNode getRootNode() {
		return rootNode;
	}

	@Override
	public Object getResultOptPass() {
		return val;
	}

	@SuppressWarnings({ "WeakerAccess" })
	private AccessorNode compileConstructor(final char[] expression, final Object ctx,
			final VariableResolverFactory vars) throws InstantiationException, IllegalAccessException,
			InvocationTargetException, ClassNotFoundException, NoSuchMethodException {

		final String[] cnsRes = captureContructorAndResidual(expression, start, length);
		final List<char[]> constructorParms = parseMethodOrConstructor(cnsRes[0].toCharArray());

		if (constructorParms != null) {
			final String s = new String(subset(expression, 0,
					ArrayTools.findFirst('(', start, length, expression)));
			final Class cls = ParseTools.findClass(vars, s, pCtx);

			final ExecutableStatement[] cStmts = new ExecutableStatement[constructorParms.size()];

			for (int i = 0; i < constructorParms.size(); i++) {
				cStmts[i] = (ExecutableStatement) subCompileExpression(constructorParms.get(i), pCtx);
			}

			Object[] parms = new Object[constructorParms.size()];
			for (int i = 0; i < constructorParms.size(); i++) {
				parms[i] = cStmts[i].getValue(ctx, vars);
			}

			final Constructor cns = getBestConstructorCandidate(parms, cls, pCtx.isStrongTyping());

			if (cns == null) {
				final StringBuilder error = new StringBuilder();
				for (int i = 0; i < parms.length; i++) {
					error.append(parms[i].getClass().getName());
					if (i + 1 < parms.length) {
						error.append(", ");
					}
				}

				throw new CompileException("unable to find constructor: " + cls.getName() + "("
						+ error.toString() + ")", this.expr, this.start);
			}
			for (int i = 0; i < parms.length; i++) {
				// noinspection unchecked
				parms[i] = convert(parms[i],
						paramTypeVarArgsSafe(cns.getParameterTypes(), i, cns.isVarArgs()));
			}
			parms = normalizeArgsForVarArgs(cns.getParameterTypes(), parms, cns.isVarArgs());

			AccessorNode ca = new ConstructorAccessor(cns, cStmts);

			if (cnsRes.length > 1) {
				final ReflectiveAccessorOptimizer compiledOptimizer = new ReflectiveAccessorOptimizer(
						pCtx, cnsRes[1].toCharArray(), 0, cnsRes[1].length(), cns.newInstance(parms),
						ctx, vars);
				compiledOptimizer.ingressType = cns.getDeclaringClass();

				compiledOptimizer.setRootNode(ca);
				compiledOptimizer.compileGetChain();
				ca = compiledOptimizer.getRootNode();

				this.val = compiledOptimizer.getResultOptPass();
			}

			return ca;
		} else {
			final ClassLoader classLoader = pCtx != null ? pCtx.getClassLoader() : currentThread()
					.getContextClassLoader();
			final Constructor<?> cns = Class.forName(new String(expression), true, classLoader)
					.getConstructor(EMPTYCLS);
			AccessorNode ca = new ConstructorAccessor(cns, null);

			if (cnsRes.length > 1) {
				// noinspection NullArgumentToVariableArgMethod
				final ReflectiveAccessorOptimizer compiledOptimizer = new ReflectiveAccessorOptimizer(
						pCtx, cnsRes[1].toCharArray(), 0, cnsRes[1].length(), cns.newInstance(null), ctx,
						vars);
				compiledOptimizer.setRootNode(ca);
				compiledOptimizer.compileGetChain();
				ca = compiledOptimizer.getRootNode();

				this.val = compiledOptimizer.getResultOptPass();
			}

			return ca;
		}
	}

	@Override
	public Class getEgressType() {
		return returnType;
	}

	@Override
	public boolean isLiteralOnly() {
		return false;
	}

	private Object propHandler(final String property, final Object ctx, final Class handler) {
		final PropertyHandler ph = getPropertyHandler(handler);
		addAccessorNode(new PropertyHandlerAccessor(property, handler, ph));
		return ph.getProperty(property, ctx, variableFactory);
	}

	public void propHandlerSet(final String property, final Object ctx, final Class handler,
			final Object value) {
		final PropertyHandler ph = getPropertyHandler(handler);
		addAccessorNode(new PropertyHandlerAccessor(property, handler, ph));
		ph.setProperty(property, ctx, variableFactory, value);
	}
}
