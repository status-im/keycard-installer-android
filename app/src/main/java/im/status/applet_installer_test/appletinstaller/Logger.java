package im.status.applet_installer_test.appletinstaller;

import android.util.Log;

public class Logger {
    public static void log(String m) {
        if (m != null) {
            Log.d("installer-debug", m);
        }
    }

    public static void log(byte[] m) {
        log(HexUtils.byteArrayToHexString(m));
    }
}
