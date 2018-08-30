package im.status.applet_installer_test.appletinstaller;

public class APDUResponse {
    public static int SW_OK = 0x9000;
    public static int SW_SECURITY_CONDITION_NOT_SATISFIED = 0x6982;
    public static int SW_AUTHENTICATION_METHOD_BLOCKED = 0x6983;

    private byte[] apdu;
    private byte[] data;
    private int sw;
    private int sw1;
    private int sw2;

    public APDUResponse(byte[] apdu)  {
        if (apdu.length < 2) {
            throw new IllegalArgumentException("APDU response must be at least 2 bytes");
        }
        this.apdu = apdu;
        this.parse();
    }

    private void parse() {
        int length = this.apdu.length;

        this.sw1 = this.apdu[length - 2] & 0xff;
        this.sw2 = this.apdu[length - 1] & 0xff;
        this.sw = (this.sw1 << 8) | this.sw2;

        this.data = new byte[length - 2];
        System.arraycopy(this.apdu, 0, this.data, 0, length - 2);
    }

    public boolean isOK() {
        return this.sw == SW_OK;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getSw() {
        return this.sw;
    }

    public int getSw1() {
        return this.sw1;
    }

    public int getSw2() {
        return this.sw2;
    }

    public byte[] getBytes() {
        return this.apdu;
    }
}
