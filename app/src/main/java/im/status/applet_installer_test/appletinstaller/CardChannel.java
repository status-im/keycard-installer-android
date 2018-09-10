package im.status.applet_installer_test.appletinstaller;

import android.nfc.tech.IsoDep;

import java.io.IOException;

import im.status.applet_installer_test.appletinstaller.CardManager;

public class CardChannel implements Channel {
    private IsoDep isoDep;

    public CardChannel(IsoDep isoDep) {
        this.isoDep = isoDep;
    }

    public APDUResponse send(APDUCommand cmd) throws IOException {
        byte[] apdu = cmd.serialize();
        Logger.log(String.format("COMMAND  %s", HexUtils.byteArrayToHexString(apdu)));
        byte[] resp = this.isoDep.transceive(apdu);
        Logger.log(String.format("RESPONSE %s %n-----------------------", HexUtils.byteArrayToHexString(resp)));
        return new APDUResponse(resp);
    }
}
