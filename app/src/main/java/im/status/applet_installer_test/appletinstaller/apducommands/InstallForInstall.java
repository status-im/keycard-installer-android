package im.status.applet_installer_test.appletinstaller.apducommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import im.status.applet_installer_test.appletinstaller.APDUCommand;

public class InstallForInstall {
    public static final int CLA = 0x80;
    public static final int INS = 0xE6;
    public static final int P1 = 0x0C;
    public static final int P2 = 0;

    private byte[] packageAID;
    private byte[] appletAID;
    private byte[] instanceAID;
    private byte[] params;

    public InstallForInstall(byte[] packageAID, byte[] appletAID, byte[] instanceAID, byte[] params) {
        this.packageAID = packageAID;
        this.appletAID = appletAID;
        this.instanceAID = instanceAID;
        this.params = params;
    }

    public APDUCommand getCommand() throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.write(this.packageAID.length);
        data.write(this.packageAID);
        data.write(this.appletAID.length);
        data.write(this.appletAID);
        data.write(this.instanceAID.length);
        data.write(this.instanceAID);

        byte[] privileges = new byte[]{0x00};
        data.write(privileges.length);
        data.write(privileges);

        byte[] fullParams = new byte[2 + params.length];
        fullParams[0] = (byte) 0xC9;
        fullParams[1] = (byte) params.length;
        System.arraycopy(params, 0, fullParams, 2, params.length);

        data.write(fullParams.length);
        data.write(fullParams);

        // empty perform token
        data.write(0x00);

        return new APDUCommand(CLA, INS, P1, P2, data.toByteArray() );
    }
}
