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

import java.util.Map;

import net.simpleframework.lib.org.mvel2.sh.Command;
import net.simpleframework.lib.org.mvel2.sh.CommandException;
import net.simpleframework.lib.org.mvel2.sh.ShellSession;

public class ShowVars implements Command {

	@Override
	public Object execute(final ShellSession session, final String[] args) {
		boolean values = false;

		final Map<String, Object> vars = session.getVariables();

		for (int i = 0; i < args.length; i++) {
			if ("-values".equals(args[i])) {
				values = true;
			} else {
				throw new CommandException("unknown argument: " + args[i]);
			}
		}

		System.out.println("Printing Variables ...");
		if (values) {
			for (final String key : vars.keySet()) {
				System.out.println(key + " => " + String.valueOf(vars.get(key)));
			}
		} else {
			for (final String key : vars.keySet()) {
				System.out.println(key);
			}
		}

		System.out.println(" ** " + vars.size() + " variables total.");

		return null;
	}

	@Override
	public String getDescription() {
		return "shows current variables";
	}

	@Override
	public String getHelp() {
		return "no help yet";
	}
}
