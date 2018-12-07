package im.status.applet_installer_test.appletinstaller;

import android.support.annotation.NonNull;
import android.util.Base64;
import im.status.keycard.globalplatform.Crypto;

import java.security.SecureRandom;

import static android.util.Base64.NO_PADDING;
import static im.status.keycard.globalplatform.Crypto.randomLong;

public class Secrets {
    private String pin;
    private String puk;
    private String pairingPassword;

    public Secrets(String pin, String puk, String pairingPassword) {
        this.pin = pin;
        this.puk = puk;
        this.pairingPassword = pairingPassword;
    }

    @NonNull
    public static Secrets generate() {
        String pairingPassword = randomToken(12);
        long pinNumber = randomLong(Crypto.PIN_BOUND);
        long pukNumber = randomLong(Crypto.PUK_BOUND);
        String pin = String.format("%06d", pinNumber);
        String puk = String.format("%012d", pukNumber);

        return new Secrets(pin, puk, pairingPassword);
    }

    public static Secrets testSecrets() {
        String pairingPassword = "KeycardTest";
        return new Secrets("000000", "123456789012", pairingPassword);
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

    public static byte[] randomBytes(int length) {
        SecureRandom random = new SecureRandom();
        byte data[] = new byte[length];
        random.nextBytes(data);

        return data;
    }

    public static String randomToken(int length) {
        return Base64.encodeToString(randomBytes(length), NO_PADDING);
    }
}