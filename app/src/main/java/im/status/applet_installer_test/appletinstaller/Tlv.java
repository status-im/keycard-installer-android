package im.status.applet_installer_test.appletinstaller;

import java.io.IOException;

public class Tlv {
    private byte tag;
    private byte[] value;

    public Tlv(byte[] value) {
        this((byte) 0x00, value);
    }

    public Tlv(byte tag, byte[] value) {
        this.tag = tag;
        this.value = value;
    }

    public Tlv find(byte tag) throws IOException {
        int offset = 0;

        while(offset + 2 < value.length) {
            byte childTag = this.value[offset];
            offset++;

            int childLength = this.value[offset] & 0xff;
            offset++;

            if (offset + childLength - 1 >= this.value.length) {
                return null;
            }

            byte[] data = new byte[childLength];

            System.arraycopy(this.value, offset, data, 0, childLength);
            offset += childLength;

            if (childTag == tag) {
                return new Tlv(childTag, data);
            }
        }

        return null;
    }

    public byte[] getValue() {
        return value;
    }

    public byte getTag() {
        return tag;
    }
}
