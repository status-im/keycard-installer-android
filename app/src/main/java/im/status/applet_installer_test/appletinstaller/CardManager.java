package im.status.applet_installer_test.appletinstaller;

import android.content.res.AssetManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import java.io.IOException;

public class CardManager extends Thread implements NfcAdapter.ReaderCallback {
    private NfcAdapter nfcAdapter;
    private AssetManager assets;
    private String capPath;
    private IsoDep isoDep;
    private boolean installationRequested;
    private long cardConnectedAt;
    private boolean installing;

    public CardManager(NfcAdapter nfcAdapter, AssetManager assets, String capPath) {
        this.nfcAdapter = nfcAdapter;
        this.assets = assets;
        this.capPath = capPath;
    }

    public boolean isConnected() {
        return this.isoDep != null && this.isoDep.isConnected();
    }

    public void startInstallation() {
        Logger.log("installation requested");
        this.installationRequested = true;
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        this.isoDep = IsoDep.get(tag);
        try {
            this.isoDep = IsoDep.get(tag);
            this.isoDep.connect();
            this.isoDep.setTimeout(120000);
        } catch (IOException e) {
            Logger.log("error connecting to tag");
        }
    }

    public void run() {
        boolean connected = this.isConnected();

        while(true) {
            boolean newConnected = this.isConnected();
            if (newConnected != connected) {
                connected = newConnected;
                Logger.log("tag " + (connected ? "connected" : "disconnected"));
                if (connected) {
                    this.onCardConnected();
                } else {
                    this.onCardDisconnected();
                }
            }

            if (connected && this.installationRequested && !this.installing) {
                long now = System.nanoTime();
                if (now - this.cardConnectedAt > 2e+9) {
                    this.install();
                }
            }

            try {
                this.sleep(50);
            } catch (InterruptedException e) {
                Logger.log("error in TagManager thread: " + e.getMessage());
                this.interrupt();
            }
        }
    }

    private void onCardConnected() {
        this.cardConnectedAt = System.nanoTime();
        if (this.installationRequested) {
            Logger.log("waiting 2 seconds to start installation");
        } else {
            Logger.log("installation not requested yet");
        }
    }

    private void onCardDisconnected() {
        this.cardConnectedAt = 0;
        this.isoDep = null;
    }

    public void install() {
        Logger.log("starting installation");
        this.installing = true;
        try {
            CardChannel ch = new CardChannel(this.isoDep);
            Installer installer = new Installer(ch, this.assets, this.capPath);
            installer.start();
        } catch (IOException e) {
            Logger.log("IO exception: " + e.getMessage());
        } catch (APDUException e) {
            Logger.log("APDU exception: " + e.getMessage());
        } finally {
            this.installing = false;
            this.installationRequested = false;
            this.cardConnectedAt = 0;
        }
    }
}
