package com.nfcopenreader;

import com.facebook.react.ReactActivity;
import android.app.Application;
import android.content.Context;
import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.soloader.SoLoader;
import com.nfcopenreader.AndroidLibrePackage;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.nfc.NfcAdapter;
//import android.nfc.NfcManager;
//import android.nfc.Tag;
//import android.os.Bundle;
//import android.preference.PreferenceManager;
//import android.support.design.widget.TabLayout;
//import android.support.v4.app.DialogFragment;
//import android.support.v4.app.Fragment;
//import android.support.v4.view.ViewPager;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.Toast;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.gson.Gson;
//import com.google.gson.stream.JsonWriter;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.concurrent.TimeUnit;
//
//import io.realm.Realm;
//import io.realm.RealmResults;
//import io.realm.Sort;

public class MainActivity extends ReactActivity {
//    private NfcAdapter = mNfcAdapter;
//    private SectionsPagerAdapter mSectionsPagerAdapter;
//
//  @Override
//    public void onStart() {
//        super.onStart();
//
//        String uid = "non-authorized";
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null)
//            uid = currentUser.getUid();
//        Log.d(LOG_ID, "User:" + uid);
//    }
//  @Override
//    public void onResume() {
//      super.onResume();
//      if (mNfcAdapter == null) {
//          mNfcAdapter = ((NfcManager) this.getSystemService(Context.NFC_SERVICE)).getDefaultAdapter();
//      }
//
//      if (mNfcAdapter != null) {
//          try {
//              mNfcAdapter.isEnabled();
//          } catch (NullPointerException e) {
//              // Drop NullPointerException
//          }
//          try {
//              mNfcAdapter.isEnabled();
//          } catch (NullPointerException e) {
//              // Drop NullPointerException
//          }
//
//          PendingIntent pi = createPendingResult(PENDING_INTENT_TECH_DISCOVERED, new Intent(), 0);
//          if (pi != null) {
//              try {
//                  mNfcAdapter.enableForegroundDispatch(this, pi,
//                          new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED) },
//                          new String[][] { new String[]{"android.nfc.tech.NfcV"} }
//                  );
//              } catch (NullPointerException e) {
//                  // Drop NullPointerException
//              }
//          }
//      }
//  }
  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "NfcOpenReader";
  }
}
