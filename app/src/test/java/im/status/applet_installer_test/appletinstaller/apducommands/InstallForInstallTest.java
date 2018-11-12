package im.status.applet_installer_test.appletinstaller.apducommands;

import org.junit.Test;

import java.io.IOException;

import im.status.applet_installer_test.appletinstaller.HexUtils;
import static org.junit.Assert.*;

public class InstallForInstallTest {
    @Test
    public void forInstall() throws IOException {
        byte[] packageAID = HexUtils.hexStringToByteArray("53746174757357616C6C6574");
        byte[] appletAID = HexUtils.hexStringToByteArray("53746174757357616C6C6574417070");
        byte[] instanceAID = HexUtils.hexStringToByteArray("53746174757357616C6C6574417070");
        byte[] params = HexUtils.hexStringToByteArray("AABBCC");
        InstallForInstall install = new InstallForInstall(packageAID, appletAID, instanceAID, params);
        APDUCommand cmd = install.getCommand();
        byte[] apdu = cmd.serialize();

        String expected = "80E60C00360C53746174757357616C6C65740F53746174757357616C6C65744170700F53746174757357616C6C6574417070010005C903AABBCC00";
        assertEquals(expected, HexUtils.byteArrayToHexString(apdu));
    }
}