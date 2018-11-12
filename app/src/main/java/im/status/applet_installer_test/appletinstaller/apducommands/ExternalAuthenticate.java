package im.status.applet_installer_test.appletinstaller.apducommands;

import im.status.applet_installer_test.appletinstaller.Crypto;
import im.status.hardwallet_lite_android.io.APDUCommand;
import im.status.hardwallet_lite_android.io.APDUResponse;

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

    public APDUCommand getCommand() {
        return new APDUCommand(CLA, INS, P1, P2, this.getHostCryptogram());
    }

    public byte[] getHostCryptogram() {
        byte[] data = new byte[this.cardChallenge.length + this.hostChallenge.length];
        System.arraycopy(cardChallenge, 0, data, 0, cardChallenge.length);
        System.arraycopy(hostChallenge, 0, data, cardChallenge.length, hostChallenge.length);
        byte[] paddedData = Crypto.appendDESPadding(data);

        return Crypto.mac3des(this.encKeyData, paddedData, Crypto.NullBytes8);
    }

    public boolean checkResponse(APDUResponse resp) {
        return resp.getSw() == APDUResponse.SW_OK;
    }
}
