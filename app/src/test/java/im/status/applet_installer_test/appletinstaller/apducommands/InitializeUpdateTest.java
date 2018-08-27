package im.status.applet_installer_test.appletinstaller.apducommands;

import org.junit.Test;

import im.status.applet_installer_test.appletinstaller.APDUCommand;
import im.status.applet_installer_test.appletinstaller.APDUException;
import im.status.applet_installer_test.appletinstaller.APDUResponse;
import im.status.applet_installer_test.appletinstaller.HexUtils;

import static org.junit.Assert.*;

public class InitializeUpdateTest {
    @Test
    public void getCommand() {
        byte[] challenge = InitializeUpdate.generateChallenge();
        InitializeUpdate init = new InitializeUpdate(challenge);
        APDUCommand cmd = init.getCommand();

        assertEquals(0x80, cmd.getCla());
        assertEquals(0x50, cmd.getIns());
        assertEquals(0, cmd.getP1());
        assertEquals(0, cmd.getP2());
        assertEquals(challenge, cmd.getData());
    }

    @Test
    public void deriveKey() {
        InitializeUpdate init = new InitializeUpdate(null);
        byte[] cardKey = HexUtils.hexStringToByteArray("404142434445464748494a4b4c4d4e4f");
        byte[] seq = HexUtils.hexStringToByteArray("0065");

        byte[] encKey = init.deriveKey(cardKey, seq, InitializeUpdate.DERIVATION_PURPOSE_ENC);
        String expectedEncKey = "85E72AAF47874218A202BF5EF891DD21";
        assertEquals(expectedEncKey, HexUtils.byteArrayToHexString(encKey));

        byte[] macKey = init.deriveKey(cardKey, seq, InitializeUpdate.DERIVATION_PURPOSE_MAC);
        String expectedMacKey = "309CF99E164F3A97F3E5017FF540A79F";
        assertEquals(expectedMacKey, HexUtils.byteArrayToHexString(macKey));

        byte[] dekKey = init.deriveKey(cardKey, seq, InitializeUpdate.DERIVATION_PURPOSE_DEK);
        String expectedDekKey = "93D08F8025242C4D775D69B9F16C939B";
        assertEquals(expectedDekKey, HexUtils.byteArrayToHexString(dekKey));
    }

    @Test
    public void validateResponse_BadResponse() throws APDUException {
        byte[] apdu = HexUtils.hexStringToByteArray("000002650183039536622002003b5e508f751c0af3016e3fbc23d3a66982");
        APDUResponse resp = new APDUResponse(apdu);

        byte[] challenge = InitializeUpdate.generateChallenge();
        InitializeUpdate init = new InitializeUpdate(challenge);

        try {
            init.validateResponse(resp);
            fail("expected APDUException to be thrown");
        } catch (APDUException e) {
            assertEquals(0x6982, e.sw);
        }
    }

    @Test
    public void validateResponse_GoodResponse() throws APDUException {
        byte[] challenge = HexUtils.hexStringToByteArray("54676ea0043a2f49");
        InitializeUpdate init = new InitializeUpdate(challenge);

        byte[] apdu = HexUtils.hexStringToByteArray("000002650183039536622002003d2310f3cc9e6cca2551458b8bdb6e9000");
        APDUResponse resp = new APDUResponse(apdu);

        init.validateResponse(resp);
    }
}