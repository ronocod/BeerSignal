package com.starstorm.beer.push

import android.content.Context
import android.content.Intent

import com.parse.ParseAnalytics
import com.parse.ParsePushBroadcastReceiver
import com.starstorm.beer.R

/**
 * Created by Conor on 05/10/2014.
 */
public class PushReceiver : ParsePushBroadcastReceiver() {

    override fun getSmallIconId(context: Context, intent: Intent): Int {
        return R.drawable.ic_notification_icon
    }

    override fun onPushOpen(context: Context, intent: Intent) {
        ParseAnalytics.trackAppOpenedInBackground(intent)
        val newIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName())
        newIntent.putExtras(intent.getExtras())
        context.startActivity(newIntent)
    }
}
