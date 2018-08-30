package im.status.applet_installer_test.appletinstaller;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class APDUWrapperTest {
    @Test
    public void wrap() throws IOException {
        byte[] macKeyData = HexUtils.hexStringToByteArray("2983BA77D709C2DAA1E6000ABCCAC951");
        byte[] data = HexUtils.hexStringToByteArray("1d4de92eaf7a2c9f");

        APDUCommand cmd = new APDUCommand(0x84, 0x82, 0x01, 0x00, data);
        APDUWrapper w = new APDUWrapper(macKeyData);

        // check null icv
        assertEquals(HexUtils.byteArrayToHexString(Crypto.NullBytes8), HexUtils.byteArrayToHexString(w.getICV()));

        APDUCommand wrapped = w.wrap(cmd);
        byte[] result = wrapped.serialize();
        String expected = "84820100101D4DE92EAF7A2C9F8F9B0DF681C1D3EC";
        assertEquals(expected, HexUtils.byteArrayToHexString(result));

        // second command


        // check icv from previous mac
        assertEquals("8F9B0DF681C1D3EC", HexUtils.byteArrayToHexString(w.getICV()));

        data = HexUtils.hexStringToByteArray("4F00");
        cmd = new APDUCommand(0x80, 0xF2, 0x80, 0x02, data, true);

        wrapped = w.wrap(cmd);
        result = wrapped.serialize();
        expected = "84F280020A4F0030F149209E17B39700";
        assertEquals(expected, HexUtils.byteArrayToHexString(result));

        // third command


        // check icv from previous mac
        assertEquals("30F149209E17B397", HexUtils.byteArrayToHexString(w.getICV()));

        data = HexUtils.hexStringToByteArray("4F00");
        cmd = new APDUCommand(0x80, 0xF2, 0x40, 0x02, data, true);

        wrapped = w.wrap(cmd);
        result = wrapped.serialize();
        expected = "84F240020A4F000D9B2D4E4365B5BD00";
        assertEquals(expected, HexUtils.byteArrayToHexString(result));
    }
}