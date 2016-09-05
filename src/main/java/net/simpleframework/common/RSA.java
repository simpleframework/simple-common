package net.simpleframework.common;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

public class RSA {

	/* 加密算法RSA */
	public static final String KEY_ALGORITHM = "RSA";

	/* 获取公钥的key */
	private static final String PUBLIC_KEY = "RSAPublicKey";

	/* 获取私钥的key */
	private static final String PRIVATE_KEY = "RSAPrivateKey";

	/* RSA最大加密明文大小 */
	private static final int MAX_ENCRYPT_BLOCK = 117;

	/* RSA最大解密密文大小 */
	private static final int MAX_DECRYPT_BLOCK = 128;

	/**
	 * 生成密钥对(公钥和私钥)
	 *
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> genKeyPair() throws Exception {
		final KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyPairGen.initialize(1024);
		final KeyPair keyPair = keyPairGen.generateKeyPair();
		final RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		final RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		final Map<String, Object> keyMap = new HashMap<String, Object>(2);
		keyMap.put(PUBLIC_KEY, publicKey);
		keyMap.put(PRIVATE_KEY, privateKey);
		return keyMap;
	}

	/* 签名算法 */
	public static final String SIGNATURE_ALGORITHM = "MD5withRSA"; // "SHA1WithRSA";

	/**
	 * 用私钥对信息生成数字签名
	 *
	 * @param data
	 *        已加密数据
	 * @param privateKey
	 *        私钥(BASE64编码)
	 * @param algorithm
	 *        算法
	 *
	 * @return
	 * @throws Exception
	 */
	public static String sign(final byte[] data, final String privateKey, final String algorithm)
			throws Exception {
		final byte[] keyBytes = Base64.decode(privateKey);
		final PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		final KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		final PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
		final Signature signature = Signature.getInstance(algorithm);
		signature.initSign(privateK);
		signature.update(data);
		return Base64.encodeToString(signature.sign());
	}

	public static String sign(final byte[] data, final String privateKey) throws Exception {
		return sign(data, privateKey, SIGNATURE_ALGORITHM);
	}

	/**
	 * 校验数字签名
	 *
	 * @param data
	 *        已加密数据
	 * @param publicKey
	 *        公钥(BASE64编码)
	 * @param sign
	 *        数字签名
	 * @param algorithm
	 *        算法
	 *
	 * @return
	 * @throws Exception
	 *
	 */
	public static boolean verify(final byte[] data, final String publicKey, final String sign,
			final String algorithm) throws Exception {
		final byte[] keyBytes = Base64.decode(publicKey);
		final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		final KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		final PublicKey publicK = keyFactory.generatePublic(keySpec);
		final Signature signature = Signature.getInstance(algorithm);
		signature.initVerify(publicK);
		signature.update(data);
		return signature.verify(Base64.decode(sign));
	}

	public static boolean verify(final byte[] data, final String publicKey, final String sign)
			throws Exception {
		return verify(data, publicKey, sign, SIGNATURE_ALGORITHM);
	}

	/**
	 * 私钥解密
	 *
	 * @param encryptedData
	 *        已加密数据
	 * @param privateKey
	 *        私钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(final byte[] encryptedData, final String privateKey)
			throws Exception {
		final byte[] keyBytes = Base64.decode(privateKey);
		final PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		final KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		final Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
		final Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateK);
		final int inputLen = encryptedData.length;
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		final byte[] decryptedData = out.toByteArray();
		out.close();
		return decryptedData;
	}

	/**
	 * 公钥解密
	 *
	 * @param encryptedData
	 *        已加密数据
	 * @param publicKey
	 *        公钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPublicKey(final byte[] encryptedData, final String publicKey)
			throws Exception {
		final byte[] keyBytes = Base64.decode(publicKey);
		final X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		final KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		final Key publicK = keyFactory.generatePublic(x509KeySpec);
		final Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, publicK);
		final int inputLen = encryptedData.length;
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		final byte[] decryptedData = out.toByteArray();
		out.close();
		return decryptedData;
	}

	/**
	 * 公钥加密
	 *
	 * @param data
	 *        源数据
	 * @param publicKey
	 *        公钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPublicKey(final byte[] data, final String publicKey)
			throws Exception {
		final byte[] keyBytes = Base64.decode(publicKey);
		final X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		final KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		final Key publicK = keyFactory.generatePublic(x509KeySpec);
		// 对数据加密
		final Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicK);
		final int inputLen = data.length;
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		final byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}

	/**
	 * 私钥加密
	 *
	 * @param data
	 *        源数据
	 * @param privateKey
	 *        私钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPrivateKey(final byte[] data, final String privateKey)
			throws Exception {
		final byte[] keyBytes = Base64.decode(privateKey);
		final PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		final KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		final Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
		final Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateK);
		final int inputLen = data.length;
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		final byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}

	/**
	 * 获取私钥
	 *
	 * @param keyMap
	 *        密钥对
	 * @return
	 * @throws Exception
	 */
	public static String getPrivateKey(final Map<String, Object> keyMap) throws Exception {
		final Key key = (Key) keyMap.get(PRIVATE_KEY);
		return Base64.encodeToString(key.getEncoded());
	}

	/**
	 * 获取公钥
	 *
	 * @param keyMap
	 *        密钥对
	 * @return
	 * @throws Exception
	 */
	public static String getPublicKey(final Map<String, Object> keyMap) throws Exception {
		final Key key = (Key) keyMap.get(PUBLIC_KEY);
		return Base64.encodeToString(key.getEncoded());
	}
}
