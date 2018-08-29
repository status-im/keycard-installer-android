package im.status.applet_installer_test.appletinstaller.apducommands;

import org.junit.Test;

import java.io.IOException;

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

    @Test
    public void getCommand() throws IOException {
        byte[] encKeyData = HexUtils.hexStringToByteArray("B587BB999A67AB99F5222D07EBB061EE");
        byte[] cardChallenge = HexUtils.hexStringToByteArray("00819711736d57f0");
        byte[] hostChallenge = HexUtils.hexStringToByteArray("796007fae07336b4");

        ExternalAuthenticate auth = new ExternalAuthenticate(encKeyData, cardChallenge, hostChallenge);

        String expectedAPDU = "84820100086EFBCD81821F267B";
        byte[] apdu = auth.getCommand().serialize();
        assertEquals(expectedAPDU, HexUtils.byteArrayToHexString(apdu));
    }
}

