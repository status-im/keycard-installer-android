package im.status.applet_installer_test.appletinstaller;

import android.content.res.AssetManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import java.io.IOException;

public class CardManager extends Thread implements NfcAdapter.ReaderCallback {
    public final static int ACTION_NONE = 0;
    public final static int ACTION_INSTALL = 1;
    public final static int ACTION_INSTALL_TEST = 2;
    public final static int ACTION_PERFTEST = 3;

    private NfcAdapter nfcAdapter;
    private AssetManager assets;
    private String capPath;
    private IsoDep isoDep;
    private int requestedAction;
    private long cardConnectedAt;
    private boolean running;

    public CardManager(NfcAdapter nfcAdapter, AssetManager assets, String capPath) {
        this.nfcAdapter = nfcAdapter;
        this.assets = assets;
        this.capPath = capPath;
        this.requestedAction = ACTION_NONE;
    }

    public boolean isConnected() {
        return this.isoDep != null && this.isoDep.isConnected();
    }

    public void requestAction(int actionRequested) {
        switch(actionRequested) {
            case ACTION_NONE:
                Logger.i("cancelling requested action");
                break;
            case ACTION_INSTALL:
                Logger.i("installation requested");
                break;
            case ACTION_INSTALL_TEST:
                Logger.i("installation with test secrets requested");
                break;
            case ACTION_PERFTEST:
                Logger.i("performance tests requested");
                break;
            default:
                Logger.i("invalid action requested, ignoring");
                return;
        }

        this.requestedAction = actionRequested;
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        this.isoDep = IsoDep.get(tag);
        try {
            this.isoDep = IsoDep.get(tag);
            this.isoDep.connect();
            this.isoDep.setTimeout(120000);
        } catch (IOException e) {
            Logger.e("error connecting to tag");
        }
    }

    public void run() {
        boolean connected = this.isConnected();

        while(true) {
            boolean newConnected = this.isConnected();
            if (newConnected != connected) {
                connected = newConnected;
                Logger.i("tag " + (connected ? "connected" : "disconnected"));
                if (connected) {
                    this.onCardConnected();
                } else {
                    this.onCardDisconnected();
                }
            }

            if (connected && (this.requestedAction != ACTION_NONE) && !this.running) {
                long now = System.currentTimeMillis();
                if (now - this.cardConnectedAt > 2000) {
                    this.perform();
                }
            }

            try {
                this.sleep(50);
            } catch (InterruptedException e) {
                Logger.e("error in TagManager thread: " + e.getMessage());
                this.interrupt();
            }
        }
    }

    private void onCardConnected() {
        this.cardConnectedAt = System.currentTimeMillis();
        if (this.requestedAction != ACTION_NONE) {
            Logger.i("waiting 2 seconds to start requested action");
        } else {
            Logger.i("no action requested yet");
        }
    }

    private void onCardDisconnected() {
        this.cardConnectedAt = 0;
        this.isoDep = null;
    }

    private void perform() {
        Logger.i("starting requested action");
        this.running = true;
        try {
            CardChannel ch = new CardChannel(this.isoDep);

            switch (requestedAction) {
                case ACTION_INSTALL:
                    Installer installer = new Installer(ch, this.assets, this.capPath, false);
                    installer.start();
                    break;
                case ACTION_INSTALL_TEST:
                    installer = new Installer(ch, this.assets, this.capPath, true);
                    installer.start();
                    break;
                case ACTION_PERFTEST:
                    PerfTest perfTest = new PerfTest(ch);
                    perfTest.test();
                    break;
                default:
                    throw new Exception("Unknown action");
            }

        } catch (IOException e) {
            Logger.e("IO exception: " + e.getMessage());
        } catch (APDUException e) {
            Logger.e("APDU exception: " + e.getMessage());
        } catch (Exception e) {
            Logger.e("Other exception: " + e.getMessage());
        } finally {
            this.running = false;
            this.requestedAction = ACTION_NONE;
            this.cardConnectedAt = 0;
        }
    }
}
