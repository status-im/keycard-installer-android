package im.status.applet_installer_test.appletinstaller;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class APDUWrapperTest {
    @Test
    public void wrap() throws IOException {
        byte[] macKeyData = HexUtils.hexStringToByteArray("904BA06BCE3037710556BE4057D1493C");
        byte[] data = HexUtils.hexStringToByteArray("7af26ab1ba32b84f");

        APDUCommand cmd = new APDUCommand(0x84, 0x82, 0x01, 0x00, data);
        APDUWrapper w = new APDUWrapper(macKeyData);
        APDUCommand wrapped = w.wrap(cmd);
        byte[] result = wrapped.serialize();
        String expected = "84820100107AF26AB1BA32B84FFE949381C7BC316C00";
        assertEquals(expected, HexUtils.byteArrayToHexString(result));
    }
}