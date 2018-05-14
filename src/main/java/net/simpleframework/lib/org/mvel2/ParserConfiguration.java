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

package net.simpleframework.lib.org.mvel2;

import static net.simpleframework.lib.org.mvel2.util.ParseTools.forNameWithInner;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.simpleframework.lib.org.mvel2.ast.Proto;
import net.simpleframework.lib.org.mvel2.compiler.AbstractParser;
import net.simpleframework.lib.org.mvel2.integration.Interceptor;
import net.simpleframework.lib.org.mvel2.util.MethodStub;

/**
 * The resusable parser configuration object.
 */
public class ParserConfiguration implements Serializable {
	private static final int MAX_NEGATIVE_CACHE_SIZE;

	protected Map<String, Object> imports;
	protected HashSet<String> packageImports;
	protected Map<String, Interceptor> interceptors;
	protected transient ClassLoader classLoader;

	private transient Set<String> nonValidImports;

	private boolean allowNakedMethCall = MVEL.COMPILER_OPT_ALLOW_NAKED_METH_CALL;

	private boolean allowBootstrapBypass = true;

	static {
		final String negCacheSize = System.getProperty("mvel2.compiler.max_neg_cache_size");
		if (negCacheSize != null) {
			MAX_NEGATIVE_CACHE_SIZE = Integer.parseInt(negCacheSize);
		} else {
			MAX_NEGATIVE_CACHE_SIZE = 1000;
		}
	}

	public ParserConfiguration() {
	}

	public ParserConfiguration(final Map<String, Object> imports,
			final Map<String, Interceptor> interceptors) {
		addAllImports(imports);
		this.interceptors = interceptors;
	}

	public ParserConfiguration(final Map<String, Object> imports,
			final HashSet<String> packageImports, final Map<String, Interceptor> interceptors) {
		addAllImports(imports);
		this.packageImports = packageImports;
		this.interceptors = interceptors;
	}

	public HashSet<String> getPackageImports() {
		return packageImports;
	}

	public void setPackageImports(final HashSet<String> packageImports) {
		this.packageImports = packageImports;
	}

	public Class getImport(final String name) {
		if (imports != null && imports.containsKey(name) && imports.get(name) instanceof Class) {
			return (Class) imports.get(name);
		}
		return (Class) (AbstractParser.LITERALS.get(name) instanceof Class
				? AbstractParser.LITERALS.get(name)
				: null);
	}

	public MethodStub getStaticImport(final String name) {
		return imports != null ? (MethodStub) imports.get(name) : null;
	}

	public Object getStaticOrClassImport(final String name) {
		return (imports != null && imports.containsKey(name) ? imports.get(name)
				: AbstractParser.LITERALS.get(name));
	}

	public void addPackageImport(final String packageName) {
		if (packageImports == null) {
			packageImports = new LinkedHashSet<>();
		}
		packageImports.add(packageName);
		if (!addClassMemberStaticImports(packageName)) {
			packageImports.add(packageName);
		}
	}

	private boolean addClassMemberStaticImports(final String packageName) {
		try {
			final Class c = Class.forName(packageName);
			initImports();
			if (c.isEnum()) {

				// noinspection unchecked
				for (final Enum e : (EnumSet<?>) EnumSet.allOf(c)) {
					imports.put(e.name(), e);
				}
				return true;
			} else {
				for (final Field f : c.getDeclaredFields()) {
					if ((f.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) == (Modifier.STATIC
							| Modifier.PUBLIC)) {
						imports.put(f.getName(), f.get(null));
					}
				}

			}
		} catch (final ClassNotFoundException e) {
			// do nothing.
		} catch (final IllegalAccessException e) {
			throw new RuntimeException("error adding static imports for: " + packageName, e);
		}
		return false;
	}

	public void addAllImports(final Map<String, Object> imports) {
		if (imports == null) {
			return;
		}

		initImports();

		Object o;

		for (final Map.Entry<String, Object> entry : imports.entrySet()) {
			if ((o = entry.getValue()) instanceof Method) {
				this.imports.put(entry.getKey(), new MethodStub((Method) o));
			} else {
				this.imports.put(entry.getKey(), o);
			}
		}
	}

	private boolean checkForDynamicImport(final String className) {
		if (packageImports == null) {
			return false;
		}
		if (!Character.isJavaIdentifierStart(className.charAt(0))) {
			return false;
		}
		if (nonValidImports != null && nonValidImports.contains(className)) {
			return false;
		}

		int found = 0;
		Class cls = null;
		for (final String pkg : packageImports) {
			try {
				cls = forNameWithInner(pkg + "." + className, getClassLoader());
				found++;
			} catch (final Throwable cnfe) {
				// do nothing.
			}
		}

		if (found > 1) {
			throw new RuntimeException("ambiguous class name: " + className);
		}
		if (found == 1) {
			addImport(className, cls);
			return true;
		}

		cacheNegativeHitForDynamicImport(className);
		return false;
	}

	public boolean hasImport(final String name) {
		return (imports != null && imports.containsKey(name))
				|| AbstractParser.CLASS_LITERALS.containsKey(name) || checkForDynamicImport(name);
	}

	private void initImports() {
		if (this.imports == null) {
			this.imports = new ConcurrentHashMap<>();
		}
	}

	public void addImport(final Class cls) {
		initImports();
		addImport(cls.getSimpleName(), cls);
	}

	public void addImport(final String name, final Class cls) {
		initImports();
		this.imports.put(name, cls);
	}

	public void addImport(final String name, final Proto proto) {
		initImports();
		this.imports.put(name, proto);
	}

	public void addImport(final String name, final Method method) {
		addImport(name, new MethodStub(method));
	}

	public void addImport(final String name, final MethodStub method) {
		initImports();
		this.imports.put(name, method);
	}

	public Map<String, Interceptor> getInterceptors() {
		return interceptors;
	}

	public void setInterceptors(final Map<String, Interceptor> interceptors) {
		this.interceptors = interceptors;
	}

	public Map<String, Object> getImports() {
		return imports;
	}

	public void setImports(final Map<String, Object> imports) {
		if (imports == null) {
			return;
		}

		Object val;

		for (final Map.Entry<String, Object> entry : imports.entrySet()) {
			if ((val = entry.getValue()) instanceof Class) {
				addImport(entry.getKey(), (Class) val);
			} else if (val instanceof Method) {
				addImport(entry.getKey(), (Method) val);
			} else if (val instanceof MethodStub) {
				addImport(entry.getKey(), (MethodStub) val);
			} else if (val instanceof Proto) {
				addImport(entry.getKey(), (Proto) entry.getValue());
			} else {
				throw new RuntimeException(
						"invalid element in imports map: " + entry.getKey() + " (" + val + ")");
			}
		}
	}

	public boolean hasImports() {
		return !(imports != null && imports.isEmpty())
				|| (packageImports != null && packageImports.size() != 0);
	}

	public ClassLoader getClassLoader() {
		return classLoader == null ? classLoader = Thread.currentThread().getContextClassLoader()
				: classLoader;
	}

	public void setClassLoader(final ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void setAllImports(final Map<String, Object> imports) {
		initImports();
		this.imports.clear();
		if (imports != null) {
			this.imports.putAll(imports);
		}
	}

	public void setImports(final HashMap<String, Object> imports) {
		// TODO: this method is here for backward compatibility. Could it be
		// removed/deprecated?
		setAllImports(imports);
	}

	private void cacheNegativeHitForDynamicImport(final String negativeHit) {
		if (nonValidImports == null) {
			nonValidImports = new LinkedHashSet<>();
		} else if (nonValidImports.size() > 1000) {
			final Iterator<String> i = nonValidImports.iterator();
			i.next();
			i.remove();
		}

		nonValidImports.add(negativeHit);
	}

	public void flushCaches() {
		if (nonValidImports != null) {
			nonValidImports.clear();
		}
	}

	public boolean isAllowNakedMethCall() {
		return allowNakedMethCall;
	}

	public void setAllowNakedMethCall(final boolean allowNakedMethCall) {
		this.allowNakedMethCall = allowNakedMethCall;
	}

	public boolean isAllowBootstrapBypass() {
		return allowBootstrapBypass;
	}

	public void setAllowBootstrapBypass(final boolean allowBootstrapBypass) {
		this.allowBootstrapBypass = allowBootstrapBypass;
	}
}
