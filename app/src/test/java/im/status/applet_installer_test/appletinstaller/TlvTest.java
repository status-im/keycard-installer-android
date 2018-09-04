package im.status.applet_installer_test.appletinstaller;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

public class TlvTest {
    @Test
    public void find() throws IOException {
        byte[] data = HexUtils.hexStringToByteArray("C102BBCCC203DDEE11C30201");
        Tlv tlv = new Tlv(data);

        Tlv child1 = tlv.find((byte) 0xC1);
        String expected = "BBCC";
        assertEquals(expected, HexUtils.byteArrayToHexString(child1.getValue()));

        Tlv child2 = tlv.find((byte) 0xC2);
        expected = "DDEE11";
        assertEquals(expected, HexUtils.byteArrayToHexString(child2.getValue()));


        // tag exists, length is bigger than available data
        assertNull(tlv.find((byte) 0xC3));
        // not found
        assertNull(tlv.find((byte) 0xFF));
        // not found
        assertNull(child2.find((byte) 0xFF));
    }

    @Test
    public void findAID() throws IOException {
        byte[] data = HexUtils.hexStringToByteArray("6f5c8408a000000151000000a550734a06072a864886fc6b01600c060a2a864886fc6b02020201630906072a864886fc6b03640b06092a864886fc6b040255650b06092a864886fc6b020103660c060a2b060104012a026e01039f6501ff9000");
        Tlv tlv = new Tlv(data);

        Tlv child1 = tlv.find((byte) 0x6F);
        assertNotNull(child1);


        Tlv child2 = child1.find((byte) 0x84);
        assertNotNull(child2);

        String expected = "A000000151000000";
        assertEquals(expected, HexUtils.byteArrayToHexString(child2.getValue()));
    }
}