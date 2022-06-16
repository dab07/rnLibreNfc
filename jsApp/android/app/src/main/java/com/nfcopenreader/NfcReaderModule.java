package com.nfcopenreader; // replace com.your-app-name with your app’s name
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import java.util.Map;
import java.util.HashMap;
import android.util.Log;

public class NfcReaderModule extends ReactContextBaseJavaModule {
    private String className = "NfcReaderModule";

   NfcReaderModule(ReactApplicationContext context) {
       super(context);
   }
   @Override
   public String getName() {
      return "NfcReaderModule";
   }
//    @ReactMethod
//    public void TestingNativeModule(String name, String ) {
//    }

   @ReactMethod
   public void TestingNativeModule(String name, String location) {
//       Log.d(className , String.format("name: %s, location: %s", name, location));
      Log.d(className , "name: " + name + "location" + location);
   }
}
