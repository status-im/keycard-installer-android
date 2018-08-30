package im.status.applet_installer_test.appletinstaller;

import java.io.IOException;

public interface Channel {
    byte[] transceive(byte[] data) throws IOException;
}
