package com.starstorm.beer.push;

import android.content.Context;
import android.content.Intent;

import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;
import com.starstorm.beer.R;

/**
 * Created by Conor on 05/10/2014.
 */
public class PushReceiver extends ParsePushBroadcastReceiver {

    public PushReceiver() {
    }

    @Override
    protected int getSmallIconId(Context context, Intent intent) {
        return R.drawable.ic_notification_icon;
    }

    @Override
    public void onPushOpen(Context context, Intent intent) {
        ParseAnalytics.trackAppOpenedInBackground(intent);
        Intent newIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        newIntent.putExtras(intent.getExtras());
        context.startActivity(newIntent);
    }
}
