package com.nfcreaderts;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;

public class MainActivity extends ReactActivity {

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "nfcReaderTS";
    }

    /**
     * Returns the instance of the {@link ReactActivityDelegate}. There the RootView is created and
     * you can specify the rendered you wish to use (Fabric or the older renderer).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NO NFC Capabilities", Toast.LENGTH_SHORT).show();
            finish();
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
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
        super.onDestroy();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.getAction() != null && intent.getAction().equals(( NfcAdapter.ACTION_TECH_DISCOVERED ))) {
            Log.i("NFC", "NFC_V tag detected");
        } else {
            Log.e("NFC", "NFCV tag not detected");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static class MainActivityDelegate extends ReactActivityDelegate {
        public MainActivityDelegate(ReactActivity activity, String mainComponentName) {
            super(activity, mainComponentName);
        }

        @Override
        protected ReactRootView createRootView() {
            ReactRootView reactRootView = new ReactRootView(getContext());
            // If you opted-in for the New Architecture, we enable the Fabric Renderer.
            reactRootView.setIsFabric(BuildConfig.IS_NEW_ARCHITECTURE_ENABLED);
            return reactRootView;
        }
    }

}
