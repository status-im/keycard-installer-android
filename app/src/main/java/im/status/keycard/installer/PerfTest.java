package im.status.keycard.installer;

import android.util.Log;

import im.status.keycard.io.CardChannel;
import im.status.keycard.applet.KeycardCommandSet;

import java.security.SecureRandom;

public class PerfTest {
  private CardChannel cardChannel;
  private KeycardCommandSet cmdSet;

  private long openSecureChannelTime = 0;
  private long loadKeysTime = 0;
  private long loginTime = 0;
  private long signTime = 0;
  private long getStatusTime = 0;

  static final String BIP44_WALLET_PATH = "m/44'/60'/0'/0/0";

  public PerfTest(CardChannel cardChannel) {
    this.cardChannel = cardChannel;
  }

  public void test() throws Exception {
    cmdSet = new KeycardCommandSet(cardChannel);
    cmdSet.select().checkOK();

    cmdSet.autoPair(cmdSet.pairingPasswordToSecret(Secrets.testSecrets().getPairingPassword()));
    openSecureChannelTime = System.currentTimeMillis();
    cmdSet.autoOpenSecureChannel();
    openSecureChannelTime = System.currentTimeMillis() - openSecureChannelTime;
    cmdSet.verifyPIN("000000").checkOK();
    cmdSet.unpairOthers(); // Recover in case of non-clean termination
    Logger.i("Measuring performances. Logging disabled. Please wait");
    Logger.setLevel(Log.INFO);

    try {
      loadKeys();
      getStatus();
      login();
      signTransactions();
    } finally {
      Logger.setLevel(Log.INFO);
      Logger.setUILevel(Log.INFO);
    }

    Logger.i("Reenabling logging.");
    cmdSet.select().checkOK();
    cmdSet.autoOpenSecureChannel();
    cmdSet.verifyPIN("000000").checkOK();
    cmdSet.autoUnpair();
    Logger.i("*************************************************");
    Logger.i("Opening Secure Channel: " + openSecureChannelTime + "ms");
    Logger.i("Derivation of m/44'/60'/0'/0/0 from master: " + loadKeysTime + "ms");
    Logger.i("All following measurements are from application selection to the last needed APDU");
    Logger.i("GET STATUS: " + getStatusTime + "ms");
    Logger.i("Login: " + loginTime + "ms");
    Logger.i("Transaction signature: " + signTime + "ms");
  }

  private void getStatus() throws Exception {
    long time = System.currentTimeMillis();
    cmdSet.select().checkOK();
    cmdSet.autoOpenSecureChannel();
    cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_APPLICATION).checkOK();
    getStatusTime = System.currentTimeMillis() - time;
  }

  private void login() throws Exception {
    long time = System.currentTimeMillis();
    cmdSet.select().checkOK();
    cmdSet.autoOpenSecureChannel();
    cmdSet.verifyPIN("000000").checkOK();
    cmdSet.exportKey("m/43'/60'/1581'/0'/0",false, false).checkOK();
    cmdSet.exportKey("m/43'/60'/1581'/1'/0",false, false).checkOK();
    loginTime = System.currentTimeMillis() - time;
  }

  private void loadKeys() throws Exception {
    byte[] seed = new byte[64];
    new SecureRandom().nextBytes(seed);

    cmdSet.loadKey(seed).checkOK();

    long time = System.currentTimeMillis();
    cmdSet.deriveKey(BIP44_WALLET_PATH).checkOK();
    loadKeysTime = System.currentTimeMillis() - time;
  }

  private void signTransactions() throws Exception {
    long time = System.currentTimeMillis();
    cmdSet.select().checkOK();
    cmdSet.autoOpenSecureChannel();
    cmdSet.verifyPIN("000000").checkOK();
    cmdSet.sign("any32bytescanbeahashyouknowthat!".getBytes()).checkOK();
    signTime = System.currentTimeMillis() - time;
  }
}
