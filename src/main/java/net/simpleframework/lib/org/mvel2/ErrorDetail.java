/**
 * MVEL 2.0
 * Copyright (C) 2007 MVFLEX/Valhalla Project and the Codehaus
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

public class ErrorDetail {

	private char[] expr;
	private int cursor;
	private boolean critical;
	private String message;

	private int lineNumber;
	private int column;

	public ErrorDetail(final char[] expr, final int cursor, final boolean critical,
			final String message) {
		this.expr = expr;
		this.cursor = cursor;
		this.critical = critical;
		this.message = message;

		calcRowAndColumn();
	}

	public boolean isCritical() {
		return critical;
	}

	public void setCritical(final boolean critical) {
		this.critical = critical;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public int getCursor() {
		return cursor;
	}

	public void calcRowAndColumn() {
		int row = 1;
		int col = 1;

		if ((lineNumber != 0 && column != 0) || expr == null || expr.length == 0) {
			return;
		}

		for (int i = 0; i < cursor; i++) {
			switch (expr[i]) {
			case '\r':
				continue;
			case '\n':
				row++;
				col = 0;
				break;

			default:
				col++;
			}
		}

		this.lineNumber = row;
		this.column = col;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getColumn() {
		return column;
	}

	public void setCursor(final int cursor) {
		this.cursor = cursor;
	}

	public void setExpr(final char[] expr) {
		this.expr = expr;
	}

	public char[] getExpr() {
		return expr;
	}

	public void setLineNumber(final int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public void setColumn(final int column) {
		this.column = column;
	}

	@Override
	public String toString() {
		if (critical) {
			return "(" + lineNumber + "," + column + ") " + message;
		} else {
			return "(" + lineNumber + "," + column + ") WARNING: " + message;
		}
	}
}
