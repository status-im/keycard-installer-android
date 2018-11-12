package im.status.applet_installer_test.appletinstaller.apducommands;

import org.junit.Test;

import java.io.IOException;

import im.status.applet_installer_test.appletinstaller.HexUtils;
import im.status.applet_installer_test.appletinstaller.Keys;

import static org.junit.Assert.*;

public class InitializeUpdateTest {
    @Test
    public void getCommand() throws IOException {
        byte[] challenge = HexUtils.hexStringToByteArray("2d315d5ffc616d10");
        InitializeUpdate init = new InitializeUpdate(challenge);
        APDUCommand cmd = init.getCommand();

        assertEquals(0x80, cmd.getCla());
        assertEquals(0x50, cmd.getIns());
        assertEquals(0, cmd.getP1());
        assertEquals(0, cmd.getP2());
        assertEquals(challenge, cmd.getData());

        String expectedAPDU = "80500000082D315D5FFC616D1000";
        byte[] apdu = cmd.serialize();
        assertEquals(expectedAPDU, HexUtils.byteArrayToHexString(apdu));
    }

    @Test
    public void validateResponse_BadResponse() throws APDUException {
        byte[] apdu = HexUtils.hexStringToByteArray("000002650183039536622002003b5e508f751c0af3016e3fbc23d3a66982");
        APDUResponse resp = new APDUResponse(apdu);

        byte[] challenge = InitializeUpdate.generateChallenge();
        InitializeUpdate init = new InitializeUpdate(challenge);

        try {
            init.verifyResponse(new Keys(new byte[]{}, new byte[]{}), resp);
            fail("expected APDUException to be thrown");
        } catch (APDUException e) {
            assertEquals(0x6982, e.sw);
        }
    }

    //TODO: reimplement test
    //@Test
    //public void validateResponse_GoodResponse() throws APDUException {
    //    byte[] encKey = HexUtils.hexStringToByteArray("16B5867FF50BE7239C2BF1245B83A362");

    //    byte[] challenge = HexUtils.hexStringToByteArray("f0467f908e5ca23f");
    //    InitializeUpdate init = new InitializeUpdate(challenge);

    //    byte[] apdu = HexUtils.hexStringToByteArray("000002650183039536622002000de9c62ba1c4c8e55fcb91b6654ce49000");
    //    APDUResponse resp = new APDUResponse(apdu);

    //    init.verifyResponse(new Keys(), resp);
    //}
}