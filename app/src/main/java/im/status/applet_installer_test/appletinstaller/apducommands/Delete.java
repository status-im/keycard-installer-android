package im.status.applet_installer_test.appletinstaller.apducommands;

import im.status.applet_installer_test.appletinstaller.APDUCommand;

public class Delete {
    private static final int CLA = 0x80;
    private static final int INS = 0xE4;
    private static final int P1 = 0x00;
    //private static final int P2 = 0x80; // delete object and related files
    private static final int P2 = 0x00;

    private byte[] aid;

    public Delete(byte[] aid) {
        this.aid = aid;
    }

    public APDUCommand getCommand() {
        byte[] data = new byte[this.aid.length + 2];
        data[0] = 0x4F;
        data[1] = (byte) this.aid.length;
        System.arraycopy(this.aid, 0, data, 2, this.aid.length);

        return new APDUCommand(CLA, INS, P1, P2, data);
    }
}
