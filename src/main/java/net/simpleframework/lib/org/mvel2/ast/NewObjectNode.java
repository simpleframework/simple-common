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
package net.simpleframework.lib.org.mvel2.ast;

import static java.lang.reflect.Array.newInstance;
import static net.simpleframework.lib.org.mvel2.DataConversion.convert;
import static net.simpleframework.lib.org.mvel2.MVEL.analyze;
import static net.simpleframework.lib.org.mvel2.MVEL.eval;
import static net.simpleframework.lib.org.mvel2.optimizers.OptimizerFactory.getThreadAccessorOptimizer;
import static net.simpleframework.lib.org.mvel2.util.ArrayTools.findFirst;
import static net.simpleframework.lib.org.mvel2.util.CompilerTools.getInjectedImports;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.captureContructorAndResidual;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.findClass;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.getBaseComponentType;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.getBestConstructorCandidate;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.parseMethodOrConstructor;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.repeatChar;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subArray;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.subset;
import static net.simpleframework.lib.org.mvel2.util.ReflectionUtil.toPrimitiveArrayType;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.ErrorDetail;
import net.simpleframework.lib.org.mvel2.ParserContext;
import net.simpleframework.lib.org.mvel2.PropertyAccessor;
import net.simpleframework.lib.org.mvel2.compiler.Accessor;
import net.simpleframework.lib.org.mvel2.compiler.ExecutableStatement;
import net.simpleframework.lib.org.mvel2.compiler.PropertyVerifier;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.optimizers.AccessorOptimizer;
import net.simpleframework.lib.org.mvel2.optimizers.OptimizerFactory;
import net.simpleframework.lib.org.mvel2.util.ArrayTools;
import net.simpleframework.lib.org.mvel2.util.ErrorUtil;

/**
 * @author Christopher Brock
 */
@SuppressWarnings({ "ManualArrayCopy" })
public class NewObjectNode extends ASTNode {
	private static final Logger LOG = Logger.getLogger(NewObjectNode.class.getName());

	private transient Accessor newObjectOptimizer;
	private TypeDescriptor typeDescr;
	private char[] name;

	private static final Class[] EMPTYCLS = new Class[0];

	public NewObjectNode(final TypeDescriptor typeDescr, final int fields,
			final ParserContext pCtx) {
		super(pCtx);
		this.typeDescr = typeDescr;
		this.fields = fields;
		this.expr = typeDescr.getExpr();
		this.start = typeDescr.getStart();
		this.offset = typeDescr.getOffset();

		if (offset < expr.length) {
			this.name = subArray(expr, start, start + offset);
		} else {
			this.name = expr;
		}

		if ((fields & COMPILE_IMMEDIATE) != 0) {
			if (pCtx != null && pCtx.hasImport(typeDescr.getClassName())) {
				pCtx.setAllowBootstrapBypass(false);
				egressType = pCtx.getImport(typeDescr.getClassName());
			} else {
				try {
					egressType = Class.forName(typeDescr.getClassName(), true, getClassLoader());
				} catch (final ClassNotFoundException e) {
					if (pCtx.isStrongTyping()) {
						pCtx.addError(new ErrorDetail(expr, start, true,
								"could not resolve class: " + typeDescr.getClassName()));
					}
					return;
					// do nothing.
				}
			}

			if (egressType != null) {
				rewriteClassReferenceToFQCN(fields);
				if (typeDescr.isArray()) {
					try {
						egressType = egressType.isPrimitive() ? toPrimitiveArrayType(egressType)
								: findClass(null, repeatChar('[', typeDescr.getArrayLength()) + "L"
										+ egressType.getName() + ";", pCtx);
					} catch (final Exception e) {
						LOG.log(Level.WARNING, "", e);
						// for now, don't handle this.
					}
				}
			}

			if (pCtx != null) {
				if (egressType == null) {
					pCtx.addError(new ErrorDetail(expr, start, true,
							"could not resolve class: " + typeDescr.getClassName()));
					return;
				}

				if (!typeDescr.isArray()) {
					final String[] cnsResid = captureContructorAndResidual(expr, start, offset);

					final List<char[]> constructorParms = parseMethodOrConstructor(
							cnsResid[0].toCharArray());

					final Class[] parms = new Class[constructorParms.size()];
					for (int i = 0; i < parms.length; i++) {
						parms[i] = analyze(constructorParms.get(i), pCtx);
					}

					if (getBestConstructorCandidate(parms, egressType, true) == null) {
						if (pCtx.isStrongTyping()) {
							pCtx.addError(new ErrorDetail(expr, start, pCtx.isStrongTyping(),
									"could not resolve constructor " + typeDescr.getClassName()
											+ Arrays.toString(parms)));
						}
					}

					if (cnsResid.length == 2) {
						final String residualProperty = cnsResid[1].trim();

						if (residualProperty.length() == 0) {
							return;
						}

						this.egressType = new PropertyVerifier(residualProperty, pCtx, egressType)
								.analyze();
					}
				}
			}
		}
	}

	private void rewriteClassReferenceToFQCN(final int fields) {
		final String FQCN = egressType.getName();

		if (typeDescr.getClassName().indexOf('.') == -1) {
			int idx = ArrayTools.findFirst('(', 0, name.length, name);

			final char[] fqcn = FQCN.toCharArray();

			if (idx == -1) {
				this.name = new char[idx = fqcn.length];
				for (int i = 0; i < idx; i++) {
					this.name[i] = fqcn[i];
				}
			} else {
				final char[] newName = new char[fqcn.length + (name.length - idx)];

				for (int i = 0; i < fqcn.length; i++) {
					newName[i] = fqcn[i];
				}

				final int i0 = name.length - idx;
				final int i1 = fqcn.length;
				for (int i = 0; i < i0; i++) {
					newName[i + i1] = name[i + idx];
				}

				this.name = newName;
			}

			this.typeDescr.updateClassName(name, 0, name.length, fields);
		}
	}

	@Override
	public Object getReducedValueAccelerated(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		if (newObjectOptimizer == null) {
			if (egressType == null) {
				/**
				 * This means we couldn't resolve the type at the time this AST node
				 * was created, which means
				 * we have to attempt runtime resolution.
				 */

				if (factory != null && factory.isResolveable(typeDescr.getClassName())) {
					try {
						egressType = (Class) factory.getVariableResolver(typeDescr.getClassName())
								.getValue();
						rewriteClassReferenceToFQCN(COMPILE_IMMEDIATE);

						if (typeDescr.isArray()) {
							try {
								egressType = findClass(factory, repeatChar('[', typeDescr.getArrayLength())
										+ "L" + egressType.getName() + ";", pCtx);
							} catch (final Exception e) {
								// for now, don't handle this.
							}
						}

					} catch (final ClassCastException e) {
						throw new CompileException("cannot construct object: " + typeDescr.getClassName()
								+ " is not a class reference", expr, start, e);
					}
				}
			}

			if (typeDescr.isArray()) {
				return (newObjectOptimizer = new NewObjectArray(
						getBaseComponentType(egressType.getComponentType()),
						typeDescr.getCompiledArraySize())).getValue(ctx, thisValue, factory);
			}

			try {
				final AccessorOptimizer optimizer = getThreadAccessorOptimizer();

				ParserContext pCtx = this.pCtx;
				if (pCtx == null) {
					pCtx = new ParserContext();
					pCtx.getParserConfiguration().setAllImports(getInjectedImports(factory));
				}

				newObjectOptimizer = optimizer.optimizeObjectCreation(pCtx, name, 0, name.length, ctx,
						thisValue, factory);

				/**
				 * Check to see if the optimizer actually produced the object during
				 * optimization. If so,
				 * we return that value now.
				 */
				if (optimizer.getResultOptPass() != null) {
					egressType = optimizer.getEgressType();
					return optimizer.getResultOptPass();
				}
			} catch (final CompileException e) {
				throw ErrorUtil.rewriteIfNeeded(e, expr, start);
			} finally {
				OptimizerFactory.clearThreadAccessorOptimizer();
			}
		}

		return newObjectOptimizer.getValue(ctx, thisValue, factory);
	}

	@Override
	public Object getReducedValue(final Object ctx, final Object thisValue,
			final VariableResolverFactory factory) {
		try {
			if (typeDescr.isArray()) {
				final Class cls = findClass(factory, typeDescr.getClassName(), pCtx);

				final int[] s = new int[typeDescr.getArrayLength()];
				final ArraySize[] arraySize = typeDescr.getArraySize();

				for (int i = 0; i < s.length; i++) {
					s[i] = convert(eval(arraySize[i].value, ctx, factory), Integer.class);
				}

				return newInstance(cls, s);
			} else {
				final String[] cnsRes = captureContructorAndResidual(name, 0, name.length);
				final List<char[]> constructorParms = parseMethodOrConstructor(cnsRes[0].toCharArray());

				if (constructorParms != null) {
					final Class cls = findClass(factory,
							new String(subset(name, 0, findFirst('(', 0, name.length, name))).trim(),
							pCtx);

					final Object[] parms = new Object[constructorParms.size()];
					for (int i = 0; i < constructorParms.size(); i++) {
						parms[i] = eval(constructorParms.get(i), ctx, factory);
					}

					final Constructor cns = getBestConstructorCandidate(parms, cls, false);

					if (cns == null) {
						throw new CompileException("unable to find constructor for: " + cls.getName(),
								expr, start);
					}

					for (int i = 0; i < parms.length; i++) {
						// noinspection unchecked
						parms[i] = convert(parms[i], cns.getParameterTypes()[i]);
					}

					if (cnsRes.length > 1) {
						return PropertyAccessor.get(cnsRes[1], cns.newInstance(parms), factory, thisValue,
								pCtx);
					} else {
						return cns.newInstance(parms);
					}
				} else {
					final Constructor<?> cns = Class.forName(typeDescr.getClassName(), true,
							pCtx.getParserConfiguration().getClassLoader()).getConstructor(EMPTYCLS);

					if (cnsRes.length > 1) {
						return PropertyAccessor.get(cnsRes[1], cns.newInstance(), factory, thisValue,
								pCtx);
					} else {
						return cns.newInstance();
					}
				}
			}
		} catch (final CompileException e) {
			throw e;
		} catch (final ClassNotFoundException e) {
			throw new CompileException("unable to resolve class: " + e.getMessage(), expr, start, e);
		} catch (final NoSuchMethodException e) {
			throw new CompileException("cannot resolve constructor: " + e.getMessage(), expr, start,
					e);
		} catch (final Exception e) {
			throw new CompileException("could not instantiate class: " + e.getMessage(), expr, start,
					e);
		}
	}

	private boolean isPrototypeFunction() {
		return pCtx.getFunctions().containsKey(typeDescr.getClassName());
	}

	private Object createPrototypalObject(final Object ctx, final Object thisRef,
			final VariableResolverFactory factory) {
		final Function function = pCtx.getFunction(typeDescr.getClassName());
		return function.getReducedValueAccelerated(ctx, thisRef, factory);
	}

	public static class NewObjectArray implements Accessor, Serializable {
		private final ExecutableStatement[] sizes;
		private final Class arrayType;

		public NewObjectArray(final Class arrayType, final ExecutableStatement[] sizes) {
			this.arrayType = arrayType;
			this.sizes = sizes;
		}

		@Override
		public Object getValue(final Object ctx, final Object elCtx,
				final VariableResolverFactory variableFactory) {
			final int[] s = new int[sizes.length];
			for (int i = 0; i < s.length; i++) {
				s[i] = convert(sizes[i].getValue(ctx, elCtx, variableFactory), Integer.class);
			}

			return newInstance(arrayType, s);
		}

		@Override
		public Object setValue(final Object ctx, final Object elCtx,
				final VariableResolverFactory variableFactory, final Object value) {
			return null;
		}

		@Override
		public Class getKnownEgressType() {
			try {
				return Class.forName("[L" + arrayType.getName() + ";");
			} catch (final ClassNotFoundException cne) {
				return null;
			}
		}
	}

	public TypeDescriptor getTypeDescr() {
		return typeDescr;
	}

	public Accessor getNewObjectOptimizer() {
		return newObjectOptimizer;
	}
}
