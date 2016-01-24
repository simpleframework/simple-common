/*
 * Copyright 2003,2004 The Apache Software Foundation
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
package net.simpleframework.lib.net.sf.cglib.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;

import net.simpleframework.lib.net.sf.cglib.core.CodeGenerationException;

/**
 * This class is meant to be used as replacement for
 * <code>java.lang.reflect.Proxy</code> under JDK 1.2. There are some known
 * subtle differences:
 * <ul>
 * <li>The exceptions returned by invoking <code>getExceptionTypes</code> on the
 * <code>Method</code> passed to the <code>invoke</code> method <b>are</b> the
 * exact set that can be thrown without resulting in an
 * <code>UndeclaredThrowableException</code> being thrown.
 * <li>{@link UndeclaredThrowableException} is used instead of
 * <code>java.lang.reflect.UndeclaredThrowableException</code>.
 * </ul>
 * <p>
 * 
 * @version $Id: Proxy.java,v 1.6 2004/06/24 21:15:19 herbyderby Exp $
 */
public class Proxy implements Serializable {
	protected InvocationHandler h;

	private static final CallbackFilter BAD_OBJECT_METHOD_FILTER = new CallbackFilter() {
		@Override
		public int accept(final Method method) {
			if (method.getDeclaringClass().getName().equals("java.lang.Object")) {
				final String name = method.getName();
				if (!(name.equals("hashCode") || name.equals("equals") || name.equals("toString"))) {
					return 1;
				}
			}
			return 0;
		}
	};

	protected Proxy(final InvocationHandler h) {
		Enhancer.registerCallbacks(getClass(), new Callback[] { h, null });
		this.h = h;
	}

	// private for security of isProxyClass
	private static class ProxyImpl extends Proxy {
		protected ProxyImpl(final InvocationHandler h) {
			super(h);
		}
	}

	public static InvocationHandler getInvocationHandler(final Object proxy) {
		if (!(proxy instanceof ProxyImpl)) {
			throw new IllegalArgumentException("Object is not a proxy");
		}
		return ((Proxy) proxy).h;
	}

	public static Class getProxyClass(final ClassLoader loader, final Class[] interfaces) {
		final Enhancer e = new Enhancer();
		e.setSuperclass(ProxyImpl.class);
		e.setInterfaces(interfaces);
		e.setCallbackTypes(new Class[] { InvocationHandler.class, NoOp.class, });
		e.setCallbackFilter(BAD_OBJECT_METHOD_FILTER);
		e.setUseFactory(false);
		return e.createClass();
	}

	public static boolean isProxyClass(final Class cl) {
		return cl.getSuperclass().equals(ProxyImpl.class);
	}

	public static Object newProxyInstance(final ClassLoader loader, final Class[] interfaces,
			final InvocationHandler h) {
		try {
			final Class clazz = getProxyClass(loader, interfaces);
			return clazz.getConstructor(new Class[] { InvocationHandler.class }).newInstance(
					new Object[] { h });
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Exception e) {
			throw new CodeGenerationException(e);
		}
	}
}
