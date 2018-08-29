package im.status.applet_installer_test.appletinstaller;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class APDUWrapperTest {
    @Test
    public void wrap() throws IOException {
        byte[] macKeyData = HexUtils.hexStringToByteArray("07EFCCEB0BB0CC01A22E0CE1E1E395F8");
        byte[] data = HexUtils.hexStringToByteArray("3CE060483AACE927");

        APDUCommand cmd = new APDUCommand(0x84, 0x82, 0x01, 0x00, data);
        APDUWrapper w = new APDUWrapper(macKeyData);
        APDUCommand wrapped = w.wrap(cmd);
        byte[] result = wrapped.serialize();
        String expected = "84820100103CE060483AACE927A3CDA954B0E88839";
        assertEquals(expected, HexUtils.byteArrayToHexString(result));
    }
}