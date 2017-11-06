/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.simpleframework.lib.org.objectweb.asm.optimizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import net.simpleframework.lib.org.objectweb.asm.ClassReader;
import net.simpleframework.lib.org.objectweb.asm.ClassWriter;
import net.simpleframework.lib.org.objectweb.asm.Handle;
import net.simpleframework.lib.org.objectweb.asm.Type;
import net.simpleframework.lib.org.objectweb.asm.commons.Remapper;
import net.simpleframework.lib.org.objectweb.asm.commons.SimpleRemapper;

/**
 * A class file shrinker utility.
 * 
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public class Shrinker {

	static final HashMap<String, String> MAPPING = new HashMap<>();

	public static void main(final String[] args) throws IOException {
		final Properties properties = new Properties();
		final int n = args.length - 1;
		for (int i = 0; i < n - 1; ++i) {
			properties.load(new FileInputStream(args[i]));
		}

		for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
			MAPPING.put((String) entry.getKey(), (String) entry.getValue());
		}

		final Set<String> unused = new HashSet<>(MAPPING.keySet());

		final File f = new File(args[n - 1]);
		final File d = new File(args[n]);

		optimize(f, d, new SimpleRemapper(MAPPING) {
			@Override
			public String map(final String key) {
				final String s = super.map(key);
				if (s != null) {
					unused.remove(key);
				}
				return s;
			}
		});

		final Iterator<String> i = unused.iterator();
		while (i.hasNext()) {
			final String s = i.next();
			if (!s.endsWith("/remove")) {
				System.out.println("INFO: unused mapping " + s);
			}
		}
	}

	static void optimize(final File f, final File d, final Remapper remapper) throws IOException {
		if (f.isDirectory()) {
			final File[] files = f.listFiles();
			for (int i = 0; i < files.length; ++i) {
				optimize(files[i], d, remapper);
			}
		} else if (f.getName().endsWith(".class")) {
			final ConstantPool cp = new ConstantPool();
			ClassReader cr = new ClassReader(new FileInputStream(f));
			// auto-boxing removal requires to recompute the maxs
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			final ClassConstantsCollector ccc = new ClassConstantsCollector(cw, cp);
			final ClassOptimizer co = new ClassOptimizer(ccc, remapper);
			cr.accept(co, ClassReader.SKIP_DEBUG);

			final Set<Constant> constants = new TreeSet<>(new ConstantComparator());
			constants.addAll(cp.values());

			cr = new ClassReader(cw.toByteArray());
			cw = new ClassWriter(0);
			final Iterator<Constant> i = constants.iterator();
			while (i.hasNext()) {
				final Constant c = i.next();
				c.write(cw);
			}
			cr.accept(cw, ClassReader.SKIP_DEBUG);

			if (MAPPING.get(cr.getClassName() + "/remove") != null) {
				return;
			}
			final String n = remapper.mapType(cr.getClassName());
			final File g = new File(d, n + ".class");
			if (!g.exists() || g.lastModified() < f.lastModified()) {
				if (!g.getParentFile().exists() && !g.getParentFile().mkdirs()) {
					throw new IOException("Cannot create directory " + g.getParentFile());
				}
				final OutputStream os = new FileOutputStream(g);
				try {
					os.write(cw.toByteArray());
				} finally {
					os.close();
				}
			}
		}
	}

	static class ConstantComparator implements Comparator<Constant> {

		@Override
		public int compare(final Constant c1, final Constant c2) {
			int d = getSort(c1) - getSort(c2);
			if (d == 0) {
				switch (c1.type) {
				case 'I':
					return ((Integer) c1.intVal).compareTo(c2.intVal);
				case 'J':
					return ((Long) c1.longVal).compareTo(c2.longVal);
				case 'F':
					return ((Float) c1.floatVal).compareTo(c2.floatVal);
				case 'D':
					return ((Double) c1.doubleVal).compareTo(c2.doubleVal);
				case 's':
				case 'S':
				case 'C':
				case 't':
					return c1.strVal1.compareTo(c2.strVal1);
				case 'T':
					d = c1.strVal1.compareTo(c2.strVal1);
					if (d == 0) {
						d = c1.strVal2.compareTo(c2.strVal2);
					}
					break;
				case 'y':
					d = c1.strVal1.compareTo(c2.strVal1);
					if (d == 0) {
						d = c1.strVal2.compareTo(c2.strVal2);
						if (d == 0) {
							final Handle bsm1 = (Handle) c1.objVal3;
							final Handle bsm2 = (Handle) c2.objVal3;
							d = compareHandle(bsm1, bsm2);
							if (d == 0) {
								d = compareObjects(c1.objVals, c2.objVals);
							}
						}
					}
					break;

				default:
					d = c1.strVal1.compareTo(c2.strVal1);
					if (d == 0) {
						d = c1.strVal2.compareTo(c2.strVal2);
						if (d == 0) {
							d = ((String) c1.objVal3).compareTo((String) c2.objVal3);
						}
					}
				}
			}
			return d;
		}

		private static int compareHandle(final Handle h1, final Handle h2) {
			int d = h1.getTag() - h2.getTag();
			if (d == 0) {
				d = h1.getOwner().compareTo(h2.getOwner());
				if (d == 0) {
					d = h1.getName().compareTo(h2.getName());
					if (d == 0) {
						d = h1.getDesc().compareTo(h2.getDesc());
					}
				}
			}
			return d;
		}

		private static int compareType(final Type mtype1, final Type mtype2) {
			return mtype1.getDescriptor().compareTo(mtype2.getDescriptor());
		}

		@SuppressWarnings("unchecked")
		private static int compareObjects(final Object[] objVals1, final Object[] objVals2) {
			final int length = objVals1.length;
			int d = length - objVals2.length;
			if (d == 0) {
				for (int i = 0; i < length; i++) {
					final Object objVal1 = objVals1[i];
					final Object objVal2 = objVals2[i];
					d = objVal1.getClass().getName().compareTo(objVal2.getClass().getName());
					if (d == 0) {
						if (objVal1 instanceof Type) {
							d = compareType((Type) objVal1, (Type) objVal2);
						} else if (objVal1 instanceof Handle) {
							d = compareHandle((Handle) objVal1, (Handle) objVal2);
						} else {
							d = ((Comparable<Object>) objVal1).compareTo(objVal2);
						}
					}

					if (d != 0) {
						return d;
					}
				}
			}
			return 0;
		}

		private static int getSort(final Constant c) {
			switch (c.type) {
			case 'I':
				return 0;
			case 'J':
				return 1;
			case 'F':
				return 2;
			case 'D':
				return 3;
			case 's':
				return 4;
			case 'S':
				return 5;
			case 'C':
				return 6;
			case 'T':
				return 7;
			case 'G':
				return 8;
			case 'M':
				return 9;
			case 'N':
				return 10;
			case 'y':
				return 11;
			case 't':
				return 12;
			default:
				return 100 + c.type - 'h';
			}
		}
	}
}
