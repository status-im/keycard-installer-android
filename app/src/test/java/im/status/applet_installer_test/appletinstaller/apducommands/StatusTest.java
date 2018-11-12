package im.status.applet_installer_test.appletinstaller.apducommands;

import org.junit.Test;

import java.io.IOException;

import im.status.applet_installer_test.appletinstaller.HexUtils;

import static org.junit.Assert.*;

public class StatusTest {
    @Test
    public void getCommand() throws IOException {
        Status status = new Status(Status.P1_ISSUER_SECURITY_DOMAIN);
        String expectedAPDU = "80F28002024F0000";
        byte[] apdu = status.getCommand().serialize();
        assertEquals(expectedAPDU, HexUtils.byteArrayToHexString(apdu));
    }
}