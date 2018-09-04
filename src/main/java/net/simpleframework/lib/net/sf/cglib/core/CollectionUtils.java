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
package net.simpleframework.lib.net.sf.cglib.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Chris Nokleberg
 * @version $Id: CollectionUtils.java,v 1.7 2004/06/24 21:15:21 herbyderby Exp $
 */
public class CollectionUtils {
	private CollectionUtils() {
	}

	public static Map bucket(final Collection c, final Transformer t) {
		final Map buckets = new HashMap();
		for (final Iterator it = c.iterator(); it.hasNext();) {
			final Object value = it.next();
			final Object key = t.transform(value);
			List bucket = (List) buckets.get(key);
			if (bucket == null) {
				buckets.put(key, bucket = new LinkedList());
			}
			bucket.add(value);
		}
		return buckets;
	}

	public static void reverse(final Map source, final Map target) {
		for (final Iterator it = source.keySet().iterator(); it.hasNext();) {
			final Object key = it.next();
			target.put(source.get(key), key);
		}
	}

	public static Collection filter(final Collection c, final Predicate p) {
		final Iterator it = c.iterator();
		while (it.hasNext()) {
			if (!p.evaluate(it.next())) {
				it.remove();
			}
		}
		return c;
	}

	public static List transform(final Collection c, final Transformer t) {
		final List result = new ArrayList(c.size());
		for (final Iterator it = c.iterator(); it.hasNext();) {
			result.add(t.transform(it.next()));
		}
		return result;
	}

	public static Map getIndexMap(final List list) {
		final Map indexes = new HashMap();
		int index = 0;
		for (final Iterator it = list.iterator(); it.hasNext();) {
			indexes.put(it.next(), new Integer(index++));
		}
		return indexes;
	}
}
