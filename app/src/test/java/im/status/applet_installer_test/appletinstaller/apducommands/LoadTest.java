package im.status.applet_installer_test.appletinstaller.apducommands;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import im.status.applet_installer_test.appletinstaller.APDUCommand;
import im.status.applet_installer_test.appletinstaller.HexUtils;

import static org.junit.Assert.*;

public class LoadTest {

    @Test
    public void getCode() throws IOException {
        URL url = this.getClass().getClassLoader().getResource("wallet.cap");
        Load load = new Load(url.getPath());

        ArrayList<APDUCommand> commands = new ArrayList<APDUCommand>();
        APDUCommand cmd;
        while((cmd = load.getCommand()) != null) {
            commands.add(cmd);
        }

        assertEquals(31, commands.size());

        // Command 1
        cmd = commands.get(0);
        assertEquals(0, cmd.getP1());
        assertEquals(0, cmd.getP2());
        String expectedData = "C4821D74010027DECAFFED02020401010C53746174757357616C6C657410696D2F7374617475732F77616C6C6574020021002700210013002902B600401581015D0301000006EB372C0024000A013504010004002904000107A0000000620001050107A0000000620102050107A0000000620101050107A0000000620201030013010F53746174757357616C6C65744170700937060040000000800000FF000100000000800000FF00010000000080000D000A010A000004EB05A005F307140739080C08D608EB0908090D008203160010070100000A7B07158106005A801A0076003003808009038B00300457800904620030051F8010";
        byte[] data = cmd.getData();
        assertEquals(expectedData, HexUtils.byteArrayToHexString(data));

        // Command 2
        cmd = commands.get(1);
        assertEquals(0, cmd.getP1());
        assertEquals(1, cmd.getP2());
        expectedData = "053100440A9380AB0B4000750EBF80930F5400300110188C002F7A0302058D00327F003307038D00347F003506038D00377F00381006038D00347F003B032F101B038D00407F004110141020038D0042940000437F005E700B2C017F00411100802F10651C41048D005F7F00607A0861032906181D2510805310806B1E7B00601606590601033816061A7B006016068E03006113412906702B1B7B0060038E03006213044329067B0060037B00601606250453600506700305381606054704412906181D7B00601606078D006329061504160510207B00600316067B006016068D00647B006016067B0065038D0066620403781A7B0060";
        data = cmd.getData();
        assertEquals(expectedData, HexUtils.byteArrayToHexString(data));

        // Last command
        cmd = commands.get(30);
        assertEquals(0x80, cmd.getP1());
        assertEquals(30, cmd.getP2());
        expectedData = "080707080C08090A04050A0A06070706081A07085029031512201209190C0B0B0503060D09030E0B0C0C080A0A0603070706104622070914240A1611130D080B0F0A0D10110905040B2E271205030E0D0D0D0D0807140808030E1E10050321032C2A070606080D140C2723180B081D0A0707060811030D0407070608201B07091408252C2E390A648B59873729EC";
        data = cmd.getData();
        assertEquals(expectedData, HexUtils.byteArrayToHexString(data));
    }
}