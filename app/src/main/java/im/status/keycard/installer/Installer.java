package im.status.keycard.installer;

import android.content.res.AssetManager;
import im.status.keycard.applet.KeycardCommandSet;
import im.status.keycard.globalplatform.GlobalPlatformCommandSet;
import im.status.keycard.globalplatform.LoadCallback;
import im.status.keycard.io.APDUException;
import im.status.keycard.io.CardChannel;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Installer {
    private CardChannel plainChannel;
    private AssetManager assets;
    private String capPath;

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

        Logger.i("select ISD...");
        cmdSet = new GlobalPlatformCommandSet(this.plainChannel);
        cmdSet.select().checkOK();

        Logger.i("opening secure channel...");
        cmdSet.openSecureChannel();

        Logger.i("deleting old version (if present)...");
        cmdSet.deleteKeycardInstancesAndPackage();

        Logger.i("loading package...");
        cmdSet.loadKeycardPackage(this.assets.open(this.capPath), new LoadCallback() {
            public void blockLoaded(int loadedBlock, int blockCount) {
                Logger.i(String.format("load %d/%d...", loadedBlock, blockCount));
            }
        });

        Logger.i("installing NDEF applet...");
        cmdSet.installNDEFApplet(Hex.decode("0024d40f12616e64726f69642e636f6d3a706b67696d2e7374617475732e657468657265756d")).checkOK();

        Logger.i("installing Keycard applet...");
        cmdSet.installKeycardApplet().checkOK();

        if (testSecrets) {
            this.personalizeApplet();
        }

        long duration = System.currentTimeMillis() - startTime;
        Logger.i(String.format("\n\ninstallation completed in %d seconds", duration / 1000));
    }

    private void personalizeApplet() throws NoSuchAlgorithmException, InvalidKeySpecException, APDUException, IOException {
        Secrets secrets = Secrets.testSecrets();

        KeycardCommandSet cmdSet = new KeycardCommandSet(this.plainChannel);
        cmdSet.select().checkOK();
        cmdSet.init(secrets.getPin(), secrets.getPuk(), secrets.getPairingPassword()).checkOK();

        Logger.i(String.format("PIN: %s\nPUK: %s\nPairing password: %s", secrets.getPin(), secrets.getPuk(), secrets.getPairingPassword()));
    }
}
