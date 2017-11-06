package net.simpleframework.lib.org.mvel2.integration;

import java.util.LinkedList;
import java.util.List;

public class GlobalListenerFactory {
	private static List<Listener> propertyGetListeners;
	private static List<Listener> propertySetListeners;

	public static boolean hasGetListeners() {
		return propertyGetListeners != null && !propertyGetListeners.isEmpty();
	}

	public static boolean hasSetListeners() {
		return propertySetListeners != null && !propertySetListeners.isEmpty();
	}

	public static boolean registerGetListener(final Listener getListener) {
		if (propertyGetListeners == null) {
			propertyGetListeners = new LinkedList<>();
		}
		return propertyGetListeners.add(getListener);
	}

	public static boolean registerSetListener(final Listener getListener) {
		if (propertySetListeners == null) {
			propertySetListeners = new LinkedList<>();
		}
		return propertySetListeners.add(getListener);
	}

	public static void notifyGetListeners(final Object target, final String name,
			final VariableResolverFactory variableFactory) {
		if (propertyGetListeners != null) {
			for (final Listener l : propertyGetListeners) {
				l.onEvent(target, name, variableFactory, null);
			}
		}
	}

	public static void notifySetListeners(final Object target, final String name,
			final VariableResolverFactory variableFactory, final Object value) {
		if (propertySetListeners != null) {
			for (final Listener l : propertySetListeners) {
				l.onEvent(target, name, variableFactory, value);
			}
		}
	}

	public static void disposeAll() {
		propertyGetListeners = null;
		propertySetListeners = null;
	}
}
