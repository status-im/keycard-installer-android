package im.status.applet_installer_test.appletinstaller;

import android.util.Log;

interface LogListener {
    public void log(String m);
}

public class Logger {
    private static LogListener listener;

    public static void setListener(LogListener l) {
        listener = l;
    }

    public static void log(String m) {
        if (m != null) {
            Log.d("installer-debug", m);
            if (listener != null) {
                listener.log(m);
            }
        }
    }

    public static void log(byte[] m) {
        log(HexUtils.byteArrayToHexString(m));
    }
}
