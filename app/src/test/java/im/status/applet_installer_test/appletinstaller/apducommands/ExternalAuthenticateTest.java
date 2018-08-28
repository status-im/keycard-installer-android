package im.status.applet_installer_test.appletinstaller.apducommands;

import org.junit.Test;

import im.status.applet_installer_test.appletinstaller.HexUtils;

import static org.junit.Assert.*;

public class ExternalAuthenticateTest {
    @Test
    public void getHostCryptogram() {
        byte[] encKeyData = HexUtils.hexStringToByteArray("0EF72A1065236DD6CAC718D5E3F379A4");
        byte[] cardChallenge = HexUtils.hexStringToByteArray("0076a6c0d55e9535");
        byte[] hostChallenge = HexUtils.hexStringToByteArray("266195e638da1b95");

        ExternalAuthenticate auth = new ExternalAuthenticate(encKeyData, cardChallenge, hostChallenge);

        String expectedHostCryptogram = "45A5F48DAE68203C";
        byte[] hostCryptogram = auth.getHostCryptogram();
        assertEquals(expectedHostCryptogram, HexUtils.byteArrayToHexString(hostCryptogram));
    }
}

