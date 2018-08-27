package im.status.applet_installer_test.appletinstaller;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Crypto {
    public static byte[] deriveKey(byte[] cardKey, byte[] seq, byte[] purposeData) {
        byte[] key24 = new byte[24];
        System.arraycopy(cardKey, 0, key24, 0, 16);
        System.arraycopy(cardKey, 0, key24, 16, 8);

        try {
            byte[] derivationData = new byte[16];
            // 2 bytes constant
            System.arraycopy(purposeData, 0, derivationData, 0, 2);
            // 2 bytes sequence counter + 12 bytes 0x00
            System.arraycopy(seq, 0, derivationData, 2, 2);

            Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
            IvParameterSpec iv = new IvParameterSpec(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
            SecretKeySpec tmpKey = new SecretKeySpec(key24, "DESede");
            cipher.init(Cipher.ENCRYPT_MODE, tmpKey, iv);

            return cipher.doFinal(derivationData);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException("error generating session keys.", e);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("error generating session keys.", e);
        }
    }

    public static byte[] appendDESPadding(byte[] data) {
        int length = data.length + 1;
        for (; length % 8 != 0; length++){}
        byte[] newData = new byte[length];
        System.arraycopy(data, 0, newData, 0, data.length);
        newData[data.length] = (byte)0x80;

        return newData;
    }
}
