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
import java.security.Security;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback, LogListener {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private NfcAdapter nfcAdapter;
    private TextView textView;
    private Button buttonInstall;
    private Button buttonPerfTest;
    private Tag tag;
    private boolean installationAttempted;
    private TagManager tagManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Logger.setListener(this);
        this.tagManager = new TagManager(nfcAdapter);
        this.tagManager.start();
        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        buttonInstall = (Button) findViewById(R.id.buttonInstall);
        buttonInstall.setEnabled(false);
        buttonInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableButtons();
                try {
                    install();
                } catch (APDUException e) {
                    logException(e);
                } catch (IOException e) {
                    logException(e);
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
            nfcAdapter.enableReaderMode(this, this.tagManager,
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
        Logger.log("tag found");
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

    public void log(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(s + "\n");
            }
        });
    }
}

