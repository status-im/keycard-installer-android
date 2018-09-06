package im.status.applet_installer_test.appletinstaller;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback, LogListener {

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
        Logger.setListener(this);
        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        buttonInstall = (Button) findViewById(R.id.buttonInstall);
        buttonInstall.setEnabled(false);
        buttonInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableInstallButton();
                try {
                    install();
                } catch (APDUException e) {
                    logException(e);
                } catch (IOException e) {
                    logException(e);
                }
            }
        });
    }

    private void logException(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            msg = "exception without message";
        }

        Logger.log("exception: " + msg);
    }

    public void install() throws IOException, APDUException {
        //if (installationAttempted) {
        //    throw new APDUException("installation already attempted");
        //}

        installationAttempted = true;

        CardManager cm = new CardManager(tag);
        cm.connect();

        AssetManager assets = this.getAssets();
        cm.install(assets, "wallet.cap");
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
        Logger.log("--------------------------\ntag found");
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

    public void log(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(s + "\n");
            }
        });
    }
}

