package com.starstorm.beer

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Base64
import android.util.Log

import com.crashlytics.android.Crashlytics
import com.parse.Parse
import com.parse.ParseFacebookUtils
import com.parse.ParseInstallation

import java.security.MessageDigest
import java.util.Locale

/**
 * Created by Conor on 06/09/2014.
 */
public class BeerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // uncomment to get the keyhash for Facebook
        //        printHashKey(getApplicationContext(), "en");

        if (!BuildConfig.DEBUG) {
            Crashlytics.start(this)
        }

        try {
            val applicationId = "" // Your Parse application ID
            val clientKey = "" // Your Parse client key
            Parse.initialize(this, applicationId, clientKey)
            ParseInstallation.getCurrentInstallation().saveInBackground()
            if (BuildConfig.DEBUG) {
                Parse.setLogLevel(Parse.LOG_LEVEL_INFO)
            }
            ParseFacebookUtils.initialize("YOUR_FACEBOOK_APP_ID")
        } catch (e: Exception) {
            Crashlytics.logException(e)
        }

    }

    class object {

        private val TAG = javaClass<BeerApplication>().getSimpleName()

        public fun printHashKey(context: Context, code: String): String {
            val locale = Locale(code)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics())

            try {
                val info = context.getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_SIGNATURES)
                val signatures = info.signatures

                if (signatures == null || signatures.size == 0) {
                    return "error"
                }

                val signature = signatures[0]
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                Log.i(TAG, "keyHash: " + keyHash)
                return keyHash
            } catch (e: Exception) {
                Log.e(TAG, "keyHash: error:" + e)
            }

            return "error"
        }
    }

}
