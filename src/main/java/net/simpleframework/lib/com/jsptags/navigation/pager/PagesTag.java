/*
 *  Pager Tag Library
 *
 *  Copyright (C) 2002  James Klicman <james@jsptags.com>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.simpleframework.lib.com.jsptags.navigation.pager;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

public final class PagesTag extends PageTagSupport implements BodyTag {

	private BodyContent bodyContent = null;
	private int page = 0;
	private int lastPage = 0;

	@Override
	public void setBodyContent(final BodyContent bc) {
		bodyContent = bc;
	}

	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();

		final int firstPage = pagerTag.getFirstIndexPage();
		lastPage = pagerTag.getLastIndexPage(firstPage);
		page = firstPage;

		return (page <= lastPage ? EVAL_BODY_TAG : SKIP_BODY);
	}

	@Override
	public void doInitBody() throws JspException {
		setPageAttributes(page);
		page++;
	}

	@Override
	public int doAfterBody() throws JspException {
		if (page <= lastPage) {
			setPageAttributes(page);
			page++;
			return EVAL_BODY_TAG;
		} else {
			try {
				bodyContent.writeOut(bodyContent.getEnclosingWriter());
				return SKIP_BODY;
			} catch (final IOException e) {
				throw new JspTagException(e.toString());
			}
		}
	}

	@Override
	public int doEndTag() throws JspException {

		bodyContent = null;

		super.doEndTag();

		return EVAL_PAGE;
	}

	@Override
	public void release() {
		bodyContent = null;
		super.release();
	}
}

/* vim:set ts=4 sw=4: */
