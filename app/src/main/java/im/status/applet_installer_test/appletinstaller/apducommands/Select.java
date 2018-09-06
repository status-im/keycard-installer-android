package im.status.applet_installer_test.appletinstaller.apducommands;

import im.status.applet_installer_test.appletinstaller.APDUCommand;

public class Select {
    private static final int CLA = 0x00;
    private static final int INS = 0xA4;
    private static final int P1 = 0x04;
    private static final int P2 = 0x00; // first occurrence

    private byte[] aid;

    public Select(byte[] aid) {
        this.aid = aid;
    }

    public APDUCommand getCommand() {
        return new APDUCommand(CLA, INS, P1, P2, this.aid);
    }
}
