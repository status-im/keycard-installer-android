package im.status.applet_installer_test.appletinstaller;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Crypto {

    public static final byte[] NullBytes8 = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public static byte[] deriveKey(byte[] cardKey, byte[] seq, byte[] purposeData) {
        byte[] key24 = resizeKey24(cardKey);

        try {
            byte[] derivationData = new byte[16];
            // 2 bytes constant
            System.arraycopy(purposeData, 0, derivationData, 0, 2);
            // 2 bytes sequence counter + 12 bytes 0x00
            System.arraycopy(seq, 0, derivationData, 2, 2);

            SecretKeySpec tmpKey = new SecretKeySpec(key24, "DESede");

            Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, tmpKey, new IvParameterSpec(NullBytes8));

            return cipher.doFinal(derivationData);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException("error generating session keys.", e);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("error generating session keys.", e);
        }
    }

    public static byte[] appendDESPadding(byte[] data) {
        int length = data.length + 1;
        for (; length % 8 != 0; length++){
        }
        byte[] newData = new byte[length];
        System.arraycopy(data, 0, newData, 0, data.length);
        newData[data.length] = (byte)0x80;

        return newData;
    }

    public static boolean verifyCryptogram(byte[] key, byte[] hostChallenge, byte[] cardChallenge, byte[] cardCryptogram) {
        byte[] data = new byte[hostChallenge.length + cardChallenge.length];
        System.arraycopy(hostChallenge, 0, data, 0, hostChallenge.length);
        System.arraycopy(cardChallenge, 0, data, hostChallenge.length, cardChallenge.length);
        byte[] paddedData = appendDESPadding(data);
        byte[] calculated = mac3des(key, paddedData, NullBytes8);

        return Arrays.equals(calculated , cardCryptogram);
    }

    public static byte[] mac3des(byte[] keyData, byte[] data, byte[] iv) {
        try {
            SecretKeySpec key = new SecretKeySpec(resizeKey24(keyData), "DESede");
            Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] result = cipher.doFinal(data, 0, 24);
            byte[] tail = new byte[8];
            System.arraycopy(result, 16, tail, 0, 8);
            return tail;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("error calculating mac.", e);
        }
    }

    public static byte[] macFull3des(byte[] keyData, byte[] data, byte[] iv) {
        try {
            SecretKeySpec keyDes = new SecretKeySpec(resizeKey8(keyData), "DES");
            Cipher cipherDes = Cipher.getInstance("DES/CBC/NoPadding");
            cipherDes.init(Cipher.ENCRYPT_MODE, keyDes, new IvParameterSpec(iv));

            SecretKeySpec keyDes3 = new SecretKeySpec(resizeKey24(keyData), "DESede");
            Cipher cipherDes3 = Cipher.getInstance("DESede/CBC/NoPadding");
            byte[] des3Iv = iv.clone();

            if (data.length > 8) {
                byte[] tmp = cipherDes.doFinal(data, 0, data.length - 8);
                System.arraycopy(tmp, tmp.length - 8, des3Iv, 0, 8);
            }

            cipherDes3.init(Cipher.ENCRYPT_MODE, keyDes3, new IvParameterSpec(des3Iv));
            byte[] result = cipherDes3.doFinal(data, data.length - 8, 8);
            byte[] tail = new byte[8];
            System.arraycopy(result, result.length - 8, tail, 0, 8);
            return tail;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("error generating full triple DES MAC.", e);
        }
    }

    public static byte[] resizeKey24(byte[] keyData) {
        byte[] key = new byte[24];
        System.arraycopy(keyData, 0, key, 0, 16);
        System.arraycopy(keyData, 0, key, 16, 8);

        return key;
    }

    public static byte[] resizeKey8(byte[] keyData) {
        byte[] key = new byte[8];
        System.arraycopy(keyData, 0, key, 0, 8);

        return key;
    }
}
