package im.status.applet_installer_test.appletinstaller.apducommands;

import im.status.applet_installer_test.appletinstaller.Crypto;

public class ExternalAuthenticate {
    public static int CLA = 0x84;
    public static int INS = 0x82;
    public static int P1 = 0x01;
    public static int P2 = 0x00;

    private byte[] encKeyData;
    private byte[] cardChallenge;
    private byte[] hostChallenge;

    public ExternalAuthenticate(byte[] encKeyData, byte[] cardChallenge, byte[] hostChallenge) {
        this.encKeyData = encKeyData;
        this.cardChallenge = cardChallenge;
        this.hostChallenge = hostChallenge;
    }

    public byte[] getHostCryptogram() {
        byte[] data = new byte[this.cardChallenge.length + this.hostChallenge.length];
        System.arraycopy(cardChallenge, 0, data, 0, cardChallenge.length);
        System.arraycopy(hostChallenge, 0, data, cardChallenge.length, hostChallenge.length);
        byte[] paddedData = Crypto.appendDESPadding(data);

        return Crypto.mac3des(this.encKeyData, paddedData, Crypto.NullBytes8);
    }
}
