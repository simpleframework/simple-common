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

package net.simpleframework.lib.org.mvel2.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class InternalNumber extends BigDecimal {
	public InternalNumber(final char[] chars, final int i, final int i1) {
		super(chars, i, i1);
	}

	public InternalNumber(final char[] chars, final int i, final int i1,
			final MathContext mathContext) {
		super(chars, i, i1, mathContext);
	}

	public InternalNumber(final char[] chars) {
		super(chars);
	}

	public InternalNumber(final char[] chars, final MathContext mathContext) {
		super(chars, mathContext);
	}

	public InternalNumber(final String s) {
		super(s);
	}

	public InternalNumber(final String s, final MathContext mathContext) {
		super(s, mathContext);
	}

	public InternalNumber(final double v) {
		super(v);
	}

	public InternalNumber(final double v, final MathContext mathContext) {
		super(v, mathContext);
	}

	public InternalNumber(final BigInteger bigInteger) {
		super(bigInteger);
	}

	public InternalNumber(final BigInteger bigInteger, final MathContext mathContext) {
		super(bigInteger, mathContext);
	}

	public InternalNumber(final BigInteger bigInteger, final int i) {
		super(bigInteger, i);
	}

	public InternalNumber(final BigInteger bigInteger, final int i, final MathContext mathContext) {
		super(bigInteger, i, mathContext);
	}

	public InternalNumber(final int i) {
		super(i);
	}

	public InternalNumber(final int i, final MathContext mathContext) {
		super(i, mathContext);
	}

	public InternalNumber(final long l) {
		super(l);
	}

	public InternalNumber(final long l, final MathContext mathContext) {
		super(l, mathContext);
	}
}
