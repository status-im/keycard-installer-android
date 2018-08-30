package im.status.applet_installer_test.appletinstaller;

import java.io.IOException;
import java.security.SecureRandom;

import im.status.applet_installer_test.appletinstaller.apducommands.ExternalAuthenticate;
import im.status.applet_installer_test.appletinstaller.apducommands.InitializeUpdate;
import im.status.applet_installer_test.appletinstaller.apducommands.Status;

public class Installer {
    private Channel channel;

    static final byte[] cardKeyData = HexUtils.hexStringToByteArray("404142434445464748494a4b4c4d4e4f");

    private Keys cardKeys;

    public Installer(Channel channel) {
        this.channel = channel;
        this.cardKeys = new Keys(cardKeyData, cardKeyData);
    }

    public void start() throws IOException, APDUException {
        byte[] hostChallenge = InitializeUpdate.generateChallenge();
        InitializeUpdate init = new InitializeUpdate(hostChallenge);
        APDUResponse resp = this.channel.send(init.getCommand());

        Session session = init.verifyResponse(this.cardKeys, resp);
        Keys sessionKeys = session.getKeys();

        this.channel = new SecureChannel(this.channel, sessionKeys);

        ExternalAuthenticate auth = new ExternalAuthenticate(sessionKeys.getEncKeyData(), session.getCardChallenge(), hostChallenge);
        resp = this.channel.send(auth.getCommand());
        if (!auth.checkResponse(resp)) {
            throw new APDUException(resp.getSw(), "bad external authenticate response");
        }

        Status status = new Status(Status.P1_EXECUTABLE_LOAD_FILES_AND_MODULES);
        resp = this.channel.send(status.getCommand());
    }
}
