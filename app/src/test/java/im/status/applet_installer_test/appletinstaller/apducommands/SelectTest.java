package im.status.applet_installer_test.appletinstaller.apducommands;

import org.junit.Test;

import java.io.IOException;

import im.status.applet_installer_test.appletinstaller.APDUCommand;
import im.status.applet_installer_test.appletinstaller.HexUtils;

import static org.junit.Assert.*;

public class SelectTest {
    @Test
    public void getCode() throws IOException {
        Select s = new Select(new byte[0]);
        byte[] apdu = s.getCommand().serialize();
        String expected = "00A4040000";
        assertEquals(expected, HexUtils.byteArrayToHexString(apdu));
    }
}