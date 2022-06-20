package com.nfcopenreader;

import com.facebook.react.ReactActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import android.view.View;
import android.app.Application;
import android.content.Context;
import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.soloader.SoLoader;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;
import com.nfcopenreader.AndroidLibrePackage;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
public class MainActivity extends ReactActivity {

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
  }
  @Override
  protected ReactActivityDelegate createReactActivityDelegate() {
    return new ReactActivityDelegate(this, getMainComponentName()) {

      @Override
      protected Bundle getLaunchOptions() {
        Bundle mInitialProps = new Bundle();
        Boolean isNfcStart = true;
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
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  protected void onPause() {
    super.onPause();
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
//    RNBranchModule.onNewIntent(intent);
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
