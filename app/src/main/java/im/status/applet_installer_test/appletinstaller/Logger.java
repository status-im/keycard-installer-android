package im.status.applet_installer_test.appletinstaller;

import android.util.Log;

interface UILogger {
    public void log(String m);
}

public class Logger {
    private static UILogger uiLogger;

    public static void setUILogger(UILogger l) {
        uiLogger = l;
    }

    public static void log(String m) {
        log(m, true);
    }

    public static void log(String m, boolean showInUI) {
        if (m != null) {
            Log.d("installer-debug", m);
            if (showInUI && uiLogger != null) {
                uiLogger.log(m);
            }
        }
    }

    public static void log(byte[] m) {
        log(m, true);
    }

    public static void log(byte[] m, boolean showInUI) {
        log(HexUtils.byteArrayToHexString(m), showInUI);
    }
}
