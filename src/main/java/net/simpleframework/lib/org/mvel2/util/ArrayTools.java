/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.simpleframework.lib.org.mvel2.util;

public class ArrayTools {

	public static int findFirst(final char c, final int start, final int offset, final char[] array) {
		final int end = start + offset;
		for (int i = start; i < end; i++) {
			if (array[i] == c) {
				return i;
			}
		}
		return -1;
	}

	public static int findLast(final char c, final int start, final int offset, final char[] array) {
		for (int i = start + offset - 1; i >= 0; i--) {
			if (array[i] == c) {
				return i;
			}
		}
		return -1;
	}
}
