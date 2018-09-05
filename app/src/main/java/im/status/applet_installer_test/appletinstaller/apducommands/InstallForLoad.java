package im.status.applet_installer_test.appletinstaller.apducommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import im.status.applet_installer_test.appletinstaller.APDUCommand;

public class InstallForLoad {
    public static final int CLA = 0x80;
    public static final int INS = 0xE6;
    public static final int P1 = 0x02;
    public static final int P2 = 0;

    private byte[] aid;
    private byte[] sdaid;

    public InstallForLoad(byte[] aid, byte[] sdaid) {
        this.aid = aid;
        this.sdaid = sdaid;
    }

    public APDUCommand getCommand() throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.write(this.aid.length);
        data.write(this.aid);
        data.write(this.sdaid.length);
        data.write(this.sdaid);

        // empty hash length and hash
        data.write(0x00);
        data.write(0x00);
        data.write(0x00);

        return new APDUCommand(CLA, INS, P1, P2, data.toByteArray());
    }
}
