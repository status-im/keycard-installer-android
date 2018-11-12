package im.status.applet_installer_test.appletinstaller;

import im.status.hardwallet_lite_android.io.APDUCommand;
import im.status.hardwallet_lite_android.io.APDUResponse;
import im.status.hardwallet_lite_android.io.CardChannel;

import java.io.IOException;

public class SecureChannel {
    private CardChannel channel;
    private APDUWrapper wrapper;

    public SecureChannel(CardChannel channel, Keys keys) {
        this.channel = channel;
        this.wrapper = new APDUWrapper(keys.getMacKeyData());
    }

    public APDUResponse send(APDUCommand cmd) throws IOException {
        Logger.d(String.format("WRAPPING %s %n", HexUtils.byteArrayToHexString(cmd.serialize())), false);
        APDUCommand wrappedCommand = this.wrapper.wrap(cmd);
        Logger.d(String.format("WRAPPED  %s %n", HexUtils.byteArrayToHexString(wrappedCommand.serialize())), false);
        return this.channel.send(wrappedCommand);
    }
}
