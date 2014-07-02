/*
 * Pager Tag Library
 * 
 * Copyright (C) 2002 James Klicman <james@jsptags.com>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package net.simpleframework.lib.com.jsptags.navigation.pager;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

import net.simpleframework.lib.com.jsptags.navigation.pager.parser.PagerTagExport;
import net.simpleframework.lib.com.jsptags.navigation.pager.parser.ParseException;
import net.simpleframework.lib.com.jsptags.navigation.pager.parser.TagExportParser;

public final class PagerTagExtraInfo extends TagExtraInfo {

	@Override
	public VariableInfo[] getVariableInfo(final TagData tagData) {
		final String export = tagData.getAttributeString("export");
		if (export != null) {
			try {
				final PagerTagExport pagerTagExport = TagExportParser.parsePagerTagExport(export);
				int len = 0;
				if (pagerTagExport.getPageOffset() != null) {
					len++;
				}
				if (pagerTagExport.getPageNumber() != null) {
					len++;
				}

				final VariableInfo[] varinfo = new VariableInfo[len];
				int i = 0;

				String name;
				if ((name = pagerTagExport.getPageOffset()) != null) {
					varinfo[i++] = new VariableInfo(name, java.lang.Integer.class.getName(), true,
							VariableInfo.NESTED);
				}
				if ((name = pagerTagExport.getPageNumber()) != null) {
					varinfo[i++] = new VariableInfo(name, java.lang.Integer.class.getName(), true,
							VariableInfo.NESTED);
				}

				return varinfo;
			} catch (final ParseException ex) {
				return new VariableInfo[0];
			}
		}
		return new VariableInfo[0];
	}

	@Override
	public boolean isValid(final TagData tagData) {
		final String export = tagData.getAttributeString("export");
		if (export != null) {
			try {
				TagExportParser.parsePagerTagExport(export);
			} catch (final ParseException ex) {
				return false;
			}
		}
		return true;
	}
}

/* vim:set ts=4 sw=4: */
