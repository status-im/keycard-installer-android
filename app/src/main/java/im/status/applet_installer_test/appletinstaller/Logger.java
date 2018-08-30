package im.status.applet_installer_test.appletinstaller;

import android.util.Log;

public class Logger {
    public static void log(String m) {
        Log.d("installer-debug", m);
    }

    public static void log(byte[] m) {
        Log.d("installer-debug", HexUtils.byteArrayToHexString(m));
    }
}
