package im.status.keycard.installer;

import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.nfc.NfcAdapter;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import de.cotech.sweetspot.NfcSweetspotData;
import im.status.keycard.android.NFCCardManager;
import de.cotech.sweetspot.ShowNfcSweetspotActivity;

public class MainActivity extends AppCompatActivity implements UILogger {
    private NfcAdapter nfcAdapter;
    private TextView textView;
    private ScrollView textViewScroll;

    private Button buttonInstall;
    private Button buttonInstallTest;
    private Button buttonPerfTest;
    private Button buttonNFCSpot;
    private ActionRunner actionRunner;
    private NFCCardManager cardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Logger.setUILogger(this);

        AssetManager assets = this.getAssets();
        this.actionRunner = new ActionRunner(assets, "keycard.cap");
        this.cardManager = new NFCCardManager();
        this.cardManager.setCardListener(this.actionRunner);
        cardManager.start();

        textViewScroll = (ScrollView) findViewById(R.id.textViewScroll);

        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());

        buttonInstall = (Button) findViewById(R.id.buttonInstall);
        buttonInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(ActionRunner.ACTION_INSTALL);
            }
        });
        buttonInstallTest = (Button) findViewById(R.id.buttonInstallTest);
        buttonInstallTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(ActionRunner.ACTION_INSTALL_TEST);
            }
        });
        buttonPerfTest = (Button) findViewById(R.id.buttonPerfTest);
        buttonPerfTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(ActionRunner.ACTION_PERFTEST);
            }
        });
        buttonNFCSpot = (Button) findViewById(R.id.buttonNFCSpot);
        buttonNFCSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ShowNfcSweetspotActivity.class);
                startActivity(i);
            }
        });

        if (NfcSweetspotData.hasSweetspotData()) {
            buttonNFCSpot.setEnabled(true);
        }

        //Logger.setUILevel(Log.INFO);
        //Logger.setLevel(Log.INFO);
    }

    private void logException(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            msg = "exception without message";
        }

        Logger.e("exception: " + msg);
    }

    private void requestAction(int action) {
        if (this.actionRunner != null) {
            clearTextView();
            this.actionRunner.requestAction(action);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(this, this.cardManager,
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

    public void log(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(s + "\n");
                //textViewScroll.fullScroll(ScrollView.FOCUS_DOWN);
                textViewScroll.scrollTo(0, textView.getBottom());
            }
        });
    }

    public void clearTextView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText("");
            }
        });
    }
}

