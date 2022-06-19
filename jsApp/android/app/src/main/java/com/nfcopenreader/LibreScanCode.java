package com.nfcopenreader;

import android.content.Context;
//import android.media.AudioManager;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
//import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
//import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import io.realm.Realm;
//
//import static android.content.Context.VIBRATOR_SERVICE;
//import static android.media.AudioManager.RINGER_MODE_SILENT;

public class LibreScanCode extends AsyncTask<Tag, Void, Boolean> {
    private static final String LOG_ID="[LibreScanCode] LOGID::"+LibreScanCode.class.getSimpleName();
    private MainActivity mainActivity;
    private String sensorTagId;
    private byte[] readData;
    private Object blockIndex;
    public LibreScanCode(MainActivity mainActivity){
            this.mainActivity=mainActivity;
            readData=new byte[360];
            }
//        private void updateProgressBar(int blockIndex) {
//            final int progress = blockIndex;
//            mainActivity.runOnUiThread(new Runnable() {
////               public void run() { ((ProgressBar) mainActivity.findViewById(R.id.pb_scan_circle)).setProgress(progress);
//                public void run() {
//                }
//            });
//        }
        public boolean readNfcTag (Tag tag) {
//            updateProgressBar(0);
            NfcV nfcvTag = NfcV.get(tag);
            Log.d(LibreScanCode.LOG_ID, "Attempting to read tag data");
            try {
                nfcvTag.connect();
                final byte[] uid = tag.getId();
                final int step = true? 3 : 1; //if not work than set step = 1.
                final int blockSize = 8;

                for (int blockIndex = 0; blockIndex <= 40; blockIndex += step) {
                    byte[] cmd;
                    if (step == 3) {
                        cmd = new byte[]{0x02, 0x23, (byte) blockIndex, 0x02}; // multi-block read 3 blocks
                    }
                    else {
                        cmd = new byte[]{0x60, 0x20, 0, 0, 0, 0, 0, 0, 0, 0, (byte) blockIndex, 0};
                        System.arraycopy(uid, 0, cmd, 2, 8);
                    }

                    byte[] readData;
//                    Long startReadingTime = System.currentTimeMillis();
                        try {
                            readData = nfcvTag.transceive(cmd);
                            break;
                        } catch (IOException e) {
//                            if ((System.currentTimeMillis() > startReadingTime + nfcReadTimeout)) {
                                Log.e(LibreScanCode.LOG_ID, "una");
                                break;
                            }
                        }

//                if (step == 3) {
//                        System.arraycopy(readData, 1, data, blockIndex * blockSize, readData.length - 1);
//                    }
//                    else {
//                        readData = Arrays.copyOfRange(readData, 2, readData.length);
//                        System.arraycopy(readData, 0, data, blockIndex * blockSize, blockSize);
//                    }

//                    updateProgressBar((Integer) blockIndex);
                Log.d(LibreScanCode.LOG_ID, "Got NFC tag data");

        } catch (Exception e) {
            Log.i(LibreScanCode.LOG_ID, e.toString());
            return false;
        } finally {
            try {
                nfcvTag.close();
            } catch (Exception e) {
                Log.e(LibreScanCode.LOG_ID, "Error closing tag!");
            }
        }
        Log.d(LibreScanCode.LOG_ID, "Tag data reader exiting");
        return true;
        }

    @Override
    protected Boolean doInBackground(Tag... tags) {
        return true;
    }
}

