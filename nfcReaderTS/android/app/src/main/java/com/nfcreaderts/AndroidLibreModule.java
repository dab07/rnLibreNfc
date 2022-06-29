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
import java.util.*;


enum MESSAGE_TONE_NAME{
    STARTED,FINISHED,FAILED
}
public class AndroidLibreModule extends ReactContextBaseJavaModule {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private String log = "";
    private String readData;
    private int startIndex = 0;
    private Promise sugarReadingPromise;
    private byte[] finalValue = new byte[9001];
    final public static String CGM_EVENT_NAME = "ABOTT_CGM_EVENT";
    private AsyncTask<Tag, Void, String> readerTask;
    private final String handledIntentFlag = "ALREADY_HANDLED";

    public long lastReadTime = 0;
    public int lastTVal = 0;
    public int lastDenseGVal = 0;
    public int lastDenseTVal = 0;
    public int lastSparseTVal = 0;
    public int lastSparseGVal = 0;
    public int lastSensorTime = 0;
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
        public String getSensorID(String tagID) {
            int [] Is = new int[]{0,0,0,0,0,0};
            int [] Js = new int[]{0,0,0,0,0,0,0,0,0,0};
            final String l = "0123456789ACDEFGHJKLMNPQRTUVWXYZ";
            for(int i=0;i<Is.length;i++) {
                Is[i] = Integer.parseInt(tagID.substring(((6 - i) * 2)-2, ((6 - i) * 2)), 16) & 255;
            }

            Js[0] = (Is[0] >> 3);
            Js[1] = (((Is[0] & 7) << 2) | (Is[1] >> 6));
            Js[2] = ((Is[1] >> 1) & 31);
            Js[3] = (((Is[1] & 1) << 4) | (Is[2] >> 4));
            Js[4] = (((Is[3-1] & 15) << 1) | (Is[3] >> 7));
            Js[5] = ((Is[3] >> 2) & 31);
            Js[6] = (((Is[3] & 3) << 3) | (Is[4] >> 5));
            Js[7] = (Is[4] & 31);
            Js[8] = (Is[5] >> 3);
            Js[9] = ((Is[5] << 2) & 31);

            String sensorID = "";
            for(int i = 0; i < Js.length; i++) {
                sensorID += l.charAt(Js[i]);
            }
            return sensorID;
        }
        private String getBlock(NfcV nfcTag, int blockNo) throws java.io.IOException {
            byte [] cmd = new byte[]{
                    (byte) 0x20, // Flags
                    (byte) 0x20, // Command: Read multiple blocks
                    0,0,0,0,0,0,0,0,
                    (byte) (blockNo+3) // block (offset)
            };
            System.arraycopy(nfcTag.getTag().getId(),0,cmd,2,8);
            String readPosString = bytesToHex(Arrays.copyOfRange(nfcTag.transceive(cmd),1,9));
            return readPosString;
        }
        private int timeSinceStart(NfcV nfcTag) throws java.io.IOException {
            int startpos = 584;
            String block = getBlock(nfcTag, startpos/16);
            int blockpos = startpos%16;
            String timehex = block.substring(blockpos+2,blockpos+4)+block.substring(blockpos,blockpos+2);
            int sTime = Integer.parseInt(timehex, 16);
//            Log.d(TAG,"Read timehex as "+block.substring(blockpos,blockpos+4)+", time is "+sTime);
            return sTime;
        }

        private int getReadPosition(NfcV nfcTag, boolean dense) throws java.io.IOException {
            String readPosString = getBlock(nfcTag, 0);

            if(dense)
                return (Integer.parseInt(readPosString.substring(4, 6), 16)-1+16)%16;
            else
                return (Integer.parseInt(readPosString.substring(6, 8), 16)-1+32)%32;
        }
        private Float processGlucose(int rawVal) {
            Float processedGlucose = ((rawVal & 0x0FFF) / 6f) - 37f;
//             processedGlucose = ((processedGlucose*1.088f)-9.2f)/18;
            // second set of corrections
            processedGlucose = (processedGlucose*0.6141f)+0.8847f;
            return processedGlucose;
        }
        private int[] getValues(NfcV nfcTag, int valueNo, boolean dense) throws java.io.IOException {
            int offset = dense ? 8 : 200;
            int blockNo  = (Math.round(((valueNo*12)+offset)/16));
            int blockPosition = (Math.round(((valueNo*12)+offset)%16));

            String block = getBlock(nfcTag, blockNo);

            if(blockPosition+10 > 11) {
                block += getBlock(nfcTag, (blockNo + 1));
            }

            int rawGlucose = Integer.parseInt(block.substring(blockPosition+2,blockPosition+4)+block.substring(blockPosition,blockPosition+2),16);
            int rawTemp = Integer.parseInt(block.substring(blockPosition+8,blockPosition+10)+block.substring(blockPosition+6,blockPosition+8),16);
            return new int[]{rawGlucose,rawTemp};
        }
        private String connectToTagAndReadData(Tag... params) {
            Tag tag = params[0];
            NfcV nfcvTag = NfcV.get(tag);

            addLog("Enter NdefReaderTask: " + nfcvTag.toString());
            addLog("Tag ID: " + tag.getId());
            String sensorID = getSensorID(bytesToHex(tag.getId()));
            addLog("Sensor ID: " + sensorID);
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
                int tStart = timeSinceStart(nfcvTag)*60;
                long curTime = System.currentTimeMillis()/1000;
                int newreadpos = getReadPosition(nfcvTag, true);
                int oldreadpos = getReadPosition(nfcvTag, false);
                byte [] cmd = new byte[]{
                        (byte) 0x20, // Flags
                        (byte) 0x20, // Command: Read multiple blocks
                        0,0,0,0,0,0,0,0,
                        (byte) (3) // block (offset)
                };
                System.arraycopy(nfcvTag.getTag().getId(),0,cmd,2,8);
                readData = bytesToHex(Arrays.copyOfRange(nfcvTag.transceive(cmd),1,9));
                addLog("readData: " + readData);

                List<int[]> denseVals = new ArrayList<int[]>();
                for(int i=newreadpos+16;i>newreadpos;i--) {
                    int[] tmpVals = getValues(nfcvTag, i%16, true);
                    denseVals.add(tmpVals);
                }
                addLog("Read "+denseVals.size()+" new dense values.");

                List<int[]> sparseVals = new ArrayList<int[]>();
                for(int i=oldreadpos+32;i>oldreadpos;i--) {
                    int[] tmpVals = getValues(nfcvTag, i%32, false);
//                    if(prefsSet && this.lastSparseGVal == tmpVals[0] && this.lastSparseTVal == tmpVals[1]) {
//                        Log.(TAG, "Cutting sparse reading short with "+sparseVals.size()+" values.");
//                        break;
//                    }
                    // if(lastSparseRecord != null) {
                    //     Log.d(TAG, "Comparing " + lastSparseRecord[0] + " and " + tmpVals[0] + ", also " + lastSparseRecord[1] + " and " + tmpVals[1] + ".");
                    //     if (Integer.parseInt(lastSparseRecord[0]) == tmpVals[0] && Integer.parseInt(lastSparseRecord[1]) == tmpVals[1])
                    //         break;
                    // }
                    sparseVals.add(tmpVals);
                }
                ArrayList<Float> DenseGlucodeVal = new ArrayList<>();
                for(int i=0;i<denseVals.size();i++) {
                    DenseGlucodeVal.add(processGlucose(denseVals.get(i)[0]));
                }
                addLog("Dense Glucose Values " + DenseGlucodeVal);

                ArrayList<Float> SparseGlucodeVal = new ArrayList<>();
                for(int i=0;i<sparseVals.size();i++) {
                    SparseGlucodeVal.add(processGlucose(sparseVals.get(i)[1]));
                }
                addLog("Sparse Glucose Values " + SparseGlucodeVal);
            } catch (IOException e) {
                addLog("Unable to transceive");
            } finally {
                try {
                    nfcvTag.close();
                } catch (IOException e) {
                    addLog("Unable to close techonology");
                    return null;
                }
            }
            addLog("Finish Reading");
            return "done..";
        }
        @Override
        protected String doInBackground(Tag... params) {
            return connectToTagAndReadData(params);
        }

    }
}
