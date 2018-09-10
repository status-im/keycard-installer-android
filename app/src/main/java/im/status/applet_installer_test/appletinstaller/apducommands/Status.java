package im.status.applet_installer_test.appletinstaller.apducommands;

import im.status.applet_installer_test.appletinstaller.APDUCommand;

public class Status {
    private static final int CLA = 0x80;
    private static final int INS = 0xF2;

    public static final int P1_ISSUER_SECURITY_DOMAIN = 0x80;
    public static final int P1_APPLICATIONS = 0x40;
    public static final int P1_EXECUTABLE_LOAD_FILES = 0x20;
    public static final int P1_EXECUTABLE_LOAD_FILES_AND_MODULES = 0x10;

    private static final int P2 = 0x02;

    private int p1;

    public Status(int p1) {
        this.p1 = p1;
    }

    public APDUCommand getCommand() {
        byte[] data = new byte[]{0x4F, 0x00};
        return new APDUCommand(CLA, INS, this.p1, P2, data, true);
    }
}
