package im.status.applet_installer_test.appletinstaller.apducommands;

import org.junit.Test;

import java.io.IOException;

import im.status.applet_installer_test.appletinstaller.APDUCommand;
import im.status.applet_installer_test.appletinstaller.HexUtils;

import static org.junit.Assert.*;

public class InstallTest {
    @Test
    public void forLoad() throws IOException {
        Install install = Install.forLoad();
        byte[] aid = HexUtils.hexStringToByteArray("53746174757357616C6C6574");
        byte[] sdaid = HexUtils.hexStringToByteArray("A000000151000000");
        APDUCommand cmd = install.getCommandForLoad(aid, sdaid);
        byte[] apdu = cmd.serialize();

        String expected = "80E60200190C53746174757357616C6C657408A000000151000000000000";
        assertEquals(expected, HexUtils.byteArrayToHexString(apdu));
    }

    @Test
    public void forInstall() throws IOException {
        Install install = Install.forInstall();
        byte[] packageAID = HexUtils.hexStringToByteArray("53746174757357616C6C6574");
        byte[] appletAID = HexUtils.hexStringToByteArray("53746174757357616C6C6574417070");
        byte[] instanceAID = HexUtils.hexStringToByteArray("53746174757357616C6C6574417070");
        byte[] params = HexUtils.hexStringToByteArray("AABBCC");
        APDUCommand cmd = install.getCommandForInstall(packageAID, appletAID, instanceAID, params);
        byte[] apdu = cmd.serialize();

        String expected = "80E60C00360C53746174757357616C6C65740F53746174757357616C6C65744170700F53746174757357616C6C6574417070010005C903AABBCC00";
        assertEquals(expected, HexUtils.byteArrayToHexString(apdu));
    }
}