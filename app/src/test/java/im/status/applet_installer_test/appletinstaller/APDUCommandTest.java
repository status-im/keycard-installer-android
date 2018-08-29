package im.status.applet_installer_test.appletinstaller;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class APDUCommandTest {
    @Test
    public void serialize() throws IOException {
        int cla = 0x80;
        int ins = 0x50;
        int p1 = 0;
        int p2 = 0;
        byte[] data = HexUtils.hexStringToByteArray("84762336c5187fe8");

        APDUCommand c = new APDUCommand(cla, ins, p1, p2, (byte[])data, true);
        String expected = "805000000884762336C5187FE800";
        String actual = HexUtils.byteArrayToHexString(c.serialize());
        assertEquals(expected, actual);

        c = new APDUCommand(cla, ins, p1, p2, (byte[])data);
        expected = "805000000884762336C5187FE8";
        actual = HexUtils.byteArrayToHexString(c.serialize());
        assertEquals(expected, actual);
    }
}