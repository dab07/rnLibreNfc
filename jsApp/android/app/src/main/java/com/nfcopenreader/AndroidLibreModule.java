package com.nfcopenreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
    private AsyncTask<Tag, Void, String> readerTask = null;
    private final String handledIntentFlag = "ALREADY_HANDLED";
    private Boolean isAudioEnabledFlag = false;
    public static MediaPlayer mediaPlayer = null;

    AndroidLibreModule(ReactApplicationContext context) {
        super(context);
    }

    public native byte[] commander1(int i11, int min);

    public native int commander2(int i12, int i13);

    public native int commander3(int i10, int i13);

    public native int getCommanderMin(int i9, int i10);

    public native int getCommanderi11(int i8, int i10);

    public native int getCommanderi3(int i);

    public native int getCommanderi4(int i, int i3);

    public native int getCommanderi5(int i2, int i3);

    public native int getCommanderi6(int i5);

    public native int getCommanderi7(int i6, int i5);

    public native int getCommanderi8(int i4);

    public native int getCommanderi9(int i7);

    public native int getCommanderi10(int i7);

    public native boolean checkForNextStep(int i10, int i9);

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
        return "nfcopenreader";
    }

    @ReactMethod
    public void openNfcSettings(final Promise settingsPromise) {
        try{
            getCurrentActivity().startActivityForResult(new Intent(Settings.ACTION_NFC_SETTINGS),0);
            settingsPromise.resolve(null);
        }catch (Exception e){
            settingsPromise.reject(e);
        }
    }


    @ReactMethod
    public void startReadingFromCgmPatch(int startIndex, final Promise sugarReading) {
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
            Log.e("Android_Libre_Module","startReadingFromCgmPatch failed", e);
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
    public void stopReadingFromCgmPatch(final Promise promise) {
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
            Log.e("CGM_Reader_Module","stopReadingFromCgmPatch failed", e);
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
        addLog("isSensorDetected from native => " + isSensorDetected);
        promise.resolve(isSensorDetected);
    }

//    @ReactMethod
//    public void checkForNfcOnDevice(final Promise promise) {
//        try {
//            NfcManager manager = (NfcManager) getReactApplicationContext().getSystemService(Context.NFC_SERVICE);
//            NfcAdapter adapter = manager.getDefaultAdapter();
//            WritableNativeMap nfcStatus = new WritableNativeMap();
//            if (adapter != null && adapter.isEnabled()) {
//                nfcStatus.putBoolean("isNfcAdapterAvailable", true);
//                nfcStatus.putBoolean("isNfcAdapterEnabled", true);
//            } else if (adapter != null && !adapter.isEnabled()) {
//                nfcStatus.putBoolean("isNfcAdapterAvailable", true);
//                nfcStatus.putBoolean("isNfcAdapterEnabled", false);
//            } else {
//                nfcStatus.putBoolean("isNfcAdapterAvailable", false);
//                nfcStatus.putBoolean("isNfcAdapterEnabled", false);
//            }
//            promise.resolve(nfcStatus);
//        } catch (Exception e) {
//            Log.e("CGM_Reader_Module","checkForNfcOnDevice failed", e);
//            Log.e("NFC Error", e.toString());
//            WritableNativeMap nfcError = new WritableNativeMap();
//            nfcError.putString("errorCode", AndroidLibre_EVENTS.NFC_CHECK_FAIL.toString());
//            nfcError.putString("error", e.toString());
//            nfcError.putString("logs", log);
//            promise.reject(e, nfcError);
//        }
//    }

    private void handleIntent(Intent intent) throws Exception {
        addLog("Handle Intent Called");
        String action = intent.getAction();
        addLog("Extracted action from intent" + action);
//        String actTechDis = NfcAdapter.ACTION_TECH_DISCOVERED;
//        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            addLog("Intent action :->" + NfcAdapter.ACTION_TECH_DISCOVERED);
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            intent.putExtra(handledIntentFlag, true);
            sendEvent(AndroidLibre_EVENTS.NFC_INITIALISATION_DONE, null, null);
            addLog("Instance of NFC Tag Created");
            String[] techList = tag.getTechList();
            sendEvent(AndroidLibre_EVENTS.NFC_TAG_INSTANCE_CREATED, null, null);
            addLog("Supported tech list created ->" + techList.toString());
            String searchedTech = NfcV.class.getName();
            addLog("Starting Async Task");
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
        Log.d("CGM_Reader_Module", logStr);
    }

    private boolean checkEmptyBytes(byte[] block) {
        for (int i = 0; i < block.length; i++) {
            if (block[i] != 0) {
                return false;
            }
        }
        return true;
    }

    private void playReadingInProgressTone() {
        try {
            ToneGenerator readingInProgressTone = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            readingInProgressTone.startTone(ToneGenerator.TONE_CDMA_PIP, 100);
        } catch (Exception e) {
            Log.e("CGM_Reader_Module", "playReadingInProgressTone failed", e);
            addLog("Failed to play playReadingInProgressTone.");
        }
    }

//    private void playReadingCompleteTone() {
//        try {
//            ToneGenerator readingInProgressTone = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
//            readingInProgressTone.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 500);
//        } catch (Exception e) {
//            Log.e("CGM_Reader_Module", "playReadingCompleteTone failed", e);
//            addLog("Failed to play playReadingCompleteTone.");
//        }
//    }

//    private class AudioNotificationPlayer {
//
//        protected void execute(@NonNull MESSAGE_TONE_NAME... message_tone_names) {
//            MESSAGE_TONE_NAME toneName = message_tone_names[0];
//            Log.d("NFC_AUDIO_CGM", "Do in BG Called " + toneName.toString());
//            if (!isAudioEnabledFlag) return;
//            try {
//                Log.d("NFC_AUDIO_CGM", "Entered Try block " + toneName.toString());
//
//                if (toneName == MESSAGE_TONE_NAME.FAILED) {
//                    mediaPlayer = MediaPlayer.create(getReactApplicationContext(), R.raw.nfc_reading_failed);
//                    Log.d("NFC_AUDIO_CGM", "Instantiated " + toneName.toString());
//                } else if (toneName == MESSAGE_TONE_NAME.FINISHED) {
//                    mediaPlayer = MediaPlayer.create(getReactApplicationContext(), R.raw.nfc_reading_finished);
//                    Log.d("NFC_AUDIO_CGM", "Instantiated " + toneName.toString());
//                } else if (toneName == MESSAGE_TONE_NAME.STARTED) {
//                    mediaPlayer = MediaPlayer.create(getReactApplicationContext(), R.raw.nfc_reading_started);
//                    Log.d("NFC_AUDIO_CGM", "Instantiated " + toneName.toString());
//                }
//                mediaPlayer.setWakeMode(getReactApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
//                mediaPlayer.start();
//                Log.d("NFC_AUDIO_CGM", "Started " + toneName.toString());
//            }catch (Exception e){
//                Log.e("NFC_AUDIO_CGM", e.toString());
//            }finally {
//                if(mediaPlayer != null) {
//                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                        @Override
//                        public void onCompletion(MediaPlayer mp) {
//                            if (mediaPlayer != null) {
//                                mediaPlayer.reset();
//                                mediaPlayer.stop();
//                                mediaPlayer.release();
//                            }
//                            mediaPlayer = null;
//                            Log.d("NFC_AUDIO_CGM", "released");
//                        }
//                    });
//                }
//            }
//
//        }
//    }

    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     */
    private class NfcVReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            try {
                // add code here
                if ("Done...".equals(result)) {
                    WritableNativeMap nfcData = new WritableNativeMap();
                    nfcData.putString("value", readData);
                    nfcData.putString("logs", log);
                    Log.d("CGM_Reader_Module", "Success callback called");
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
                Log.e("CGM_Reader_Module", "onPostExecute failed", e);
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
            Boolean isConnected = false;
            long starTime = System.currentTimeMillis();
            long currentTimeMillis = starTime;
            while (!isConnected) {
                try {
                    nfcvTag.connect();
                    isConnected = true;
                } catch (IOException e) {
                    addLog("Connection failed retrying" + e.toString());
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
                Log.e("CGM_Reader_Module", "connectToTagAndReadData failed", e);
                addLog("Error occured while connecting");
                addLog(e.toString()

                );
                sendEvent(AndroidLibre_EVENTS.NFC_TAG_CONNECTION_FAILED, null, null);
                return "Failed...";
            }
            readData = "";
            addLog("readData variable intialised with empty string");

            byte[][] bloques = new byte[2510][8];
            byte[] total = new byte[16001];

            try {
                addLog("Entered readData try block");
                int totalIdx = 0;
                addLog("Loop started");
                int numberOfEmptyByes = 0;

                for (int i = 0; i <= 1200; i++) {

                    if (i > MUST_READ_TO_INDEX && i < startIndex) {
                        readData = readData + "" + ", ";
                        continue;
                    }

                    byte[] oneBlock = new byte[0];

                    while (!this.isCancelled()) {
                        try {
                            if (i % 30 == 0) {
                                playReadingInProgressTone();
                                int readingIndex = (i/100) + 1;
                                sendEvent(AndroidLibre_EVENTS.ONE_BLOCK_READ,Integer.toString(readingIndex) , null);
                            }
                            oneBlock = readPatchFram(nfcvTag, i * 8, 8);
                            String currentBytesToHex = bytesToHex(oneBlock);
//              addLog("At loc " + i + " " + currentBytesToHex);
                            readData = readData + currentBytesToHex + ", ";
                            break;
                        } catch (TagLostException tagLostException) {
                            Log.e("Android_Libre_Module","connectToTagAndReadData failed", tagLostException);
                        } catch (IOException e) {
                            Log.e("Android_Libre_Module","connectToTagAndReadData failed", e);
                            addLog("Error in transceive " + e.toString());
                            sendEvent(AndroidLibre_EVENTS.NFC_ERROR_IN_TRANSIEVE, null, null);
                        }
                    }
                    oneBlock = Arrays.copyOfRange(oneBlock, 0, oneBlock.length);
                    bloques[i] = Arrays.copyOf(oneBlock, 8);
                    if (checkEmptyBytes(bloques[i])) {
                        numberOfEmptyByes++;
                    } else {
                        numberOfEmptyByes = 0;
                    }

                    if (numberOfEmptyByes >= 20) {
                        // when we have reached the end of data
                        break;
                    }

                    for (int idx = 0; idx < bloques[i].length; idx++) {
                        total[totalIdx++] = bloques[i][idx];
                    }
                }

            } catch (Exception e) {
                Log.e("CGM_Reader_Module","connectToTagAndReadData failed", e);
                addLog("Error in transieve" + e.toString());
                sendEvent(AndroidLibre_EVENTS.NFC_ERROR_IN_TRANSIEVE, null, null);
                return null;
            }
            try {
                nfcvTag.close();
            } catch (IOException e) {
                Log.e("CGM_Reader_Module", "Nfc Close failed", e);
                addLog(e.toString());
                addLog(log);
                return null;
            }
            addLog("Reading data finished");
//            playReadingCompleteTone();
            finalValue = total.clone();
            return "Done...";
        }

        @Override
        protected String doInBackground(Tag... params) {
            return connectToTagAndReadData(params);
        }

        public byte[] readPatchFram(NfcV nfcvTag, int i, int i2) throws IOException {
            int i3 = getCommanderi3(i);
            int i4 = getCommanderi4(i, i3);
            int i5 = getCommanderi5(i2, i3);
            int i6 = getCommanderi6(i5);
            int i7 = getCommanderi7(i6, i5);
            int i8 = getCommanderi8(i4);
            byte[] bArr = new byte[i7];
            int i9 = getCommanderi9(i7);
            int i10 = getCommanderi10(i7);
            while (checkForNextStep(i10, i9)) {
                int i11 = getCommanderi11(i8, i10);
                int min = getCommanderMin(i9, i10);
                byte[] tranceiveWithRetries = tranceiveWithRetries(nfcvTag, commander1(i11, min));
                if (!responseIsSuccess(tranceiveWithRetries) || tranceiveWithRetries.length < (min * 8) + 1) {
                    return null;
                }
                for (int i12 = 0; i12 < min; i12++) {
                    for (int i13 = 0; i13 < 8; i13++) {
                        bArr[(i10 * 8) + i13] = tranceiveWithRetries[(i12 * 8) + i13 + 1];
                    }
                    i10++;
                }
            }
            return Arrays.copyOfRange(bArr, i3, i5);
        }

        public boolean responseIsSuccess(byte[] bArr) {
            return bArr != null && bArr.length > 0 && (bArr[0] & 1) == 0;
        }

        public byte[] tranceiveWithRetries(NfcV nfcvTag, byte[] bArr) {
            int i = 0;
            IOException e = null;
            byte[] bArr2 = null;
            while (i < 43 && !this.isCancelled()) {
                try {
                    byte[] transceive = nfcvTag.transceive(bArr);
                    if (responseIsSuccess(transceive)) {
                        return transceive;
                    }
                    i++;
                    bArr2 = transceive;
                } catch (IOException e2) {
                    Log.e("CGM_Reader_Module","tranceiveWithRetries failed", e2);
                    addLog(e2.toString());
                }
            }
            if (e != null) {
                return null;
            }
            return bArr2;
        }
    }
}
