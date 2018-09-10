package im.status.applet_installer_test.appletinstaller;

import android.provider.Settings;

import java.io.IOException;

public class SecureChannel implements Channel {
    private Channel channel;
    private APDUWrapper wrapper;

    public SecureChannel(Channel channel, Keys keys) {
        this.channel = channel;
        this.wrapper = new APDUWrapper(keys.getMacKeyData());
    }

    public APDUResponse send(APDUCommand cmd) throws IOException {
        Logger.log(String.format("WRAPPING %s %n", HexUtils.byteArrayToHexString(cmd.serialize())), false);
        APDUCommand wrappedCommand = this.wrapper.wrap(cmd);
        Logger.log(String.format("WRAPPED  %s %n", HexUtils.byteArrayToHexString(wrappedCommand.serialize())), false);
        return this.channel.send(wrappedCommand);
    }
}
