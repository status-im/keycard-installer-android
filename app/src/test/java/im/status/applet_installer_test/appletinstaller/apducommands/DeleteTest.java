package im.status.applet_installer_test.appletinstaller.apducommands;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

import im.status.applet_installer_test.appletinstaller.HexUtils;

import static org.junit.Assert.*;

public class DeleteTest {
    @Test
    public void getCommand() throws IOException {
        byte[] aid = HexUtils.hexStringToByteArray("53746174757357616C6C6574");
        Delete delete = new Delete(aid);
        String expected = "80E400000E4F0C53746174757357616C6C6574";
        byte[] apdu = delete.getCommand().serialize();
        assertEquals(expected, HexUtils.byteArrayToHexString(apdu));
    }
}