package net.simpleframework.lib.org.mvel2.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.simpleframework.lib.org.mvel2.CompileException;
import net.simpleframework.lib.org.mvel2.ErrorDetail;

/**
 * @author Mike Brock .
 */
public class ErrorUtil {
	private static final Logger LOG = Logger.getLogger(ErrorUtil.class.getName());

	public static CompileException rewriteIfNeeded(final CompileException caught, final char[] outer,
			final int outerCursor) {
		if (outer != caught.getExpr()) {
			if (caught.getExpr().length <= caught.getCursor()) {
				caught.setCursor(caught.getExpr().length - 1);
			}

			try {
				final String innerExpr = new String(caught.getExpr()).substring(caught.getCursor());
				caught.setExpr(outer);

				final String outerStr = new String(outer);

				final int newCursor = outerStr.substring(outerStr.indexOf(new String(caught.getExpr())))
						.indexOf(innerExpr);

				caught.setCursor(newCursor);
			} catch (final Throwable t) {
				LOG.log(Level.WARNING, "", t);
			}
		}
		return caught;
	}

	public static ErrorDetail rewriteIfNeeded(final ErrorDetail detail, final char[] outer,
			final int outerCursor) {
		if (outer != detail.getExpr()) {
			final String innerExpr = new String(detail.getExpr()).substring(detail.getCursor());
			detail.setExpr(outer);

			int newCursor = outerCursor;
			newCursor += new String(outer).substring(outerCursor).indexOf(innerExpr);

			detail.setCursor(newCursor);
		}
		return detail;
	}
}
