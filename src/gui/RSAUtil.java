package gui;

import java.util.Base64;
import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class RSAUtil {
	public static final String KEY_ALGORITHM = "RSA";
	public static final String CIPHER_ALGORITHM = "RSA/None/PKCS1Padding";
	static {
		try{
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		}catch(Exception e) {
			System.out.println(e);
		}
	}
	static Base64.Encoder encoder = Base64.getEncoder();
	static Base64.Decoder decoder = Base64.getDecoder();
	private static Map<Integer, String> keyMap = new HashMap<Integer, String>();  //用于封装随机产生的公钥与私钥
	
	/*
	/**
	 * 返回公钥
	 * @param name 通信端名称 Server/Client
	 * @return 公钥
	 * @throws Exception
	 *
	public static String getPubKey(String name){
		try {
			genKeyPair(name);
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return keyMap.get(0);
	}
	/**
	 * 返回私钥
	 * @param name 通信端名称 Server/Client
	 * @return 私钥
	 * @throws Exception
	 *
	public static String getPriKey(String name){
		try {
			genKeyPair(name);
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return keyMap.get(1);
	}*/
	
	static Map<Integer, String> ServerkeyMap = genKeyPair("Server");
	static Map<Integer, String> ClientkeyMap = genKeyPair("Client");
	
	/** 
	 * 随机生成密钥对 
	 * @param name 密钥生成者
	 * @return keyMap保存的密钥对
	 * @throws NoSuchAlgorithmException 
	 */  
	public static Map<Integer, String> genKeyPair(String name) {  
		//KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象  
		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
			//初始化密钥对生成器，密钥大小为96-1024位 
			keyPairGen.initialize(1024,new SecureRandom(name.getBytes())); 
			//生成一个密钥对，保存在keyPair中  
			KeyPair keyPair = keyPairGen.generateKeyPair();  
			RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   //得到私钥  
			RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  //得到公钥  
			String publicKeyString = new String(encoder.encodeToString(publicKey.getEncoded()));  
			//得到私钥字符串  
			String privateKeyString = new String(encoder.encodeToString((privateKey.getEncoded())));  
			//将公钥和私钥保存到Map
			keyMap.put(0,publicKeyString);  //0表示公钥
			keyMap.put(1,privateKeyString);  //1表示私钥
		} catch (NoSuchAlgorithmException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} 
		return keyMap;
	}
	
	/** 
	 * RSA公钥加密   
	 * @param str 需要加密的字符串
	 * @param publicKey 公钥 
	 * @return 密文 
	 * @throws Exception
	 */  
	public static String encrypt(String str, String publicKey) throws Exception{
		//base64编码的公钥
		byte[] decoded = decoder.decode(publicKey);
		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(decoded));
		//RSA加密
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		String outStr = encoder.encodeToString(cipher.doFinal(str.getBytes("UTF-8")));
		return outStr;
	}

	/** 
	 * RSA私钥解密
	 * @param str 需要解密的字符串
	 * @param privateKey 私钥 
	 * @return 明文
	 * @throws Exception
	 */  
	public static String decrypt(String str, String privateKey) throws Exception{
		//64位解码加密后的字符串
		byte[] inputByte = decoder.decode(str.getBytes("UTF-8"));
		//base64编码的私钥
		byte[] decoded = decoder.decode(privateKey);  
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(decoded));  
		//RSA解密
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, priKey);
		String outStr = new String(cipher.doFinal(inputByte));
		return outStr;
	}
	
	/** 
	 * 签名
	 * @param str 需要签名的明文
	 * @param privateKey 签名者的私钥
	 * @return 签名
	 * @throws Exception
	 */
	public static byte[] sign(String str, String privateKey) {
		try {
			Signature signature = Signature.getInstance("MD5withRSA");
	        signature.initSign(String2PriKey(privateKey));
	        signature.update(str.getBytes());
	        byte[] result = signature.sign();
			return result;
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
		}
		return null;
	}
	
	/** 
	 * 验证
	 * @param str 解密后的明文
	 * @param publicKey 验证者的公钥
	 * @param result 签名
	 * @throws Exception
	 */
	public static void verify(String str, String publicKey, byte[] result) {
		try {
			Signature signature = Signature.getInstance("MD5withRSA");
	        signature.initVerify(String2PubKey(publicKey));
	        signature.update(str.getBytes());
			boolean bool = signature.verify(result);
			System.out.println("签名验证结果：" + bool + "\n");
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
		}
	}
	
	public static PublicKey String2PubKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = decoder.decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }
	
	public static PrivateKey String2PriKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = decoder.decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }
	
	/*
	public static void main(String[] args) throws Exception {
		//生成公钥和私钥
		String name = "Server";
		//String name = "Client";
		genKeyPair(name);
		//加密字符串
		String message = "1787085EE71C3865";
		System.out.println("随机生成的公钥为:" + keyMap.get(0));
		System.out.println("随机生成的私钥为:" + keyMap.get(1));
		String messageEn = encrypt(message,keyMap.get(0));
		System.out.println(message + "加密后的字符串为:" + messageEn);
		String messageDe = decrypt(messageEn,keyMap.get(1));
		System.out.println("还原后的字符串为:" + messageDe);
	}*/
	
}
