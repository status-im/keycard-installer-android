package im.status.applet_installer_test.appletinstaller;

public class Session {
    private Keys keys;
    private byte[] cardChallenge;

    public Session(Keys keys, byte[] cardChallenge) {
        this.keys = keys;
        this.cardChallenge = cardChallenge;
    }

    public Keys getKeys() {
        return keys;
    }

    public byte[] getCardChallenge() {
        return cardChallenge;
    }
}
