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
    public void verifyCryptogram() {
        byte[] encKey = HexUtils.hexStringToByteArray("A08DF4027BD8ACC9BD89DA2760909D09");
        byte[] cardCryptogram = HexUtils.hexStringToByteArray("1cd15bd25265c990");
        byte[] hostChallenge = HexUtils.hexStringToByteArray("13e7a37b30ef2c22");
        byte[] cardChallenge = HexUtils.hexStringToByteArray("0066ef6b33b04b11");
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