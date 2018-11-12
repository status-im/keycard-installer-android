package im.status.applet_installer_test.appletinstaller;

import android.util.Log;

import im.status.hardwallet_lite_android.io.CardChannel;
import im.status.hardwallet_lite_android.wallet.WalletAppletCommandSet;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.math.ec.FixedPointUtil;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Random;

public class PerfTest {
  private CardChannel cardChannel;
  private WalletAppletCommandSet cmdSet;

  private long openSecureChannelTime = 0;
  private long loadKeysTime = 0;
  private long loginTime = 0;
  private long signTime = 0;
  private long deriveKeyFromParent = 0;
  private long getStatusTime = 0;
  private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
  public static final ECDomainParameters CURVE;

  static {
    FixedPointUtil.precompute(CURVE_PARAMS.getG(), 12);
    CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
  }

  // m/44'/60'/0'/0/0
  static final byte[] BIP44_PATH = new byte[] { (byte) 0x80, 0x00, 0x00, 0x2c, (byte) 0x80, 0x00, 0x00, 0x3c, (byte) 0x80, 0x00, 0x00, 0x00, (byte) 0x00, 0x00, 0x00, 0x00, (byte) 0x00, 0x00, 0x00, 0x00};

  public PerfTest(CardChannel cardChannel) {
    this.cardChannel = cardChannel;
  }

  public void test() throws Exception {
    cmdSet = new WalletAppletCommandSet(cardChannel);
    cmdSet.select().checkOK();
    cmdSet.autoPair(Secrets.testSecrets().getPairingToken());
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
    Logger.i("Transaction signature (after login): " + signTime + "ms");
    Logger.i("Transaction signature (subsequent): " + (signTime - deriveKeyFromParent) + "ms");
  }

  private void getStatus() throws Exception {
    long time = System.currentTimeMillis();
    cmdSet.select().checkOK();
    cmdSet.autoOpenSecureChannel();
    cmdSet.getStatus(WalletAppletCommandSet.GET_STATUS_P1_APPLICATION).checkOK();
    getStatusTime = System.currentTimeMillis() - time;
  }

  private void login() throws Exception {
    long time = System.currentTimeMillis();
    cmdSet.select().checkOK();
    cmdSet.autoOpenSecureChannel();
    cmdSet.verifyPIN("000000").checkOK();
    cmdSet.deriveKey(new byte[] { (byte) 0xC0, 0x00, 0x00, 0x00}, WalletAppletCommandSet.DERIVE_P1_SOURCE_PARENT).checkOK();
    cmdSet.exportKey(WalletAppletCommandSet.EXPORT_KEY_P1_HIGH, false).checkOK();
    cmdSet.deriveKey(new byte[] { (byte) 0xC0, 0x00, 0x00, 0x01}, WalletAppletCommandSet.DERIVE_P1_SOURCE_PARENT).checkOK();
    cmdSet.exportKey(WalletAppletCommandSet.EXPORT_KEY_P1_HIGH, false).checkOK();
    loginTime = System.currentTimeMillis() - time;
  }

  private void loadKeys() throws Exception {
    KeyPairGenerator g = keypairGenerator();
    KeyPair keyPair = g.generateKeyPair();
    byte[] chainCode = new byte[32];
    new Random().nextBytes(chainCode);

    cmdSet.loadKey(keyPair, false, chainCode).checkOK();

    long time = System.currentTimeMillis();
    cmdSet.deriveKey(BIP44_PATH, WalletAppletCommandSet.DERIVE_P1_SOURCE_CURRENT).checkOK();
    loadKeysTime = System.currentTimeMillis() - time;
  }

  private void signTransactions() throws Exception {
    long time = System.currentTimeMillis();
    cmdSet.select().checkOK();
    cmdSet.autoOpenSecureChannel();
    cmdSet.verifyPIN("000000").checkOK();
    deriveKeyFromParent = System.currentTimeMillis();
    cmdSet.deriveKey(new byte[] { (byte) 0x00, 0x00, 0x00, 0x00}, WalletAppletCommandSet.DERIVE_P1_SOURCE_PARENT).checkOK();
    deriveKeyFromParent = System.currentTimeMillis() - deriveKeyFromParent;
    cmdSet.sign("any32bytescanbeahashyouknowthat!".getBytes()).checkOK();
    signTime = System.currentTimeMillis() - time;
  }

  private KeyPairGenerator keypairGenerator() throws Exception {
    ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
    KeyPairGenerator g = KeyPairGenerator.getInstance("ECDH");
    g.initialize(ecSpec);

    return g;
  }
}
