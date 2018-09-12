package im.status.applet_installer_test.appletinstaller;

import android.support.annotation.NonNull;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Secrets {
    private String puk;
    private String pairingPassword;
    private byte[] pairingToken;

    public Secrets(String puk, String pairingPassword, byte[] pairingToken) {
        this.puk = puk;
        this.pairingPassword = pairingPassword;
        this.pairingToken = pairingToken;
    }

    @NonNull
    public static Secrets generate() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pairingPassword = Crypto.randomToken(12);
        byte[] pairingToken = Crypto.generatePairingKey(pairingPassword.toCharArray());
        long pukNumber = Crypto.randomLong(Crypto.PUK_BOUND);
        String puk = String.format("%012d", pukNumber);

        return new Secrets(puk, pairingPassword, pairingToken);
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
