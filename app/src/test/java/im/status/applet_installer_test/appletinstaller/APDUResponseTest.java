package im.status.applet_installer_test.appletinstaller;

import org.junit.Test;

import static org.junit.Assert.*;

public class APDUResponseTest {
    @Test
    public void parsing() {
        byte[] apdu = HexUtils.hexStringToByteArray("000002650183039536622002003b5e508f751c0af3016e3fbc23d3a69000");
        APDUResponse resp = new APDUResponse(apdu);

        assertEquals(0x9000, resp.getSw());
        assertEquals(0x90, resp.getSw1());
        assertEquals(0x00, resp.getSw2());

        String expected = "000002650183039536622002003B5E508F751C0AF3016E3FBC23D3A6";
        assertEquals(expected, HexUtils.byteArrayToHexString(resp.getData()));
        assertTrue(resp.isOK());
    }
}