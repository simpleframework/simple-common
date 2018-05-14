package net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes;

import static net.simpleframework.lib.org.mvel2.DataConversion.convert;
import static net.simpleframework.lib.org.mvel2.util.ParseTools.getBestCandidate;

import java.lang.reflect.Method;

import net.simpleframework.lib.org.mvel2.compiler.AccessorNode;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.util.PropertyTools;

public class SetterAccessor implements AccessorNode {
	private AccessorNode nextNode;
	private final Method method;
	private Class<?> targetType;
	private final boolean primitive;

	private boolean coercionRequired = false;

	public static final Object[] EMPTY = new Object[0];

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		try {
			if (coercionRequired) {
				return method.invoke(ctx, convert(value, targetType));
			} else {
				return method.invoke(ctx,
						value == null && primitive ? PropertyTools.getPrimitiveInitialValue(targetType)
								: value);
			}
		} catch (final IllegalArgumentException e) {
			if (ctx != null && method.getDeclaringClass() != ctx.getClass()) {
				final Method o = getBestCandidate(EMPTY, method.getName(), ctx.getClass(),
						ctx.getClass().getMethods(), true);
				if (o != null) {
					return executeOverrideTarget(o, ctx, value);
				}
			}

			if (!coercionRequired) {
				coercionRequired = true;
				return setValue(ctx, elCtx, variableFactory, value);
			}
			throw new RuntimeException("unable to bind property", e);
		} catch (final Exception e) {
			throw new RuntimeException("error calling method: " + method.getDeclaringClass().getName()
					+ "." + method.getName(), e);
		}
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory vars) {
		return null;
	}

	public SetterAccessor(final Method method) {
		this.method = method;
		assert method != null;
		primitive = (this.targetType = method.getParameterTypes()[0]).isPrimitive();
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public AccessorNode setNextNode(final AccessorNode nextNode) {
		return this.nextNode = nextNode;
	}

	@Override
	public AccessorNode getNextNode() {
		return nextNode;
	}

	@Override
	public String toString() {
		return method.getDeclaringClass().getName() + "." + method.getName();
	}

	@Override
	public Class getKnownEgressType() {
		return method.getReturnType();
	}

	private Object executeOverrideTarget(final Method o, final Object ctx, final Object value) {
		try {
			return o.invoke(ctx, convert(value, targetType));
		} catch (final Exception e2) {
			throw new RuntimeException("unable to invoke method", e2);
		}
	}
}
