package im.status.keycard.installer;

import android.util.Log;

interface UILogger {
    public void log(String m);
}

public class Logger {
    private static UILogger uiLogger;
    private static int Level = Log.VERBOSE;
    private static int UILevel = Log.VERBOSE;

    public static void setUILogger(UILogger l) {
        uiLogger = l;
    }

    public static void setLevel(int level) {
        Level = level;
    }

    public static void setUILevel(int level) {
        UILevel = level;
    }

    public static void log(int _level, String m, boolean showInUI) {
        if (m != null && _level >= Level) {
            Log.println(_level, "installer-debug", m);
            if (showInUI && uiLogger != null && _level >= UILevel) {
                uiLogger.log(m);
            }
        }
    }

    public static void log(int level, String m) {
        log(level, m, true);
    }

    public static void d(String m, boolean showInUI) {
        log(Log.DEBUG, m, showInUI);
    }

    public static void d(String m) {
        d(m, true);
    }

    public static void i(String m, boolean showInUI) {
        log(Log.INFO, m, showInUI);
    }

    public static void i(String m) {
        i(m, true);
    }

    public static void e(String m, boolean showInUI) {
        log(Log.ERROR, m, showInUI);
    }

    public static void e(String m) {
        e(m, true);
    }
}
