package im.status.applet_installer_test.appletinstaller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class APDUCommand {
    protected int cla;
    protected int ins;
    protected int p1;
    protected int p2;
    protected int lc;
    protected byte[] data;

    public APDUCommand(int cla, int ins, int p1, int p2, byte[] data) {
        this.cla = cla;
        this.ins = ins;
        this.p1 = p1;
        this.p2 = p2;
        this.data = data;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(this.cla);
        out.write(this.ins);
        out.write(this.p1);
        out.write(this.p2);

        int lc = this.data.length;
        if (lc > 0) {
            out.write(lc);
            out.write(this.data);
        }

        out.write(0); // Response length

        return out.toByteArray();
    }
}
