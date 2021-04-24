package gui;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {
	//算法名称/加密模式/填充方式
    public static final String KEY_ALGORITHM = "AES";
    //三种填充模式 NoPadding、Zeros、PKCS5Padding
    public static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    /** 
     * 生成密钥key对象
     * @param KeyStr密钥种子
     * @return 密钥对象
     * @throws Exception
     */
    private static SecretKey keyGenerator(String keyStr) throws Exception {
        byte input[] = HexString2Bytes(keyStr);
        KeyGenerator kgen = KeyGenerator.getInstance(KEY_ALGORITHM);	//创建AES的Key生产者
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");	//kgen.init(128, new SecureRandom(password.getBytes()));无法运行
        random.setSeed(input);
        kgen.init(128, random);
        SecretKey secretKey = kgen.generateKey();	//根据用户密码，生成一个密钥
        byte[] enCodeFormat = secretKey.getEncoded();	//返回基本编码格式的密钥
        SecretKeySpec aeskey = new SecretKeySpec(enCodeFormat, KEY_ALGORITHM);	//转换为AES专用密钥
        return aeskey;
    }
 
    private static int parse(char c) {
        if (c >= 'a') return (c - 'a' + 10) & 0x0f;
        if (c >= 'A') return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }
 
    //十六进制字符串转换成字节数组
    public static byte[] HexString2Bytes(String hexstr) {
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexstr.charAt(j++);
            char c1 = hexstr.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }
    
    /**
     * AES ECB模式 加密
     * @param data 明文
     * @param key 密钥
     * @return Base64编码的密文
     * @throws Exception
     */
    public static String encrypt(String data, String key) throws Exception {
    	Key aeskey = keyGenerator(key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        //初始化Cipher对象，设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, aeskey);
        //执行加密操作，加密后的结果通过Base64编码进行传输
        Base64.Encoder encoder = Base64.getEncoder();
        //System.out.println("key:" + encoder.encodeToString(aeskey.getEncoded()));
        return encoder.encodeToString(cipher.doFinal(data.getBytes()));
    }

    /**
     * AES ECB模式 解密
     * @param data Base64编码的密文
     * @param key 密钥
     * @return 明文
     * @throws Exception
     */
    public static String decrypt(String data, String key) throws Exception {
    	Key aeskey = keyGenerator(key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        //初始化Cipher对象，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, aeskey);
        //执行解密操作，解密后的结果通过Base64编码还原
        Base64.Decoder decoder = Base64.getDecoder();
        return new String(cipher.doFinal(decoder.decode(data)));
    }
    
    /*
    public static void main(String[] args) throws Exception {
        String data = "你好 helloworld";
        String key = "A1B2C3D4E5F60708";
        String encryptData = encrypt(data, key);
        System.out.println("加密后：" + encryptData);
        String decryptData = decrypt(encryptData, key);
        System.out.println("解密后：" + decryptData);        
    }*/
}
