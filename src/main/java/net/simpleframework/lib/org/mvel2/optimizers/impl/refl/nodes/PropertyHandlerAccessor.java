package net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes;

import net.simpleframework.lib.org.mvel2.MVEL;
import net.simpleframework.lib.org.mvel2.integration.PropertyHandler;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class PropertyHandlerAccessor extends BaseAccessor {
	private final String propertyName;
	private final PropertyHandler propertyHandler;
	private final Class conversionType;

	public PropertyHandlerAccessor(final String propertyName, final Class conversionType,
			final PropertyHandler propertyHandler) {
		this.propertyName = propertyName;
		this.conversionType = conversionType;
		this.propertyHandler = propertyHandler;
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory) {
		if (!conversionType.isAssignableFrom(ctx.getClass())) {
			if (nextNode != null) {
				return nextNode.getValue(MVEL.getProperty(propertyName, ctx), elCtx, variableFactory);
			} else {
				return MVEL.getProperty(propertyName, ctx);
			}
		}
		try {
			if (nextNode != null) {
				return nextNode.getValue(
						propertyHandler.getProperty(propertyName, ctx, variableFactory), elCtx,
						variableFactory);
			} else {
				return propertyHandler.getProperty(propertyName, ctx, variableFactory);
			}
		} catch (final Exception e) {
			throw new RuntimeException("unable to access field", e);
		}
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		if (nextNode != null) {
			return nextNode.setValue(propertyHandler.getProperty(propertyName, ctx, variableFactory),
					ctx, variableFactory, value);
		} else {
			return propertyHandler.setProperty(propertyName, ctx, variableFactory, value);
		}
	}

	@Override
	public Class getKnownEgressType() {
		return Object.class;
	}
}
