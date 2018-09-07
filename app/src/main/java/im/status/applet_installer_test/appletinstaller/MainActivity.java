package im.status.applet_installer_test.appletinstaller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.security.Security;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private NfcAdapter nfcAdapter;
    private TextView textView;
    private Button buttonInstall;
    private Button buttonPerfTest;
    private Tag tag;
    private boolean installationAttempted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        textView = (TextView) findViewById(R.id.textView);
        buttonInstall = (Button) findViewById(R.id.buttonInstall);
        buttonInstall.setEnabled(false);
        buttonInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableButtons();
                try {
                    install();
                } catch (Exception e) {
                    Logger.log(e.getMessage());
                }
            }
        });
        buttonPerfTest = (Button) findViewById(R.id.buttonPerfTest);
        buttonPerfTest.setEnabled(false);
        buttonPerfTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableButtons();
                try {
                    perfTest();
                } catch (Exception e) {
                    Logger.log(e.getMessage());
                }
            }
        });
    }

    public void install() throws Exception {
        if (installationAttempted) {
            throw new Exception("installation already attempted");
        }

        installationAttempted = true;

        Logger.log("installing");
        CardManager cm = new CardManager(tag);
        cm.connect();
        cm.install();
    }

    public void perfTest() throws Exception {
        Logger.log("Starting performance tests");
        PerfTest pf = new PerfTest(tag);
        pf.connect();
        pf.test();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(this, this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        try {
            start(tag);
        } catch (final IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.append("\nexception: " + e.getMessage());
                }
            });
        }
    }

    private void start(Tag tag) throws IOException {
        this.tag = tag;
        Logger.log("tag found");
        this.enableButtons();
    }

    public void enableButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonInstall.setEnabled(true);
                buttonPerfTest.setEnabled(true);
            }
        });
    }

    public void disableButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonInstall.setEnabled(false);
                buttonPerfTest.setEnabled(false);
            }
        });
    }
}

