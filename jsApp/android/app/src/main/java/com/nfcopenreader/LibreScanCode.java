import com.nfcopenreader;

import android.content.Context;
import android.media.AudioManager;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.nfcopenreader.R;
import io.realm.Realm;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.media.AudioManager.RINGER_MODE_SILENT;

public void LibreScanCode extends AsyncTask<Tag, Void, Boolean> {
    private static final String LOG_ID="OpenLibre::"+LibreScanCode.class.getSimpleName();
    private MainActivity mainActivity;
    private String sensorTagId;
    private byte[]data;

//    public LibreScanCode(MainActivity mainActivity){
//            this.mainActivity=mainActivity;
//            data=new byte[360];
//            }
//        private void updateProgressBar(int blockIndex) {
//            final int progress = blockIndex;
//            mainActivity.runOnUiThread(new Runnable() {
//                public void run() { ((ProgressBar) mainActivity.findViewById(R.id.pb_scan_circle)).setProgress(progress);
//                    }
//                });
//        }
        private boolean readNfcTag (Tag tag) {
            updateProgressBar(0);
            NfcV nfcvTag = NfcV.get(tag);
            Log.d(LibreScanCode.LOG_ID, "Attempting to read tag data");
            try {
                nfcvTag.connect();
                final byte[] uid = tag.getId();
                boolean NFC_USE_MULTI_BLOCK_READ = settings.getBoolean("pref_nfc_use_multi_block_read", NFC_USE_MULTI_BLOCK_READ);
                final int step = NFC_USE_MULTI_BLOCK_READ ? 3 : 1;
                final int blockSize = 8;

                for (int blockIndex = 0; blockIndex <= 40; blockIndex += step) {
                    byte[] cmd;
                    if (OpenLibre.NFC_USE_MULTI_BLOCK_READ) {
                        cmd = new byte[]{0x02, 0x23, (byte) blockIndex, 0x02}; // multi-block read 3 blocks
                    }
                    else {
                        cmd = new byte[]{0x60, 0x20, 0, 0, 0, 0, 0, 0, 0, 0, (byte) blockIndex, 0};
                        System.arraycopy(uid, 0, cmd, 2, 8);
                    }

                    byte[] readData;
                    Long startReadingTime = System.currentTimeMillis();
                    while (true) {
                        try {
                            readData = nfcvTag.transceive(cmd);
                            break;
                        } catch (IOException e) {
                            if ((System.currentTimeMillis() > startReadingTime + nfcReadTimeout)) {
                                Log.e(NfcVReaderTask.LOG_ID, "tag read timeout");
                                return false;
                            }
                        }
                    }

                    if (OpenLibre.NFC_USE_MULTI_BLOCK_READ) {
                        System.arraycopy(readData, 1, data, blockIndex * blockSize, readData.length - 1);
                    }
                    else {
                        readData = Arrays.copyOfRange(readData, 2, readData.length);
                        System.arraycopy(readData, 0, data, blockIndex * blockSize, blockSize);
                    }

                    updateProgressBar(blockIndex);
                }
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
    }

