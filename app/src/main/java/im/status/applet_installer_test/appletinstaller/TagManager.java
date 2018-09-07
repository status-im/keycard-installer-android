package im.status.applet_installer_test.appletinstaller;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import java.io.IOException;

public class TagManager extends Thread implements NfcAdapter.ReaderCallback {
    NfcAdapter nfcAdapter;
    IsoDep isoDep;

    public TagManager(NfcAdapter nfcAdapter) {
        this.nfcAdapter = nfcAdapter;
    }

    public boolean isConnected() {
        return this.isoDep != null && this.isoDep.isConnected();
    }

    public void run() {
        boolean connected = this.isConnected();

        while(true) {
            boolean newConnected = this.isConnected();
            if (newConnected != connected) {
                connected = newConnected;
                Logger.log("tag " + (connected ? "connected" : "disconnected"));

                if (!newConnected) {
                    this.isoDep = null;
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

    @Override
    public void onTagDiscovered(Tag tag) {
        this.isoDep = IsoDep.get(tag);
        try {
            this.isoDep.connect();
        } catch (IOException e) {
            Logger.log("error connecting to tag");
        }
    }
}
