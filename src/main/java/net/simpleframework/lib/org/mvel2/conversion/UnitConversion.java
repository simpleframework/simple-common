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

package net.simpleframework.lib.org.mvel2.conversion;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.simpleframework.lib.org.mvel2.ConversionHandler;
import net.simpleframework.lib.org.mvel2.Unit;

public class UnitConversion implements ConversionHandler {
	private static final Logger LOG = Logger.getLogger(UnitConversion.class.getName());

	@Override
	public Object convertFrom(final Object in) {
		try {
			return Unit.class.newInstance().convertFrom(in);
		} catch (final InstantiationException e) {
			LOG.log(Level.SEVERE, "", e);
		} catch (final IllegalAccessException e) {
			LOG.log(Level.SEVERE, "", e);
		}
		return null;
	}

	@Override
	public boolean canConvertFrom(final Class cls) {
		if (Unit.class.isAssignableFrom(cls) || Number.class.isAssignableFrom(cls)) {
			try {
				return Unit.class.newInstance().canConvertFrom(cls);
			} catch (final InstantiationException e) {
				LOG.log(Level.SEVERE, "", e);
			} catch (final IllegalAccessException e) {
				LOG.log(Level.SEVERE, "", e);
			}
		}
		return false;
	}
}
