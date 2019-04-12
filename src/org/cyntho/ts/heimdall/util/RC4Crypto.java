package org.cyntho.ts.heimdall.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;

public class RC4Crypto {

    private static String algorithm = "RC4";

    public static byte[] encrypt(String plain, String password) throws Exception {

        SecureRandom random = new SecureRandom(password.getBytes());
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);

        keyGenerator.init(random);
        SecretKey key = keyGenerator.generateKey();

        Cipher cipher = Cipher.getInstance(algorithm);

        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher.doFinal(plain.getBytes());
    }

    public static String decrypt(byte[] cipher, String key) throws Exception {
        SecureRandom random = new SecureRandom(key.getBytes());

        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);

        keyGenerator.init(random);

        SecretKey secretKey = keyGenerator.generateKey();

        Cipher c = Cipher.getInstance(algorithm);
        c.init(Cipher.DECRYPT_MODE, secretKey);

        return new String(c.doFinal(cipher));
    }
}
