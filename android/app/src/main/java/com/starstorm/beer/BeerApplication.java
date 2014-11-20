package com.starstorm.beer;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.util.Base64;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;

import java.security.MessageDigest;
import java.util.Locale;

/**
 * Created by Conor on 06/09/2014.
 */
public class BeerApplication extends Application {

    private static final String TAG = BeerApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        // uncomment to get the keyhash for Facebook
//        printHashKey(getApplicationContext(), "en");

        if (!BuildConfig.DEBUG) {
            Crashlytics.start(this);
        }

        try {
            final String applicationId = ""; // Your Parse application ID
            final String clientKey = ""; // Your Parse client key
            Parse.initialize(this, applicationId, clientKey);
            ParseInstallation.getCurrentInstallation().saveInBackground();
            if (BuildConfig.DEBUG) {
                Parse.setLogLevel(Parse.LOG_LEVEL_INFO);
            }
            ParseFacebookUtils.initialize("YOUR_FACEBOOK_APP_ID");
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        Log.d(TAG, "Application onCreate() finished");
    }

    public static String printHashKey(Context context, String code) {
        Log.d(TAG, "keyHash: start");
        Locale locale = new Locale(code);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo("com.starstorm.beer", PackageManager.GET_SIGNATURES);
            Signature[] signatures = info.signatures;

            if (signatures == null || signatures.length == 0) {
                return "error";
            }

            Signature signature = signatures[0];
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(signature.toByteArray());
            String keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
            Log.i(TAG, "keyHash: " + keyHash);
            return keyHash;
        } catch (Exception e) {
            Log.e(TAG, "keyHash: error:" + e);
        }

        return "error";
    }

}
