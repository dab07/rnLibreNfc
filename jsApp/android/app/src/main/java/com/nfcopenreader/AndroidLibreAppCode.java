package com.nfcopenreader;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
//import java.io.StringWriter;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.PendingIntent;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.IntentFilter.MalformedMimeTypeException;
//import android.graphics.Color;
//import android.media.MediaPlayer;
//import android.media.MediaPlayer.OnCompletionListener;
//import android.nfc.NfcAdapter;
//import android.nfc.Tag;
//import android.nfc.tech.NfcA;
//import android.nfc.tech.NfcB;
//import android.nfc.tech.NfcBarcode;
//import android.nfc.tech.NfcF;
//import android.nfc.tech.NfcV;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.CountDownTimer;
//import android.os.Vibrator;
//import android.util.Log;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.util.Log;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import java.util.Map;
import java.util.HashMap;

public class AndroidLibreAppCode extends ReactContextBaseJavaModule {
    AndroidLibreAppCode(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "AndroidLibreAppCode";
    }
    @ReactMethod
    public void ScanLibre() {
        AndroidLibreAppCode.readNfcTag();
//        Log.d("tag", "msg");
    }
}



