package im.status.applet_installer_test.appletinstaller;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import java.io.IOException;

public class CardManager {
    private Tag tag;
    private IsoDep isoDep;

    public CardManager(Tag tag) {
        this.tag = tag;
    }

    public void connect() throws IOException {
        this.isoDep = IsoDep.get(tag);
        this.isoDep.connect();
    }

    public void install() throws IOException, APDUException {
        CardChannel ch = new CardChannel(this.isoDep);
        Installer installer = new Installer(ch);
        installer.start();
    }
}
