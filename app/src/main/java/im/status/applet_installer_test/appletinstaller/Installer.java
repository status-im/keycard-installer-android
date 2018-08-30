package im.status.applet_installer_test.appletinstaller;

import java.io.IOException;
import java.security.SecureRandom;

import im.status.applet_installer_test.appletinstaller.apducommands.InitializeUpdate;

public class Installer {
    private Channel channel;

    static final byte[] cardKeyData = HexUtils.hexStringToByteArray("404142434445464748494a4b4c4d4e4f");

    private Keys cardKeys;

    public Installer(Channel channel) {
        this.channel = channel;
        this.cardKeys = new Keys(cardKeyData, cardKeyData);
    }

    public void start() throws IOException, APDUException {
        SecureRandom random = new SecureRandom();
        byte hostChallenge[] = new byte[8];
        random.nextBytes(hostChallenge);
        InitializeUpdate init = new InitializeUpdate(hostChallenge);
        byte[] data = init.getCommand().serialize();

        // get data
        //byte[] data = new byte[]{(byte) 0x80, (byte) 0xCA, 0x00, (byte) 0x66};

        byte[] respData = this.channel.transceive(data);
        APDUResponse resp = new APDUResponse(respData);
        Logger.log(respData);

        Keys keys = init.verifyResponse(this.cardKeys, resp);
    }
}
