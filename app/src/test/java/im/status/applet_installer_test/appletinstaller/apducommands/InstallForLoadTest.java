package im.status.applet_installer_test.appletinstaller.apducommands;

import org.junit.Test;

import java.io.IOException;

import im.status.applet_installer_test.appletinstaller.HexUtils;

import static org.junit.Assert.*;

public class InstallForLoadTest {
    @Test
    public void forLoad() throws IOException {
        byte[] aid = HexUtils.hexStringToByteArray("53746174757357616C6C6574");
        byte[] sdaid = HexUtils.hexStringToByteArray("A000000151000000");
        InstallForLoad install = new InstallForLoad(aid, sdaid);
        APDUCommand cmd = install.getCommand();
        byte[] apdu = cmd.serialize();

        String expected = "80E60200190C53746174757357616C6C657408A000000151000000000000";
        assertEquals(expected, HexUtils.byteArrayToHexString(apdu));
    }
}