package com.nfcopenreader;

import android.app.PendingIntent;
import com.facebook.react.ReactActivity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;

import com.facebook.react.ReactActivityDelegate;
public class MainActivity extends ReactActivity {

  NfcAdapter nfcAdapter;
  PendingIntent pendingIntent;
  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "NfcOpenReader";
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(null);

    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    if (nfcAdapter == null) {
      Toast.makeText(this,"NO NFC Capabilities", Toast.LENGTH_SHORT).show();
      finish();
    }
    pendingIntent = PendingIntent.getActivity(this,0,
            new Intent(this,this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
  }
  @Override
  protected ReactActivityDelegate createReactActivityDelegate() {
    return new ReactActivityDelegate(this, getMainComponentName()) {

      @Override
      protected Bundle getLaunchOptions() {
        Bundle mInitialProps = new Bundle();
        Boolean isNfcStart = false;
        Intent intent = getIntent();
        Log.i("NFC", "get launch options");
        if (intent != null && intent.getAction() != null && intent.getAction().equals((NfcAdapter.ACTION_TECH_DISCOVERED))) {
          Log.i("NFC", "set flag true");
          isNfcStart = true;
        }
        Log.i("NFC", "initial porps set" + isNfcStart.toString());
        mInitialProps.putBoolean("isNfcStart", isNfcStart);
        return mInitialProps;
      }
    };
  }

  @Override
  protected void onResume() {
    super.onResume();
//    assert nfcAdapter != null;
//    nfcAdapter.enableForegroundDispatch(this,pendingIntent,null,null);
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  protected void onPause() {
    super.onPause();
//    if (nfcAdapter != null) {
//      nfcAdapter.disableForegroundDispatch(this);
//    }
  }

  @Override
  protected void onDestroy() {
//    if (MusicControlModule.INSTANCE != null) {
//      MusicControlModule.INSTANCE.destroy();
//    }
//    mCastContext = null;
    super.onDestroy();
  }

  @Override
  public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    if (intent != null && intent.getAction() != null && intent.getAction().equals((NfcAdapter.ACTION_TECH_DISCOVERED))) {
      Log.i("NFC", "NFC_V tag detected");
    } else {
      Log.e("NFC", "NFCV tag not detected");
//      mCastContext = CastContext.getSharedInstance(this);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
  }

}
