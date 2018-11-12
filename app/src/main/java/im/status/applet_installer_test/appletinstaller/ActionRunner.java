package im.status.applet_installer_test.appletinstaller;

import android.content.res.AssetManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import im.status.hardwallet_lite_android.io.APDUException;
import im.status.hardwallet_lite_android.io.CardChannel;
import im.status.hardwallet_lite_android.io.OnCardConnectedListener;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ActionRunner implements OnCardConnectedListener {
    public final static int ACTION_NONE = 0;
    public final static int ACTION_INSTALL = 1;
    public final static int ACTION_INSTALL_TEST = 2;
    public final static int ACTION_PERFTEST = 3;

    private AssetManager assets;
    private String capPath;
    private int requestedAction;


    public ActionRunner(AssetManager assets, String capPath) {
        this.assets = assets;
        this.capPath = capPath;
        this.requestedAction = ACTION_NONE;
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

    private void perform(CardChannel ch) {
        Logger.i("starting requested action");
        try {
            if (!ch.isConnected()) {
                Logger.i("tag disconnected");
                return;
            }

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
            this.requestedAction = ACTION_NONE;
        }
    }

    @Override
    public void onConnected(final CardChannel channel) {
        if (this.requestedAction != ACTION_NONE) {
            Logger.i("waiting 2 seconds to start requested action");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    perform(channel);
                }
            }, 2000);
        } else {
            Logger.i("no action requested yet");
        }
    }
}
