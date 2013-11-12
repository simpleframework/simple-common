package net.simpleframework.lib.org.mvel2.optimizers.impl.refl.nodes;

import net.simpleframework.lib.org.mvel2.compiler.AccessorNode;
import net.simpleframework.lib.org.mvel2.integration.GlobalListenerFactory;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;

public class Notify implements AccessorNode {
	private final String name;
	private AccessorNode nextNode;

	public Notify(final String name) {
		this.name = name;
	}

	@Override
	public Object getValue(final Object ctx, final Object elCtx, final VariableResolverFactory vrf) {
		GlobalListenerFactory.notifyGetListeners(ctx, name, vrf);
		return nextNode.getValue(ctx, elCtx, vrf);
	}

	@Override
	public Object setValue(final Object ctx, final Object elCtx,
			final VariableResolverFactory variableFactory, final Object value) {
		GlobalListenerFactory.notifySetListeners(ctx, name, variableFactory, value);
		return nextNode.setValue(ctx, elCtx, variableFactory, value);
	}

	@Override
	public AccessorNode getNextNode() {
		return nextNode;
	}

	@Override
	public AccessorNode setNextNode(final AccessorNode nextNode) {
		return this.nextNode = nextNode;
	}

	@Override
	public Class getKnownEgressType() {
		return Object.class;
	}
}
