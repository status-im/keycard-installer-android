package im.status.applet_installer_test.appletinstaller;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import im.status.hardwallet_lite_android.globalplatform.Load;
import im.status.hardwallet_lite_android.io.APDUException;
import im.status.hardwallet_lite_android.io.APDUResponse;
import im.status.hardwallet_lite_android.io.CardChannel;
import im.status.hardwallet_lite_android.globalplatform.ApplicationID;
import im.status.hardwallet_lite_android.globalplatform.GlobalPlatformCommandSet;
import im.status.hardwallet_lite_android.wallet.WalletAppletCommandSet;

public class Installer {
    private CardChannel plainChannel;
    private AssetManager assets;
    private String capPath;

    static final byte[] PACKAGE_AID = HexUtils.hexStringToByteArray("53746174757357616C6C6574");
    static final byte[] WALLET_AID = HexUtils.hexStringToByteArray("53746174757357616C6C6574417070");
    static final byte[] NDEF_APPLET_AID = HexUtils.hexStringToByteArray("53746174757357616C6C65744E4643");
    static final byte[] NDEF_INSTANCE_AID = HexUtils.hexStringToByteArray("D2760000850101");

    private GlobalPlatformCommandSet cmdSet;

    private boolean testSecrets;

    public Installer(CardChannel channel, AssetManager assets, String capPath, boolean testSecrets) {
        this.plainChannel = channel;
        this.assets = assets;
        this.capPath = capPath;
        this.testSecrets = testSecrets;
    }

    public void start() throws IOException, APDUException, NoSuchAlgorithmException, InvalidKeySpecException {
        Logger.i("installation started...");
        long startTime = System.currentTimeMillis();

        Logger.i("auto select sdaid...");
        cmdSet = new GlobalPlatformCommandSet(this.plainChannel);
        ApplicationID sdaid = new ApplicationID(cmdSet.select().checkOK().getData());

        SecureRandom random = new SecureRandom();
        byte hostChallenge[] = new byte[8];
        random.nextBytes(hostChallenge);
        Logger.i("initialize update...");
        cmdSet.initializeUpdate(hostChallenge).checkOK();

        Logger.i("external authenticate...");
        cmdSet.externalAuthenticate(hostChallenge).checkOK();


        Logger.i("delete NDEF instance AID...");
        cmdSet.delete(NDEF_INSTANCE_AID).checkSW(APDUResponse.SW_OK, APDUResponse.SW_REFERENCED_DATA_NOT_FOUND);

        Logger.i("delete wallet AID...");
        cmdSet.delete(WALLET_AID).checkSW(APDUResponse.SW_OK, APDUResponse.SW_REFERENCED_DATA_NOT_FOUND);

        Logger.i("delete package AID...");
        cmdSet.delete(PACKAGE_AID).checkSW(APDUResponse.SW_OK, APDUResponse.SW_REFERENCED_DATA_NOT_FOUND);

        Logger.i("install for load...");
        cmdSet.installForLoad(PACKAGE_AID, sdaid.getAID()).checkSW(APDUResponse.SW_OK, APDUResponse.SW_REFERENCED_DATA_NOT_FOUND);

        InputStream in = this.assets.open(this.capPath);
        Load load = new Load(in);

        byte[] block;
        int steps = load.blocksCount();
        while((block = load.nextDataBlock()) != null) {
            int count = load.getCount() - 1;
            Logger.i(String.format("load %d/%d...", count + 1, steps));
            cmdSet.load(block, count, load.hasMore()).checkOK();
        }

        Logger.i("install for install ndef...");
        byte[] params = HexUtils.hexStringToByteArray("0024d40f12616e64726f69642e636f6d3a706b67696d2e7374617475732e657468657265756d");
        cmdSet.installForInstall(PACKAGE_AID, NDEF_APPLET_AID, NDEF_INSTANCE_AID, params).checkOK();

        Logger.i("install for install wallet...");
        cmdSet.installForInstall(PACKAGE_AID, WALLET_AID, WALLET_AID, new byte[0]).checkOK();

        this.personalizeApplet();

        long duration = System.currentTimeMillis() - startTime;
        Logger.i(String.format("\n\ninstallation completed in %d seconds", duration / 1000));
    }

    private void personalizeApplet() throws NoSuchAlgorithmException, InvalidKeySpecException, APDUException, IOException {
        Secrets secrets = testSecrets ? Secrets.testSecrets() : Secrets.generate();

        WalletAppletCommandSet cmdSet = new WalletAppletCommandSet(this.plainChannel);
        cmdSet.select().checkOK();
        cmdSet.init(secrets.getPin(), secrets.getPuk(), secrets.getPairingToken()).checkOK();

        Logger.i(String.format("PIN: %s\nPUK: %s\nPairing password: %s\nPairing token: %s", secrets.getPin(), secrets.getPuk(), secrets.getPairingPassword(), HexUtils.byteArrayToHexString(secrets.getPairingToken())));
    }
}
