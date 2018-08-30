package im.status.applet_installer_test.appletinstaller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private NfcAdapter nfcAdapter;
    private TextView textView;
    private Button buttonInstall;
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
                disableInstallButton();
                try {
                    install();
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
    public void onTagDiscovered (Tag tag) {
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
        this.enableInstallButton();
    }

    public void enableInstallButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonInstall.setEnabled(true);
            }
        });
    }

    public void disableInstallButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonInstall.setEnabled(false);
            }
        });
    }
}

