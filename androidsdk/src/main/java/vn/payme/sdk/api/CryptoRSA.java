package vn.payme.sdk.api;

import android.os.Build;
import android.util.Base64;
import androidx.annotation.RequiresApi;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.*;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import vn.payme.sdk.PayME;

class CryptoRSA {
    PublicKey publicKey;
    PrivateKey privateKey;
    byte[] encryptedBytes, decryptedBytes;
    Cipher cipher, cipher1;
    String decrypted;

    private final static String CRYPTO_METHOD = "RSA";
    private final static int CRYPTO_BITS = 512;
    private static final String PUBLIC_KEY_BASE64_ENCODED = PayME.publicKey;

    private static final String PRIVATE_KEY_BASE64_ENCODED_1 = PayME.appPrivateKey;

    public CryptoRSA() throws NoSuchAlgorithmException, InvalidKeySpecException {
            generateKeyPair();

    }

    private void generateKeyPair()
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        privateKey = stringToPrivateKey(PRIVATE_KEY_BASE64_ENCODED_1);
        publicKey =stringToPublicKey(PUBLIC_KEY_BASE64_ENCODED);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String encrypt(Object... args)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {

        String plain = (String) args[0];
        PublicKey rsaPublicKey;
        if (args.length == 1) {
            rsaPublicKey = this.publicKey;
        } else {
            rsaPublicKey = (PublicKey) args[1];
        }
        cipher =  Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        encryptedBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
    }

    public String decrypt(String result,PrivateKey... args)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {
        PrivateKey rsaPrivateKey;
        if (args.length == 1) {
            rsaPrivateKey = args[0];
        } else {
            rsaPrivateKey = this.privateKey;
        }
        cipher1 =  Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher1.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        byte[] decoded = Base64.decode(result,Base64.DEFAULT);
        decryptedBytes = cipher1.doFinal(decoded);
        decrypted = new String(decryptedBytes);

        return decrypted;
    }

    public static PrivateKey stringToPrivateKey(String privateKeyPEM) throws NoSuchAlgorithmException, InvalidKeySpecException {
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "");
        privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
        byte[] encoded = Base64.decode(privateKeyPEM,Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return kf.generatePrivate(keySpec);
    }
    public static PublicKey stringToPublicKey(String privateKeyPEM) throws NoSuchAlgorithmException, InvalidKeySpecException {
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "");
        privateKeyPEM = privateKeyPEM.replace("-----END PUBLIC KEY-----", "");
        byte[] encoded = Base64.decode(privateKeyPEM,Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        return pubKey;
    }

    static PublicKey getPublicKey(PrivateKey privKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPrivateKeySpec priv = kf.getKeySpec(privKey, RSAPrivateKeySpec.class);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(priv.getModulus(), BigInteger.valueOf(65537));
        return kf.generatePublic(keySpec);
    }
}