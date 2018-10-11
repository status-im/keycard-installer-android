package im.status.applet_installer_test.appletinstaller;

import android.content.res.AssetManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import im.status.applet_installer_test.appletinstaller.apducommands.*;

public class Installer {
    private Channel plainChannel;
    private Channel channel;
    private Keys cardKeys;
    private AssetManager assets;
    private String capPath;
    private boolean testSecrets;

    static final byte[] cardKeyData = HexUtils.hexStringToByteArray("404142434445464748494a4b4c4d4e4f");

    public Installer(Channel channel, AssetManager assets, String capPath, boolean testSecrets) {
        this.plainChannel = channel;
        this.channel = channel;
        this.cardKeys = new Keys(cardKeyData, cardKeyData);
        this.assets = assets;
        this.capPath = capPath;
        this.testSecrets = testSecrets;
    }

    public void start() throws IOException, APDUException, NoSuchAlgorithmException, InvalidKeySpecException {
        Logger.i("installation started");
        long startTime = System.currentTimeMillis();

        Select discover = new Select(new byte[0]);
        APDUResponse resp = this.send("discover", discover.getCommand());

        byte[] sdaid = this.getSDAID(resp.getData());
        Logger.d("sdaid: " + HexUtils.byteArrayToHexString(sdaid));

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
        byte[] appletAID = HexUtils.hexStringToByteArray("53746174757357616C6C6574417070");


        Delete deleteApplet = new Delete(appletAID);
        Logger.i("sending delete (applet)");
        this.channel.send(deleteApplet.getCommand());

        Delete deletePkg = new Delete(aid);
        Logger.i("sending delete (pkg)");
        this.channel.send(deletePkg.getCommand());


        InstallForLoad preLoad = new InstallForLoad(aid, sdaid);
        this.send("perform for load", preLoad.getCommand());


        //URL url = this.getClass().getClassLoader().getResource("wallet.cap");
        InputStream in = this.assets.open(this.capPath);
        Load load = new Load(in);

        APDUCommand loadCmd;
        while((loadCmd = load.getCommand()) != null) {
            this.send("load " + load.getCount() + "/37", loadCmd);
        }


        byte[] packageAID = HexUtils.hexStringToByteArray("53746174757357616C6C6574");
        byte[] instanceAID = HexUtils.hexStringToByteArray("53746174757357616C6C6574417070");

        InstallForInstall install = new InstallForInstall(packageAID, appletAID, instanceAID, new byte[0]);
        this.send("perform and make selectable", install.getCommand());


        installSecrets();

        long duration = System.currentTimeMillis() - startTime;
        Logger.i(String.format("installation completed in %d seconds", duration / 1000));
    }

    private void installSecrets() throws NoSuchAlgorithmException, InvalidKeySpecException, APDUException, IOException {
        Secrets secrets = testSecrets ? Secrets.testSecrets() : Secrets.generate();

        WalletAppletCommandSet cmdSet = new WalletAppletCommandSet((CardChannel) this.plainChannel);
        byte[] ecKey = cmdSet.select().checkOK().getData();
        SecureChannelSession secureChannel = new SecureChannelSession(Arrays.copyOfRange(ecKey, 2, ecKey.length));
        cmdSet.setSecureChannel(secureChannel);
        cmdSet.init(secrets.getPin(), secrets.getPuk(), secrets.getPairingToken()).checkOK();

        Logger.i(String.format("PIN: %s\nPUK: %s\nPairing password: %s\nPairing token: %s", secrets.getPin(), secrets.getPuk(), secrets.getPairingPassword(), HexUtils.byteArrayToHexString(secrets.getPairingToken())));
    }

    private APDUResponse send(String description, APDUCommand cmd) throws IOException, APDUException {
        Logger.d("sending command " + description);
        APDUResponse resp = this.channel.send(cmd);

        if(resp.getSw() == APDUResponse.SW_SECURITY_CONDITION_NOT_SATISFIED) {
            Logger.e("SW_SECURITY_CONDITION_NOT_SATISFIED: card might be blocked");
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
