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

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import net.simpleframework.lib.com.jsptags.navigation.pager.parser.PagerTagExport;
import net.simpleframework.lib.com.jsptags.navigation.pager.parser.ParseException;
import net.simpleframework.lib.com.jsptags.navigation.pager.parser.TagExportParser;

public final class PagerTag extends TagSupport {

	static final String DEFAULT_ID = "pager";

	private static final int DEFAULT_MAX_ITEMS = Integer.MAX_VALUE, DEFAULT_MAX_PAGE_ITEMS = 10,
			DEFAULT_MAX_INDEX_PAGES = 10;

	static final String OFFSET_PARAM = ".offset";

	static final String
	// scope values
			PAGE = "page",
			REQUEST = "request",
			// index values
			CENTER = "center", FORWARD = "forward", HALF_FULL = "half-full";

	/*
	 * Tag Properties
	 */
	private String url = null;
	private String index = null;
	private int items = 0;
	private int maxItems = DEFAULT_MAX_ITEMS;
	private int maxPageItems = DEFAULT_MAX_PAGE_ITEMS;
	private int maxIndexPages = DEFAULT_MAX_INDEX_PAGES;
	private boolean isOffset = false;
	private String export = null;
	private String scope = null;

	/*
	 * Tag Variables
	 */
	private StringBuffer uri = null;
	private int params = 0;
	private int offset = 0;
	private int itemCount = 0;
	private int pageNumber = 0;
	private Integer pageNumberInteger = null;

	private String idOffsetParam = DEFAULT_ID + OFFSET_PARAM;
	private PagerTagExport pagerTagExport = null;
	private Object oldPager = null;
	private Object oldOffset = null;
	private Object oldPageNumber = null;

	public PagerTag() {
		id = DEFAULT_ID;
	}

	@Override
	public final void setId(final String sid) {
		super.setId(sid);
		idOffsetParam = sid + OFFSET_PARAM;
	}

	public final void setUrl(final String value) {
		url = value;
	}

	public final String getUrl() {
		return url;
	}

	public final void setIndex(final String val) throws JspException {
		if (!(val == null || CENTER.equals(val) || FORWARD.equals(val) || HALF_FULL.equals(val))) {
			throw new JspTagException("value for attribute \"index\" "
					+ "must be either \"center\", \"forward\" or \"half-full\".");
		}
		index = val;
	}

	public final String getIndex() {
		return index;
	}

	public final void setItems(final int value) {
		items = value;
	}

	public final int getItems() {
		return items;
	}

	public final void setMaxItems(final int value) {
		maxItems = value;
	}

	public final int getMaxItems() {
		return maxItems;
	}

	public final void setMaxPageItems(final int value) {
		maxPageItems = value;
	}

	public final int getMaxPageItems() {
		return maxPageItems;
	}

	public final void setMaxIndexPages(final int value) {
		maxIndexPages = value;
	}

	public final int getMaxIndexPages() {
		return maxIndexPages;
	}

	public final void setIsOffset(final boolean val) {
		isOffset = val;
	}

	public final boolean getIsOffset() {
		return isOffset;
	}

	public final void setExport(final String value) throws JspException {
		if (export != value) {
			try {
				pagerTagExport = TagExportParser.parsePagerTagExport(value);
			} catch (final ParseException ex) {
				throw new JspTagException(ex.getMessage());
			}
		}
		export = value;
	}

	public final String getExport() {
		return export;
	}

	public final void setScope(final String val) throws JspException {
		if (!(val == null || PAGE.equals(val) || REQUEST.equals(val))) {
			throw new JspTagException("value for attribute \"scope\" "
					+ "must be either \"page\" or \"request\".");
		}
		scope = val;
	}

	public final String getScope() {
		return scope;
	}

	final void addParam(String name, String value) {
		if (value != null) {
			name = java.net.URLEncoder.encode(name);
			value = java.net.URLEncoder.encode(value);

			uri.append(params == 0 ? '?' : '&').append(name).append('=').append(value);

			params++;

		} else {
			final String[] values = pageContext.getRequest().getParameterValues(name);

			if (values != null) {
				name = java.net.URLEncoder.encode(name);
				for (int i = 0, l = values.length; i < l; i++) {
					value = java.net.URLEncoder.encode(values[i]);
					uri.append(params == 0 ? '?' : '&').append(name).append('=').append(value);

					params++;
				}
			}
		}
	}

	final boolean nextItem() {
		boolean showItem = false;
		if (itemCount < maxItems) {
			showItem = (itemCount >= offset && itemCount < (offset + maxPageItems));
			itemCount++;
		}
		return showItem;
	}

	final int getOffset() {
		return offset;
	}

	final boolean isIndexNeeded() {
		return (offset != 0 || getItemCount() > maxPageItems);
	}

	final boolean hasPrevPage() {
		return (offset > 0);
	}

	final boolean hasNextPage() {
		return (getItemCount() > getNextOffset());
	}

	final boolean hasPage(final int page) {
		return (page >= 0 && getItemCount() > (page * maxPageItems));
	}

	final int getPrevOffset() {
		return Math.max(0, offset - maxPageItems);
	}

	final int getNextOffset() {
		return offset + maxPageItems;
	}

	final String getOffsetUrl(final int pageOffset) {
		final int uriLen = uri.length();
		uri.append(params == 0 ? '?' : '&').append(idOffsetParam).append('=').append(pageOffset);
		final String offsetUrl = uri.toString();
		uri.setLength(uriLen);
		return offsetUrl;
	}

	final String getPageUrl(final int i) {
		return getOffsetUrl(maxPageItems * i);
	}

	final Integer getOffsetPageNumber(final int pageOffset) {
		return new Integer(1 + pageNumber(pageOffset));
	}

	final Integer getPageNumber(final int i) {
		if (i == pageNumber) {
			return pageNumberInteger;
		}
		return new Integer(1 + i);
	}

	final int getPageNumber() {
		return pageNumber;
	}

	final int getPageCount() {
		return pageNumber(getItemCount());
	}

	final int getFirstIndexPage() {
		int firstPage = 0;
		final int halfIndexPages = maxIndexPages / 2;

		if (FORWARD.equals(index)) {
			firstPage = Math.min(pageNumber + 1, getPageCount());
		} else if (!(HALF_FULL.equals(index) && pageNumber < halfIndexPages)) {
			final int pages = getPageCount();
			if (pages > maxIndexPages) {
				// put the current page in middle of the index
				firstPage = Math.max(0, pageNumber - halfIndexPages);

				final int indexPages = pages - firstPage;
				if (indexPages < maxIndexPages) {
					firstPage -= (maxIndexPages - indexPages);
				}
			}
		}

		return firstPage;
	}

	final int getLastIndexPage(final int firstPage) {
		final int pages = getPageCount();
		final int halfIndexPages = maxIndexPages / 2;
		int maxPages;
		if (HALF_FULL.equals(index) && pageNumber < halfIndexPages) {
			maxPages = pageNumber + halfIndexPages;
		} else {
			maxPages = firstPage + maxIndexPages;
		}
		return (pages <= maxPages ? pages : maxPages) - 1;
	}

	final int getItemCount() {
		return (items != 0 ? items : itemCount);
	}

	private final int pageNumber(final int offset) {
		return (offset / maxPageItems) + (offset % maxPageItems == 0 ? 0 : 1);
	}

	@Override
	public int doStartTag() throws JspException {

		String baseUri;
		if (url != null) {
			baseUri = url;
		} else {
			baseUri = ((HttpServletRequest) pageContext.getRequest()).getRequestURI();
			final int i = baseUri.indexOf('?');
			if (i != -1) {
				baseUri = baseUri.substring(0, i);
			}
		}
		if (uri == null) {
			uri = new StringBuffer(baseUri.length() + 32);
		} else {
			uri.setLength(0);
		}
		uri.append(baseUri);

		params = 0;
		offset = 0;
		itemCount = 0;

		final String offsetParam = pageContext.getRequest().getParameter(idOffsetParam);
		if (offsetParam != null) {
			try {
				offset = Math.max(0, Integer.parseInt(offsetParam));
				if (isOffset) {
					itemCount = offset;
				}
			} catch (final NumberFormatException ignore) {
			}
		}

		pageNumber = pageNumber(offset);
		pageNumberInteger = new Integer(1 + pageNumber);

		if (REQUEST.equals(scope)) {
			final ServletRequest request = pageContext.getRequest();

			oldPager = request.getAttribute(id);
			request.setAttribute(id, this);

			if (pagerTagExport != null) {
				String name;
				if ((name = pagerTagExport.getPageOffset()) != null) {
					oldOffset = request.getAttribute(name);
					request.setAttribute(name, new Integer(offset));
				}
				if ((name = pagerTagExport.getPageNumber()) != null) {
					oldPageNumber = request.getAttribute(name);
					request.setAttribute(name, pageNumberInteger);
				}
			}
		} else {
			if (pagerTagExport != null) {
				String name;
				if ((name = pagerTagExport.getPageOffset()) != null) {
					oldOffset = pageContext.getAttribute(name);
					pageContext.setAttribute(name, new Integer(offset));
				}
				if ((name = pagerTagExport.getPageNumber()) != null) {
					oldPageNumber = pageContext.getAttribute(name);
					pageContext.setAttribute(name, pageNumberInteger);
				}
			}
		}

		return EVAL_BODY_INCLUDE;
	}

	private static void restoreAttribute(final ServletRequest request, final String name,
			final Object oldValue) {
		if (oldValue != null) {
			request.setAttribute(name, oldValue);
		} else {
			request.removeAttribute(name);
		}
	}

	private static void restoreAttribute(final PageContext pageContext, final String name,
			final Object oldValue) {
		if (oldValue != null) {
			pageContext.setAttribute(name, oldValue);
		} else {
			pageContext.removeAttribute(name);
		}
	}

	@Override
	public int doEndTag() throws JspException {
		if (REQUEST.equals(scope)) {
			final ServletRequest request = pageContext.getRequest();

			restoreAttribute(request, id, oldPager);
			oldPager = null;

			if (pagerTagExport != null) {
				String name;
				if ((name = pagerTagExport.getPageOffset()) != null) {
					restoreAttribute(request, name, oldOffset);
					oldOffset = null;
				}

				if ((name = pagerTagExport.getPageNumber()) != null) {
					restoreAttribute(request, name, oldPageNumber);
					oldPageNumber = null;
				}
			}
		} else {
			if (pagerTagExport != null) {
				String name;
				if ((name = pagerTagExport.getPageOffset()) != null) {
					restoreAttribute(pageContext, name, oldOffset);
					oldOffset = null;
				}

				if ((name = pagerTagExport.getPageNumber()) != null) {
					restoreAttribute(pageContext, name, oldPageNumber);
					oldPageNumber = null;
				}
			}
		}

		// limit size of re-usable StringBuffer
		if (uri.capacity() > 1024) {
			uri = null;
		}

		pageNumberInteger = null;

		return EVAL_PAGE;
	}

	@Override
	public void release() {
		url = null;
		index = null;
		items = 0;
		maxItems = DEFAULT_MAX_ITEMS;
		maxPageItems = DEFAULT_MAX_PAGE_ITEMS;
		maxIndexPages = DEFAULT_MAX_INDEX_PAGES;
		isOffset = false;
		export = null;
		scope = null;

		uri = null;
		params = 0;
		offset = 0;
		itemCount = 0;
		pageNumber = 0;
		pageNumberInteger = null;

		idOffsetParam = DEFAULT_ID + OFFSET_PARAM;
		pagerTagExport = null;
		oldPager = null;
		oldOffset = null;
		oldPageNumber = null;

		super.release();
	}
}

/* vim:set ts=4 sw=4: */
