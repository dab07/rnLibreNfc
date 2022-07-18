package com.nfcreaderts;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;
import android.content.IntentFilter;
import android.app.Activity;

import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import android.view.MenuItem;
import androidx.annotation.NonNull;

public class MainActivity extends ReactActivity {

    final static String TAG = "nfcReaderTS";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    private static final int PENDING_INTENT_TECH_DISCOVERED = 1;

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
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NO NFC Capabilities", Toast.LENGTH_SHORT).show();
            finish();
        }
//        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);

        // for STEP COUNTER
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            subscribe();
        }

    }
    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed!");
                                } else {
                                    Log.w(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }
    private void readData() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                long total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                Log.i(TAG, "Total steps: " + total);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "There was a problem getting the step count.", e);
                            }
                        });
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the main; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_read_data) {
//            readData();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }



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
                Log.i("NFC", "initial porps set" + isNfcStart);
                mInitialProps.putBoolean("isNfcStart ", isNfcStart);
                return mInitialProps;
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
//        assert nfcAdapter != null;
        if (nfcAdapter == null) {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }
        pendingIntent = createPendingResult(PENDING_INTENT_TECH_DISCOVERED, new Intent(), 0);
        if (pendingIntent != null) {
            try {
                nfcAdapter.enableForegroundDispatch(this, pendingIntent,
                        new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED) },
                        new String[][] { new String[]{"android.nfc.tech.NfcV"}}
                );
            } catch (NullPointerException e) {

            }
        }
//        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNewIntent(Intent data) {
        super.onNewIntent(data);
        setIntent(data);

//        if (intent != null) {
//            if (intent.getAction() != null) {
//                Log.i("NFC", "Intent: " + intent.getAction());
//            }
//            else {
//                Log.i("NFC", "action is null but not Intent");
//            }
//        }
//        else {
//            Log.i("NFC", "Intent = null");
//        }
        Log.i("NFC", "intent: ");
        String action = data.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = data.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            assert tag != null;
//            byte[] id = tag.getId();
//            String[] techList = tag.getTechList();
            Log.i(TAG, "tag found: " + tag);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PENDING_INTENT_TECH_DISCOVERED:
                onNewIntent(data);
                break;
        }
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
