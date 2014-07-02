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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

public abstract class PagerTagSupport extends TagSupport {

	protected PagerTag pagerTag = null;

	protected final void restoreAttribute(final String name, final Object oldValue) {
		if (oldValue != null) {
			pageContext.setAttribute(name, oldValue);
		} else {
			pageContext.removeAttribute(name);
		}
	}

	private final PagerTag findRequestPagerTag(final String pagerId) {
		final Object obj = pageContext.getRequest().getAttribute(pagerId);
		if (obj instanceof PagerTag) {
			return (PagerTag) obj;
		}
		return null;
	}

	@Override
	public int doStartTag() throws JspException {
		if (id != null) {
			pagerTag = findRequestPagerTag(id);
			if (pagerTag == null) {
				throw new JspTagException("pager tag with id of \"" + id + "\" not found.");
			}
		} else {
			pagerTag = (PagerTag) findAncestorWithClass(this, PagerTag.class);
			if (pagerTag == null) {
				pagerTag = findRequestPagerTag(PagerTag.DEFAULT_ID);
				if (pagerTag == null) {
					throw new JspTagException("not nested within a pager tag"
							+ " and no pager tag found at request scope.");
				}
			}
		}

		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws JspException {
		pagerTag = null;
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		pagerTag = null;
		super.release();
	}
}

/* vim:set ts=4 sw=4: */
