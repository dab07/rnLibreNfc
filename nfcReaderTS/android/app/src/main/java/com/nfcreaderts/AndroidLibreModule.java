package com.nfcreaderts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.util.Arrays;


enum MESSAGE_TONE_NAME{
    STARTED,FINISHED,FAILED
}
public class AndroidLibreModule extends ReactContextBaseJavaModule {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private String log = "";
    private String readData, buffer;
    private int startIndex = 0;
    private static final int MUST_READ_TO_INDEX = 22;
    private Promise sugarReadingPromise;
    private byte[] finalValue = new byte[9001];
    final public static String CGM_EVENT_NAME = "ABOTT_CGM_EVENT";
    private AsyncTask<Tag, Void, String> readerTask;
    private final String handledIntentFlag = "ALREADY_HANDLED";
    private Boolean isAudioEnabledFlag = false;
    public static MediaPlayer mediaPlayer = null;

    public AndroidLibreModule(ReactApplicationContext context) {
        super(context);
    }

    /**
     * /**
     *
     * @param activity The corresponding {@link } requesting to stop the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void sendEvent(AndroidLibre_EVENTS eventName, @Nullable String extraText, @Nullable WritableMap params) {
        if(params == null) {
            params = new WritableNativeMap();
        }
        if(extraText == null) {
            params.putString("eventName", eventName.toString());
        }else{
            params.putString("eventName", eventName.toString() + " " + extraText);
        }
        this.getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(CGM_EVENT_NAME, params);
    }

    @NonNull
    @Override
    public String getName() {
        return "nfcReaderTS";
    }


    @ReactMethod
    public void startReadingFromLibre(int startIndex, final Promise sugarReading) {
        this.startIndex = startIndex;
        sugarReadingPromise = sugarReading;
        if(this.readerTask != null){
            if(this.readerTask.cancel(true)){
                this.readerTask = null;
            }
        }
        try {
            addLog("Init sensor called -> Handling Intent NFC now");
            Intent intent = getCurrentActivity().getIntent();
            sendEvent(AndroidLibre_EVENTS.NFC_INITIALISATION_STARTED, null, null);
            handleIntent(intent);
        } catch (Exception e) {
            Log.e("Android_Libre_Module","[startReadingFromLibre] failed", e);
            WritableNativeMap nfcError = new WritableNativeMap();
            nfcError.putString("errorCode", AndroidLibre_EVENTS.NFC_INITIALISATION_FAILED.toString());
            nfcError.putString("error", e.toString());
            nfcError.putString("logs", log);
            sugarReadingPromise.reject(e, nfcError);
            sendEvent(AndroidLibre_EVENTS.NFC_INITIALISATION_FAILED, null, null);
            return;
        }
    }

    @ReactMethod
    public void stopReadingFromLibre(final Promise promise) {
        try {
            if (this.readerTask != null) {
                this.readerTask.cancel(true);
                this.readerTask = null;
                mediaPlayer = null;
                sendEvent(AndroidLibre_EVENTS.READING_STOPPED, null, null);
                promise.resolve(true);
            }
            promise.resolve(false);
        } catch (Exception e) {
            Log.e("AndroidLibreModule","[stopReadingFromLibre] failed", e);
            Log.e("NFC Error", e.toString());
            promise.reject(e);
        }
    }

    @ReactMethod
    public void isSensorDetected(final Promise promise) {
        boolean isSensorDetected = false;

        Intent receivedIntnent = getCurrentActivity().getIntent();
        Boolean isIntentAlreadyHandled = false;
        Bundle extras = receivedIntnent.getExtras();
        if (extras != null) {
            isIntentAlreadyHandled = extras.getBoolean(handledIntentFlag);
        }
        String action = receivedIntnent.getAction();
        addLog("Extracted action from intent " + action);
        addLog("isIntentAlreadyHandled value was => " + isIntentAlreadyHandled);

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) && !isIntentAlreadyHandled) {
            isSensorDetected = true;
        }
//        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
//                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
//                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) && !isIntentAlreadyHandled) {
//            isSensorDetected = true;
//        }
        addLog("isSensorDetected from native => " + isSensorDetected);
        promise.resolve(isSensorDetected);
    }

    @ReactMethod
    public void checkForNfcOnDevice(final Promise promise) {
        try {
            NfcManager manager = (NfcManager) getReactApplicationContext().getSystemService(Context.NFC_SERVICE);
            NfcAdapter adapter = manager.getDefaultAdapter();
            WritableNativeMap nfcStatus = new WritableNativeMap();
            if (adapter != null && adapter.isEnabled()) {
                nfcStatus.putBoolean("isNfcAdapterAvailable", true);
                nfcStatus.putBoolean("isNfcAdapterEnabled", true);
            } else if (adapter != null && !adapter.isEnabled()) {
                nfcStatus.putBoolean("isNfcAdapterAvailable", true);
                nfcStatus.putBoolean("isNfcAdapterEnabled", false);
            } else {
                nfcStatus.putBoolean("isNfcAdapterAvailable", false);
                nfcStatus.putBoolean("isNfcAdapterEnabled", false);
            }


            promise.resolve(nfcStatus);
        } catch (Exception e) {
            Log.e("AndroidLibreModule","checkForNfcOnDevice failed", e);
            Log.e("NFC Error", e.toString());
            WritableNativeMap nfcError = new WritableNativeMap();
            nfcError.putString("errorCode", AndroidLibre_EVENTS.NFC_CHECK_FAIL.toString());
            nfcError.putString("error", e.toString());
            nfcError.putString("logs", log);
            promise.reject(e, nfcError);
        }
    }

    private void handleIntent(Intent intent) throws Exception {
        addLog("Handle Intent Called");
        String action = intent.getAction();
        addLog("Extracted action from intent" + action);
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
//        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
//                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
//                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
//            addLog("Intent action: " + NfcAdapter.ACTION_TECH_DISCOVERED);
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            intent.putExtra(handledIntentFlag, true);
            sendEvent(AndroidLibre_EVENTS.NFC_INITIALISATION_DONE, null, null);
            addLog("Instance of NFC Tag Created");
            String[] techList = tag.getTechList();
            sendEvent(AndroidLibre_EVENTS.NFC_TAG_INSTANCE_CREATED, null, null);
            addLog("Supported tech list created ->" + techList.toString());
            String searchedTech = NfcV.class.getName();
            addLog("[NfcVReaderTask]: Starting Async Task");
            readerTask = new NfcVReaderTask();
            readerTask.execute(tag);

        } else {
            addLog("App Was not launched with Tech Discovered intent");
            WritableNativeMap nfcError = new WritableNativeMap();
            nfcError.putString("errorCode", AndroidLibre_EVENTS.NFC_ERROR_NO_TAG_DETECTED.toString());
            nfcError.putString("error", AndroidLibre_EVENTS.NFC_ERROR_NO_TAG_DETECTED.toString());
            nfcError.putString("logs", log);
            Exception tagNotDetected = new Exception(AndroidLibre_EVENTS.NFC_ERROR_NO_TAG_DETECTED.toString());
            sugarReadingPromise.reject(tagNotDetected, nfcError);
            return;
        }
    }

    private void addLog(String logStr) {
        log = log + logStr + "\n";
        Log.d("AndroidLibreModule", logStr);
    }

    private boolean checkEmptyBytes(byte[] block) {
        for (int i = 0; i < block.length; i++) {
            if (block[i] != 0) {
                return false;
            }
        }
        return true;
    }


    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     */
    private class NfcVReaderTask extends AsyncTask<Tag, Void, String> {
        @Override
        protected void onPostExecute(String result) {
            try {
                // add code here
                if ("[onPostExecute]: Done...".equals(result)) {
                    WritableNativeMap nfcData = new WritableNativeMap();
                    nfcData.putString("value", readData);
                    nfcData.putString("logs", log);
                    Log.d("AndroidLibreModule", "Success callback called");
                    sugarReadingPromise.resolve(nfcData);
                } else {
                    WritableNativeMap nfcError = new WritableNativeMap();
                    nfcError.putString("errorCode", AndroidLibre_EVENTS.NFC_ERROR_NO_TAG_DETECTED.toString());
                    nfcError.putString("error", AndroidLibre_EVENTS.NFC_ERROR_NO_TAG_DETECTED.toString());
                    nfcError.putString("logs", log);
                    Exception tagNotDetected = new Exception(AndroidLibre_EVENTS.NFC_ERROR_UNKNOWN.toString());
                    sugarReadingPromise.reject(tagNotDetected, nfcError);
                }
            } catch (Exception e) {
                Log.e("AndroidLibreModule", "onPostExecute failed", e);
                WritableNativeMap nfcError = new WritableNativeMap();
                nfcError.putString("errorCode", AndroidLibre_EVENTS.NFC_ERROR_NO_TAG_DETECTED.toString());
                nfcError.putString("error", AndroidLibre_EVENTS.NFC_ERROR_NO_TAG_DETECTED.toString());
                nfcError.putString("logs", log);
                sugarReadingPromise.reject(e, nfcError);
            } finally {
                this.cancel(true);
            }
        }


        private void connectWithRetries(NfcV nfcvTag) throws IOException {
            nfcvTag.isConnected();
            Boolean isConnected = false;
            long starTime = System.currentTimeMillis();
            long currentTimeMillis = starTime;
            while (!isConnected) {
                try {
                    nfcvTag.connect();
                    isConnected = true;
                } catch (IOException e) {
                    addLog("Connection failed retrying " + e.toString());
                    if ((currentTimeMillis - starTime) > 30000) {
                        throw (e);
                    }
                    synchronized (this) {
                        try {
                            wait(1000);
                        } catch (InterruptedException ie) {
                            addLog("Interrupted exception at wait");
                        }
                    }
                }
            }
        }

        private String connectToTagAndReadData(Tag... params) {
            Tag tag = params[0];
            NfcV nfcvTag = NfcV.get(tag);

            addLog("Enter NdefReaderTask: " + nfcvTag.toString());
            addLog("Tag ID: " + tag.getId());
            sendEvent(AndroidLibre_EVENTS.NFC_READ_STARTED, null, null);
            try {
                addLog("Trying to connect to tag");
                connectWithRetries(nfcvTag);
                sendEvent(AndroidLibre_EVENTS.NFC_TAG_CONNECTED, null, null);
                addLog("Connection Successful");
            } catch (Exception e) {
                Log.e("AndroidLibreModule", "connectToTagAndReadData failed", e);
                addLog("Error occured while connecting");
                addLog(e.toString()

                );
                sendEvent(AndroidLibre_EVENTS.NFC_TAG_CONNECTION_FAILED, null, null);
                return "Failed...";
            }
            readData = "";
            addLog("readData variable intialised with empty string");

            try {
                byte[] cmd = new byte[] {
                        (byte)0x00, // Flags
                        (byte)0x2B // Command: Get system information
                };
                byte[] data = nfcvTag.transceive(cmd);

                data = Arrays.copyOfRange(data, 2, data.length - 1);
                addLog("data: " + data);
            } catch (IOException e) {
                addLog("Unable to transceive");
            }
            return "done..";
        }
        @Override
        protected String doInBackground(Tag... params) {
            return connectToTagAndReadData(params);
        }

    }
}
