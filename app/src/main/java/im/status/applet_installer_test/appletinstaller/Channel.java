package im.status.applet_installer_test.appletinstaller;

import java.io.IOException;

public interface Channel {
    APDUResponse send(APDUCommand cmd) throws IOException;
}
