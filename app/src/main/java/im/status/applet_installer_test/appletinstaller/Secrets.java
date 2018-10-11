package im.status.applet_installer_test.appletinstaller;

import android.support.annotation.NonNull;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Secrets {
    private String pin;
    private String puk;
    private String pairingPassword;
    private byte[] pairingToken;

    public Secrets(String pin, String puk, String pairingPassword, byte[] pairingToken) {
        this.pin = pin;
        this.puk = puk;
        this.pairingPassword = pairingPassword;
        this.pairingToken = pairingToken;
    }

    @NonNull
    public static Secrets generate() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pairingPassword = Crypto.randomToken(12);
        byte[] pairingToken = Crypto.generatePairingKey(pairingPassword.toCharArray());
        long pinNumber = Crypto.randomLong(Crypto.PIN_BOUND);
        long pukNumber = Crypto.randomLong(Crypto.PUK_BOUND);
        String pin = String.format("%06d", pinNumber);
        String puk = String.format("%012d", pukNumber);

        return new Secrets(pin, puk, pairingPassword, pairingToken);
    }

    public static Secrets testSecrets() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pairingPassword = "WalletAppletTest";
        byte[] pairingToken = Crypto.generatePairingKey(pairingPassword.toCharArray());
        return new Secrets("000000", "123456789012", pairingPassword, pairingToken);
    }

    public String getPin() {
        return pin;
    }

    public String getPuk() {
        return puk;
    }

    public String getPairingPassword() {
        return pairingPassword;
    }

    public byte[] getPairingToken() {
        return pairingToken;
    }
}
