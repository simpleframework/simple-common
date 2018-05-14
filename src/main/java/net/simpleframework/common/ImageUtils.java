package net.simpleframework.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import net.simpleframework.common.logger.Log;
import net.simpleframework.common.logger.LogFactory;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class ImageUtils {
	static Log log = LogFactory.getLogger(ImageUtils.class);

	public static final String code = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private static Random random = new Random();

	public static Color getRandColor(int fc, int bc) {
		fc = Math.min(fc, 255);
		bc = Math.min(bc, 255);
		final int r = fc + random.nextInt(bc - fc);
		final int g = fc + random.nextInt(bc - fc);
		final int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	public static String genCode(final OutputStream outputStream) throws IOException {
		return genCode(90, 38, outputStream);
	}

	public static String genCode(final int width, final int height, final OutputStream outputStream)
			throws IOException {
		final BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = createGraphics(bi);
		final GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, width, height,
				getRandColor(120, 200), false);
		g.setPaint(gp);
		g.fillRect(0, 0, width, height);
		for (int i = 0; i < 50; i++) {
			g.setColor(getRandColor(210, 220));
			g.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width),
					random.nextInt(height));
		}
		g.setColor(Color.GRAY);
		g.drawRect(0, 0, width - 1, height - 1);

		final Font font = new Font("comic sans ms", Font.BOLD, 24);
		g.setFont(font);

		final FontMetrics metrics = g.getFontMetrics();
		int stringWidth = 0;
		final char[] text = new char[4];
		final int[] textWidth = new int[4];
		for (int i = 0; i < 4; i++) {
			final int j = random.nextInt(code.length());
			text[i] = code.charAt(j);
			textWidth[i] = metrics.charWidth(text[i]);
			stringWidth += textWidth[i];
		}
		int posX = (width - stringWidth) / 2;
		final int posY = (height - metrics.getHeight()) / 2 + metrics.getAscent();
		for (int i = 0; i < text.length; i++) {
			g.setColor(getRandColor(50, 120));
			g.drawString(Convert.toString(text[i]), posX, posY);
			posX += textWidth[i];
		}
		ImageIO.write(bi, "png", outputStream);
		return Convert.toString(text);
	}

	public static void thumbnail(final InputStream inputStream, final int width, final int height,
			final OutputStream outputStream) throws IOException {
		thumbnail(inputStream, width, height, outputStream, "png");
	}

	public static void thumbnail(final InputStream inputStream, final int width, final int height,
			final OutputStream outputStream, final String filetype) throws IOException {
		thumbnail(inputStream, width, height, false, outputStream, filetype);
	}

	public static void thumbnail(final InputStream inputStream, int width, int height,
			final boolean stretch, final OutputStream outputStream, String filetype)
			throws IOException {
		int w, h;
		final BufferedImage sbi = ImageIO.read(inputStream);
		if (sbi == null) {
			IoUtils.copyStream(inputStream, outputStream);
			return;
		}
		if (width == 0) {
			width = sbi.getWidth();
		}
		if (height == 0) {
			height = sbi.getHeight();
		}

		if (!stretch) {
			final double d = (double) width / (double) height;
			final double d0 = (double) sbi.getWidth() / (double) sbi.getHeight();
			if (d < d0) {
				w = width;
				h = (int) (width / d0);
			} else {
				w = (int) (height * d0);
				h = height;
			}
		} else {
			w = width;
			h = height;
		}

		final boolean alpha = sbi.getAlphaRaster() != null;
		final BufferedImage bi = new BufferedImage(width, height, sbi.getType());
		final Graphics2D g = createGraphics(bi);
		if (w == width && h == height) {
			g.drawImage(sbi, 0, 0, w, h, null);
		} else {
			if (!alpha) {
				g.setBackground(Color.white);
				g.fillRect(0, 0, width, height);
			}
			if (w != width) {
				g.drawImage(sbi, Math.abs(w - width) / 2, 0, w, h, null);
			} else {
				g.drawImage(sbi, 0, Math.abs(h - height) / 2, w, h, null);
			}
		}
		g.dispose();
		if (filetype == null) {
			filetype = alpha ? "png" : "jpg";
		}
		ImageIO.write(bi, filetype, outputStream);
	}

	public static void thumbnail(final BufferedImage sbi, final double d,
			final OutputStream outputStream, final String filetype) throws IOException {
		final int w = (int) (sbi.getWidth() * d), h = (int) (sbi.getHeight() * d);
		final BufferedImage bi = new BufferedImage(w, h, sbi.getType());
		final Graphics2D g = createGraphics(bi);
		g.drawImage(sbi, 0, 0, w, h, null);
		g.dispose();
		ImageIO.write(bi, filetype, outputStream);
	}

	public static void thumbnail(final InputStream inputStream, final double d,
			final OutputStream outputStream, final String filetype) throws IOException {
		BufferedImage sbi;
		if (d == 1 || (sbi = ImageIO.read(inputStream)) == null) {
			IoUtils.copyStream(inputStream, outputStream);
			return;
		}
		thumbnail(sbi, d, outputStream, filetype);
	}

	public static void thumbnail(final InputStream inputStream, final double d,
			final OutputStream outputStream) throws IOException {
		thumbnail(inputStream, d, outputStream, "png");
	}

	public static void scale(final InputStream inputStream, final int width, final int height,
			final OutputStream outputStream, String filetype) throws IOException {
		final BufferedImage sbi = ImageIO.read(inputStream);
		if (sbi == null) {
			IoUtils.copyStream(inputStream, outputStream);
			return;
		}

		int w, h;
		if (width == 0 && height == 0) {
			w = sbi.getWidth();
			h = sbi.getHeight();
		} else {
			w = width;
			h = height;
			final double d0 = (double) sbi.getWidth() / (double) sbi.getHeight();
			if (w == 0) {
				w = (int) (h * d0);
			} else if (h == 0) {
				h = (int) (w / d0);
			}
		}

		final BufferedImage bi = new BufferedImage(w, h, sbi.getType());
		final Graphics2D g = createGraphics(bi);
		final boolean alpha = sbi.getAlphaRaster() != null;
		if (!alpha) {
			g.setBackground(Color.white);
			g.fillRect(0, 0, width, height);
		}
		g.drawImage(sbi, 0, 0, w, h, null);
		g.dispose();
		if (filetype == null) {
			filetype = alpha ? "png" : "jpg";
		}
		ImageIO.write(bi, filetype, outputStream);
	}

	private static Graphics2D createGraphics(final BufferedImage bi) {
		final Graphics2D g = bi.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		return g;
	}

	public static void clip(final InputStream istream, final OutputStream ostream,
			final Rectangle rect, final String suffix) throws IOException {
		try {
			final ImageInputStream iis = ImageIO.createImageInputStream(istream);
			final ImageReader reader = ImageIO.getImageReadersBySuffix(suffix).next();
			reader.setInput(iis, true);
			final ImageReadParam param = reader.getDefaultReadParam();
			param.setSourceRegion(rect);
			final BufferedImage bi = reader.read(0, param);
			ImageIO.write(bi, suffix, ostream);
		} finally {
			if (istream != null) {
				try {
					istream.close();
				} catch (final Exception e) {
				}
			}
			if (ostream != null) {
				try {
					ostream.close();
				} catch (final Exception e) {
				}
			}
		}
	}

	public static boolean isImage(final String ext) {
		return ext == null ? false : MimeTypes.getMimeType(ext).startsWith("image/");
	}

	public static boolean isImage(final File file) {
		return isImage(file, true);
	}

	public static boolean isImage(final File file, final boolean bext) {
		final String ext = FileUtils.getFilenameExtension(file.getName());
		if (bext && StringUtils.hasText(ext)) {
			return isImage(ext);
		} else {
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(file);
				return ImageIO.read(inputStream) != null;
			} catch (final IOException e) {
				return false;
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (final IOException e) {
					}
				}
			}
		}
	}
}
