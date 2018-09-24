package im.status.applet_installer_test.appletinstaller;

import android.util.Log;

import im.status.applet_installer_test.appletinstaller.apducommands.SecureChannelSession;
import im.status.applet_installer_test.appletinstaller.apducommands.WalletAppletCommandSet;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.DLSequence;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

public class PerfTest {
  private CardChannel cardChannel;
  private WalletAppletCommandSet cmdSet;
  private SecureChannelSession secureChannel;

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

    byte[] tmp;

    try {
      tmp = Crypto.generatePairingKey(new char[] {'W', 'a', 'l', 'l', 'e', 't', 'A','p', 'p', 'l', 'e', 't', 'T', 'e', 's', 't'});
    } catch (Exception e) {
      tmp = null;
    }

    SHARED_SECRET = tmp;
  }

  static final byte DERIVE_P1_SOURCE_MASTER = (byte) 0x00;
  static final byte DERIVE_P1_SOURCE_PARENT = (byte) 0x40;
  static final byte DERIVE_P1_SOURCE_CURRENT = (byte) 0x80;
  static final byte EXPORT_KEY_P1_HIGH = 0x01;
  static final byte SIGN_P1_PRECOMPUTED_HASH = 0x01;
  static final byte GET_STATUS_P1_APPLICATION = 0x00;
  static final byte GET_STATUS_P1_KEY_PATH = 0x01;

  // m/44'/60'/0'/0/0
  static final byte[] BIP44_PATH = new byte[] { (byte) 0x80, 0x00, 0x00, 0x2c, (byte) 0x80, 0x00, 0x00, 0x3c, (byte) 0x80, 0x00, 0x00, 0x00, (byte) 0x00, 0x00, 0x00, 0x00, (byte) 0x00, 0x00, 0x00, 0x00};

  // TODO: Make this an input
  public static final byte[] SHARED_SECRET;

  public PerfTest(CardChannel cardChannel) {
    this.cardChannel = cardChannel;
  }

  public void test() throws Exception {
    cmdSet = new WalletAppletCommandSet(cardChannel);
    byte[] keyData = extractPublicKeyFromSelect(cmdSet.select().getData());
    secureChannel = new SecureChannelSession(keyData);
    cmdSet.setSecureChannel(secureChannel);
    cmdSet.autoPair(SHARED_SECRET);
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
    cmdSet.select();
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
    cmdSet.select();
    cmdSet.autoOpenSecureChannel();
    cmdSet.getStatus(GET_STATUS_P1_APPLICATION).checkOK();
    getStatusTime = System.currentTimeMillis() - time;
  }

  private void login() throws Exception {
    long time = System.currentTimeMillis();
    cmdSet.select();
    cmdSet.autoOpenSecureChannel();
    cmdSet.verifyPIN("000000").checkOK();
    cmdSet.deriveKey(new byte[] { (byte) 0xC0, 0x00, 0x00, 0x00}, DERIVE_P1_SOURCE_PARENT, false, false).checkOK();
    cmdSet.exportKey(EXPORT_KEY_P1_HIGH, false).checkOK();
    cmdSet.deriveKey(new byte[] { (byte) 0xC0, 0x00, 0x00, 0x01}, DERIVE_P1_SOURCE_PARENT, false, false).checkOK();
    cmdSet.exportKey(EXPORT_KEY_P1_HIGH, false).checkOK();
    loginTime = System.currentTimeMillis() - time;
  }

  private void loadKeys() throws Exception {
    KeyPairGenerator g = keypairGenerator();
    KeyPair keyPair = g.generateKeyPair();
    byte[] chainCode = new byte[32];
    new Random().nextBytes(chainCode);

    cmdSet.loadKey(keyPair, false, chainCode).checkOK();

    long time = System.currentTimeMillis();
    cmdSet.deriveKey(BIP44_PATH, DERIVE_P1_SOURCE_CURRENT, false, false).checkOK();
    loadKeysTime = System.currentTimeMillis() - time;
  }

  private void signTransactions() throws Exception {
    long time = System.currentTimeMillis();
    cmdSet.select();
    cmdSet.autoOpenSecureChannel();
    cmdSet.verifyPIN("000000").checkOK();
    deriveKeyFromParent = System.currentTimeMillis();
    cmdSet.deriveKey(new byte[] { (byte) 0x00, 0x00, 0x00, 0x00}, DERIVE_P1_SOURCE_PARENT, false, false).checkOK();
    deriveKeyFromParent = System.currentTimeMillis() - deriveKeyFromParent;
    cmdSet.sign("any32bytescanbeahashyouknowthat!".getBytes(), SIGN_P1_PRECOMPUTED_HASH, true, true).checkOK();
    signTime = System.currentTimeMillis() - time;
  }

  private KeyPairGenerator keypairGenerator() throws Exception {
    ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
    KeyPairGenerator g = KeyPairGenerator.getInstance("ECDH");
    g.initialize(ecSpec);

    return g;
  }

  private byte[] extractPublicKeyFromSelect(byte[] select) {
    return Arrays.copyOfRange(select, 22, 22 + select[21]);
  }

  private byte[] derivePublicKey(byte[] data) throws Exception {
    byte[] pubKey = Arrays.copyOfRange(data, 3, 4 + data[3]);
    byte[] signature = Arrays.copyOfRange(data, 4 + data[3], data.length);
    byte[] hash = MessageDigest.getInstance("SHA256").digest("STATUS KEY DERIVATION".getBytes());

    pubKey[0] = 0x02;

    ECPoint candidate = CURVE.getCurve().decodePoint(pubKey);
    if (!verifySig(hash, signature, candidate)) {
      pubKey[0] = 0x03;
      candidate = CURVE.getCurve().decodePoint(pubKey);
      if (!verifySig(hash, signature, candidate)) {
        throw new Exception("Public key is incorrect");
      }
    }

    return candidate.getEncoded(false);
  }

  private boolean verifySig(byte[] hash, byte[] signature, ECPoint pub) {
    ECDSASigner signer = new ECDSASigner();
    ECPublicKeyParameters params = new ECPublicKeyParameters(pub, CURVE);
    signer.init(false, params);
    ECDSASignature sig =  ECDSASignature.decodeFromDER(signature);
    return signer.verifySignature(hash, sig.r, sig.s);
  }

  static class ECDSASignature {
    /** The two components of the signature. */
    public final BigInteger r, s;

    /**
     * Constructs a signature with the given components. Does NOT automatically canonicalise the signature.
     */
    public ECDSASignature(BigInteger r, BigInteger s) {
      this.r = r;
      this.s = s;
    }

    public static ECDSASignature decodeFromDER(byte[] bytes) {
      ASN1InputStream decoder = null;
      try {
        decoder = new ASN1InputStream(bytes);
        DLSequence seq = (DLSequence) decoder.readObject();
        if (seq == null)
          throw new RuntimeException("Reached past end of ASN.1 stream.");
        ASN1Integer r, s;
        try {
          r = (ASN1Integer) seq.getObjectAt(0);
          s = (ASN1Integer) seq.getObjectAt(1);
        } catch (ClassCastException e) {
          throw new IllegalArgumentException(e);
        }
        // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
        // Thus, we always use the positive versions. See: http://r6.ca/blog/20111119T211504Z.html
        return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        if (decoder != null)
          try { decoder.close(); } catch (IOException x) {}
      }
    }
  }
}
