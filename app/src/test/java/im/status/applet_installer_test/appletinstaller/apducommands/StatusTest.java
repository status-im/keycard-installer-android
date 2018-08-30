package im.status.applet_installer_test.appletinstaller.apducommands;

import org.junit.Test;

import java.io.IOException;

import im.status.applet_installer_test.appletinstaller.APDUCommand;
import im.status.applet_installer_test.appletinstaller.APDUWrapper;
import im.status.applet_installer_test.appletinstaller.HexUtils;

import static org.junit.Assert.*;

public class StatusTest {
    @Test
    public void getCommand() throws IOException {
        Status status = new Status(Status.P1_ISSUER_SECURITY_DOMAIN);
        String expectedAPDU = "80F28002024F0000";
        byte[] apdu = status.getCommand().serialize();
        assertEquals(expectedAPDU, HexUtils.byteArrayToHexString(apdu));

        APDUWrapper wrapper = new APDUWrapper(HexUtils.hexStringToByteArray("34211B65F5A563C077EC5384876DC809"));
        APDUCommand wrappedCommand = wrapper.wrap(status.getCommand());

        //expectedAPDU = "84F280020A4F0071BF1479317EDB4700";
        //apdu = wrappedCommand.serialize();
        //assertEquals(expectedAPDU, HexUtils.byteArrayToHexString(apdu));


        //expectedAPDU = "84F280020A4F0071BF1479317EDB4700";
        //apdu = wrappedCommand.serialize();
        //assertEquals(expectedAPDU, HexUtils.byteArrayToHexString(apdu));
    }
}