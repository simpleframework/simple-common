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

package net.simpleframework.lib.org.mvel2.sh.command.basic;

import static net.simpleframework.lib.org.mvel2.sh.text.TextUtil.padTwo;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import net.simpleframework.lib.org.mvel2.sh.Command;
import net.simpleframework.lib.org.mvel2.sh.ShellSession;
import net.simpleframework.lib.org.mvel2.sh.text.TextUtil;
import net.simpleframework.lib.org.mvel2.util.StringAppender;

public class ObjectInspector implements Command {
	private static final int PADDING = 17;

	@Override
	public Object execute(final ShellSession session, final String[] args) {

		if (args.length == 0) {
			System.out.println("inspect: requires an argument.");
			return null;
		}

		if (!session.getVariables().containsKey(args[0])) {
			System.out.println("inspect: no such variable: " + args[0]);
			return null;
		}

		final Object val = session.getVariables().get(args[0]);

		System.out.println("Object Inspector");
		System.out.println(TextUtil.paint('-', PADDING));

		if (val == null) {
			System.out.println("[Value is Null]");
			return null;
		}

		final Class cls = val.getClass();
		boolean serialized = true;
		long serializedSize = 0;

		try {
			final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			final ObjectOutputStream objectOut = new ObjectOutputStream(
					new BufferedOutputStream(outStream));
			objectOut.writeObject(val);
			objectOut.flush();
			outStream.flush();

			serializedSize = outStream.size();
		} catch (final Exception e) {
			serialized = false;
		}

		write("VariableName", args[0]);
		write("Hashcode", val.hashCode());
		write("ClassType", cls.getName());
		write("Serializable", serialized);
		if (serialized) {
			write("SerializedSize", serializedSize + " bytes");
		}
		write("ClassHierarchy", renderClassHeirarchy(cls));
		write("Fields", cls.getFields().length);
		renderFields(cls);

		write("Methods", cls.getMethods().length);
		renderMethods(cls);

		System.out.println();

		return null;
	}

	private static String renderClassHeirarchy(Class cls) {
		final List<String> list = new LinkedList<String>();
		list.add(cls.getName());

		while ((cls = cls.getSuperclass()) != null) {
			list.add(cls.getName());
		}

		final StringAppender output = new StringAppender();

		for (int i = list.size() - 1; i != -1; i--) {
			output.append(list.get(i));
			if ((i - 1) != -1) {
				output.append(" -> ");
			}
		}

		return output.toString();
	}

	private static void renderFields(final Class cls) {
		final Field[] fields = cls.getFields();

		for (int i = 0; i < fields.length; i++) {
			write("", fields[i].getType().getName() + " " + fields[i].getName());
		}
	}

	private static void renderMethods(final Class cls) {
		final Method[] methods = cls.getMethods();

		Method m;
		final StringAppender appender = new StringAppender();
		int mf;
		for (int i = 0; i < methods.length; i++) {
			appender.append(TextUtil.paint(' ', PADDING + 2));
			if (((mf = (m = methods[i]).getModifiers()) & Modifier.PUBLIC) != 0) {
				appender.append("public");
			} else if ((mf & Modifier.PRIVATE) != 0) {
				appender.append("private");
			} else if ((mf & Modifier.PROTECTED) != 0) {
				appender.append("protected");
			}

			appender.append(' ').append(m.getReturnType().getName()).append(' ').append(m.getName())
					.append("(");
			final Class[] parmTypes = m.getParameterTypes();
			for (int y = 0; y < parmTypes.length; y++) {
				if (parmTypes[y].isArray()) {
					appender.append(parmTypes[y].getComponentType().getName() + "[]");
				} else {
					appender.append(parmTypes[y].getName());
				}
				if ((y + 1) < parmTypes.length) {
					appender.append(", ");
				}
			}
			appender.append(")");

			if (m.getDeclaringClass() != cls) {
				appender.append("    [inherited from: ").append(m.getDeclaringClass().getName())
						.append("]");
			}

			if ((i + 1) < methods.length) {
				appender.append('\n');
			}
		}

		System.out.println(appender.toString());
	}

	private static void write(final Object first, final Object second) {
		System.out.println(padTwo(first, ": " + second, PADDING));
	}

	@Override
	public String getDescription() {
		return "inspects an object";
	}

	@Override
	public String getHelp() {
		return "No help yet";
	}
}
