package im.status.applet_installer_test.appletinstaller.apducommands;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import im.status.applet_installer_test.appletinstaller.APDUCommand;
import im.status.applet_installer_test.appletinstaller.APDUException;
import im.status.applet_installer_test.appletinstaller.APDUResponse;
import im.status.applet_installer_test.appletinstaller.HexUtils;

public class InitializeUpdate {
    public static int CLA = 0x80;
    public static int INS = 0x50;
    public static int P1 = 0;
    public static int P2 = 0;

    public static byte[] DERIVATION_PURPOSE_ENC = new byte[]{(byte) 0x01, (byte) 0x82};
    public static byte[] DERIVATION_PURPOSE_MAC = new byte[]{(byte) 0x01, (byte) 0x01};
    public static byte[] DERIVATION_PURPOSE_DEK = new byte[]{(byte) 0x01, (byte) 0x81};

    private byte[] challenge;

    public InitializeUpdate(byte[] challenge) {
        this.challenge = challenge;
    }

    public APDUCommand getCommand() {
        return new APDUCommand(CLA, INS, P1, P2, this.challenge);
    }

    public static byte[] generateChallenge() {
        SecureRandom random = new SecureRandom();
        byte challenge[] = new byte[8];
        random.nextBytes(challenge);

        return challenge;
    }

    public void validateResponse(APDUResponse resp) throws APDUException {
        if (resp.getSw() == APDUResponse.SW_SECURITY_CONDITION_NOT_SATISFIED) {
            throw new APDUException(resp.getSw(), "security confition not satisfied");
        }

        if (resp.getSw() == APDUResponse.SW_AUTHENTICATION_METHOD_BLOCKED) {
            throw new APDUException(resp.getSw(), "authentication method blocked");
        }

        byte[] data = resp.getData();

        if (data.length != 28) {
            throw new APDUException(resp.getSw(), String.format("bad data length, expected 28, got %d", data.length));
        }

        byte[] diversificationdData = new byte[10];
        System.arraycopy(data, 0, diversificationdData, 0, 10);

        byte[] cardChallenge = new byte[8];
        System.arraycopy(data, 12, cardChallenge, 0, 8);

        byte[] ssc = new byte[2];
        System.arraycopy(data, 12, ssc, 0, 2);

        byte[] cardCryptogram = new byte[8];
        System.arraycopy(data, 20, cardCryptogram, 0, 8);

        System.out.printf("diversification: %s, %n", HexUtils.byteArrayToHexString(diversificationdData));
        System.out.printf("cardChallege: %s, %n", HexUtils.byteArrayToHexString(cardChallenge));
        System.out.printf("ssc: %s, %n", HexUtils.byteArrayToHexString(ssc));
        System.out.printf("cardCryptogram: %s, %n", HexUtils.byteArrayToHexString(cardCryptogram));


        //System.out.printf("key data: %s, %n", HexUtils.byteArrayToHexString(keyData));

    }

    public byte[] deriveKey(byte[] cardKey, byte[] seq, byte[] purposeData) {
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

            byte[] keyData = cipher.doFinal(derivationData);

            return keyData;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException("error generating session keys.", e);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("error generating session keys.", e);
        }
    }
}
