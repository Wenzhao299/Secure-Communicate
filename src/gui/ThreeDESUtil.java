package gui;

import java.security.Key;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class ThreeDESUtil {
    //算法名称
    public static final String KEY_ALGORITHM = "DESede";
    //算法名称/加密模式/填充方式
    public static final String CIPHER_ALGORITHM = "DESede/CBC/PKCS5Padding";

    /**
     * 生成密钥key对象
     * @param KeyStr 密钥字符串
     * @return 密钥对象
     * @throws Exception  
     */
    private static Key keyGenerator(String keyStr) throws Exception {
        byte input[] = HexString2Bytes(keyStr);
        DESedeKeySpec KeySpec = new DESedeKeySpec(input);
        SecretKeyFactory KeyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        return ((Key) (KeyFactory.generateSecret(((java.security.spec.KeySpec)(KeySpec)))));
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
     * 3DES CBC模式 加密
     * @param data 明文
     * @param keyiv IV
     * @param key 密钥
     * @return Base64编码的密文
     * @throws Exception
     */
    public static String encrypt(String data, byte[] keyiv, String key) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Key deskey = keyGenerator(key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        IvParameterSpec ips = new IvParameterSpec(keyiv);
        cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
        Base64.Encoder encoder = Base64.getEncoder();
        //System.out.println("key:" + encoder.encodeToString(deskey.getEncoded()));
        return encoder.encodeToString(cipher.doFinal(data.getBytes()));
    }
 
    /**
     * 3DES CBC模式 解密
     * @param data Base64编码的密文
     * @param keyiv IV
     * @param key 密钥
     * @return 明文
     * @throws Exception
     */
    public static String decrypt(String data, byte[] keyiv, String key) throws Exception {
        Key deskey = keyGenerator(key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        IvParameterSpec ips = new IvParameterSpec(keyiv);
        cipher.init(Cipher.DECRYPT_MODE, deskey, ips);
        Base64.Decoder decoder = Base64.getDecoder();
        return new String(cipher.doFinal(decoder.decode(data)));
    }
    
    /*
    public static void main(String[] args) throws Exception {
        String key = "A1B2C3D4E5F60708A1B2C3D4E5F60708A1B2C3D4E5F60708";
        byte[] keyiv = { 1, 2, 3, 4, 5, 6, 7, 8 };
        String data = "你好 helloworld";
        String encryptData = encrypt(data, keyiv, key);
        System.out.println("加密后：" + encryptData);
        String decryptData = decrypt(encryptData, keyiv, key);
        System.out.println("解密后：" + decryptData);
    }*/
}