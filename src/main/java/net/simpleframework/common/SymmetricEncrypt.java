package net.simpleframework.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;

public class SymmetricEncrypt {
	private final String algorithm;

	private Key key;

	public SymmetricEncrypt(final String str) {
		this(str, "DES");
	}

	public SymmetricEncrypt(final String str, final String algorithm) {
		this.algorithm = algorithm;
		// 生成密匙
		try {
			KeyGenerator _generator = KeyGenerator.getInstance(algorithm);
			final SecureRandom sRandom = SecureRandom.getInstance("SHA1PRNG");
			sRandom.setSeed(str.getBytes());
			_generator.init(sRandom);
			this.key = _generator.generateKey();
			_generator = null;
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException("Cause: " + e);
		}
	}

	public Key getKey() {
		return key;
	}

	public void setKey(final Key key) {
		this.key = key;
	}

	private byte[] encryptByte(final byte[] byteS) {
		byte[] byteFina = null;
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byteFina = cipher.doFinal(byteS);
		} catch (final Exception e) {
			throw new RuntimeException("Cause: " + e);
		} finally {
			cipher = null;
		}
		return byteFina;
	}

	private byte[] decryptByte(final byte[] byteD) {
		Cipher cipher;
		byte[] byteFina = null;
		try {
			cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, key);
			byteFina = cipher.doFinal(byteD);
		} catch (final Exception e) {
			throw new RuntimeException("Cause: " + e);
		} finally {
			cipher = null;
		}
		return byteFina;
	}

	public String encrypt(final String strMing) {
		return Base64.encodeToString(encryptByte(strMing.getBytes()));
	}

	public String decrypt(final String strMi) {
		try {
			return new String(decryptByte(Base64.decode(strMi)), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("Cause: " + e);
		}
	}

	public void encryptFile(final String file, final String destFile) throws Exception {
		final Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, this.key);
		final InputStream is = new FileInputStream(file);
		final OutputStream out = new FileOutputStream(destFile);
		final CipherInputStream cis = new CipherInputStream(is, cipher);
		try {
			final byte[] buffer = new byte[1024];
			int r;
			while ((r = cis.read(buffer)) > 0) {
				out.write(buffer, 0, r);
			}
		} finally {
			cis.close();
			is.close();
			out.close();
		}
	}

	public void decryptFile(final String file, final String dest) throws Exception {
		final Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, this.key);
		final InputStream is = new FileInputStream(file);
		final OutputStream out = new FileOutputStream(dest);
		final CipherOutputStream cos = new CipherOutputStream(out, cipher);
		try {
			final byte[] buffer = new byte[1024];
			int r;
			while ((r = is.read(buffer)) >= 0) {
				cos.write(buffer, 0, r);
			}
		} finally {
			cos.close();
			out.close();
			is.close();
		}
	}
}
