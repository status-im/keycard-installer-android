package im.status.applet_installer_test.appletinstaller;

import android.nfc.tech.IsoDep;

import java.io.IOException;

import im.status.applet_installer_test.appletinstaller.CardManager;

public class CardChannel implements Channel {
    private IsoDep isoDep;

    public CardChannel(IsoDep isoDep) {
        this.isoDep = isoDep;
    }

    public byte[] transceive(byte[] data) throws IOException {
        Logger.log(String.format("COMMAND %s %n", HexUtils.byteArrayToHexString(data)));
        byte[] resp = this.isoDep.transceive(data);
        Logger.log(String.format("RESPONSE %s %n", HexUtils.byteArrayToHexString(resp)));
        return resp;
    }
}
