package im.status.applet_installer_test.appletinstaller;

import android.content.res.AssetManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import im.status.applet_installer_test.appletinstaller.apducommands.ExternalAuthenticate;
import im.status.applet_installer_test.appletinstaller.apducommands.InitializeUpdate;
import im.status.applet_installer_test.appletinstaller.apducommands.InstallForInstall;
import im.status.applet_installer_test.appletinstaller.apducommands.InstallForLoad;
import im.status.applet_installer_test.appletinstaller.apducommands.Load;
import im.status.applet_installer_test.appletinstaller.apducommands.Select;

public class Installer {
    private Channel channel;
    private Keys cardKeys;
    private AssetManager assets;
    private String capPath;

    static final byte[] cardKeyData = HexUtils.hexStringToByteArray("404142434445464748494a4b4c4d4e4f");

    public Installer(Channel channel, AssetManager assets, String capPath) {
        this.channel = channel;
        this.cardKeys = new Keys(cardKeyData, cardKeyData);
        this.assets = assets;
        this.capPath = capPath;
    }

    public void start() throws IOException, APDUException, NoSuchAlgorithmException, InvalidKeySpecException {
        long startTime = System.currentTimeMillis();

        Select discover = new Select(new byte[0]);
        APDUResponse resp = this.send("discover", discover.getCommand());

        byte[] sdaid = this.getSDAID(resp.getData());
        Logger.log("sdaid: " + HexUtils.byteArrayToHexString(sdaid));

        byte[] hostChallenge = InitializeUpdate.generateChallenge();
        InitializeUpdate init = new InitializeUpdate(hostChallenge);
        resp = this.send("init update", init.getCommand());

        Session session = init.verifyResponse(this.cardKeys, resp);
        Keys sessionKeys = session.getKeys();

        this.channel = new SecureChannel(this.channel, sessionKeys);

        ExternalAuthenticate auth = new ExternalAuthenticate(sessionKeys.getEncKeyData(), session.getCardChallenge(), hostChallenge);
        resp = this.send("external auth", auth.getCommand());
        if (!auth.checkResponse(resp)) {
            throw new APDUException(resp.getSw(), "bad external authenticate response");
        }

        //Status status = new Status(Status.P1_EXECUTABLE_LOAD_FILES_AND_MODULES);
        //resp = this.send("status", status.getCommand());

        byte[] aid = HexUtils.hexStringToByteArray("53746174757357616C6C6574");

        //Delete delete = new Delete(aid);
        //Logger.log("sending command delete");
        //this.channel.send(delete.getCommand());


        InstallForLoad preLoad = new InstallForLoad(aid, sdaid);
        this.send("perform for load", preLoad.getCommand());


        //URL url = this.getClass().getClassLoader().getResource("wallet.cap");
        InputStream in = this.assets.open(this.capPath);
        Load load = new Load(in);

        APDUCommand loadCmd;
        while((loadCmd = load.getCommand()) != null) {
            this.send("load " + load.getCount() + "/31", loadCmd);
        }


        byte[] packageAID = HexUtils.hexStringToByteArray("53746174757357616C6C6574");
        byte[] appletAID = HexUtils.hexStringToByteArray("53746174757357616C6C6574417070");
        byte[] instanceAID = HexUtils.hexStringToByteArray("53746174757357616C6C6574417070");

        Secrets secrets = Secrets.generate();
        ByteArrayOutputStream params = new ByteArrayOutputStream();
        params.write(secrets.getPuk().getBytes());
        params.write(secrets.getPairingToken());

        InstallForInstall install = new InstallForInstall(packageAID, appletAID, instanceAID, params.toByteArray());
        this.send("perform and make selectable", install.getCommand());

        Logger.log(String.format("PUK: %s\nPairing password: %s\nPairing token: %s", secrets.getPuk(), secrets.getPairingPassword(), HexUtils.byteArrayToHexString(secrets.getPairingToken())));

        long duration = System.currentTimeMillis() - startTime;
        Logger.log(String.format("installation completed in %d seconds", duration / 1000));
    }

    private APDUResponse send(String description, APDUCommand cmd) throws IOException, APDUException {
        Logger.log("sending command " + description);
        APDUResponse resp = this.channel.send(cmd);

        if(resp.getSw() == APDUResponse.SW_SECURITY_CONDITION_NOT_SATISFIED) {
            Logger.log("SW_SECURITY_CONDITION_NOT_SATISFIED: card might be blocked");
            throw new APDUException(resp.getSw(), "security confition not satisfied. card might be blocked " + description);
        }

        if (!resp.isOK()) {
            throw new APDUException(resp.getSw(), "bad response for command " + description);
        }

        return resp;
    }

    private byte[] getSDAID(byte[] data) throws IOException, APDUException {
        Tlv tlv = new Tlv(data);
        tlv = tlv.find((byte) 0x6F);
        if (tlv == null) {
            throw new APDUException("error searching for tag 0x6F in discover response");
        }

        tlv = tlv.find((byte) 0x84);
        if (tlv == null) {
            throw new APDUException("error searching for tag 0x84 in discover response");
        }

        return tlv.getValue();
    }
}
