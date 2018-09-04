package im.status.applet_installer_test.appletinstaller.apducommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;

import im.status.applet_installer_test.appletinstaller.APDUCommand;

public class Install {
    public static final int CLA = 0x80;
    public static final int INS = 0xE6;

    public static final int P1_FOR_LOAD = 0x02;
    public static final int P1_FOR_INSTALL = 0x0C;

    public static final int P2 = 0;

    private int p1;

    public Install(int p1) {
        this.p1 = p1;
    }

    public APDUCommand getCommandForLoad(byte[] aid, byte[] sdaid) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.write(aid.length);
        data.write(aid);
        data.write(sdaid.length);
        data.write(sdaid);

        // empty hash length and hash
        data.write(0x00);
        data.write(0x00);
        data.write(0x00);

        return new APDUCommand(CLA, INS, this.p1, P2, data.toByteArray() );
    }

    public APDUCommand getCommandForInstall(byte[] packageAID, byte[] appletAID, byte[] instanceAID, byte[] params) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.write(packageAID.length);
        data.write(packageAID);
        data.write(appletAID.length);
        data.write(appletAID);
        data.write(instanceAID.length);
        data.write(instanceAID);

        byte[] priviledges = new byte[]{0x00};
        data.write(priviledges.length);
        data.write(priviledges);

        byte[] fullParams = new byte[2 + params.length];
        fullParams[0] = (byte) 0xC9;
        fullParams[1] = (byte) params.length;
        System.arraycopy(params, 0, fullParams, 2, params.length);

        data.write(fullParams.length);
        data.write(fullParams);

        // empty install token
        data.write(0x00);

        return new APDUCommand(CLA, INS, this.p1, P2, data.toByteArray() );
    }

    public static Install forLoad() {
        return new Install(P1_FOR_LOAD);
    }

    public static Install forInstall() {
        return new Install(P1_FOR_INSTALL);
    }
}
