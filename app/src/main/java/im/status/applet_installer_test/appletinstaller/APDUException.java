package im.status.applet_installer_test.appletinstaller;

public class APDUException extends Exception {
    public final int sw;

    public APDUException(int sw, String message) {
        super(message + ", 0x" + String.format("0x%04X", sw));
        this.sw = sw;
    }
}
