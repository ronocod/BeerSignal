package com.starstorm.beer.util

import com.crashlytics.android.Crashlytics
import com.facebook.Request
import com.facebook.Response
import com.facebook.model.GraphUser
import com.parse.ParseException
import com.parse.ParseFacebookUtils
import com.parse.ParseUser
import com.parse.SaveCallback

/**
 * Created by Conor on 05/11/2014.
 */
public class FacebookHelper {
    class object {

        public val TAG: String = javaClass<FacebookHelper>().getSimpleName()

        public fun getFacebookIdInBackground() {
            Request.newMeRequest(ParseFacebookUtils.getSession(), object : Request.GraphUserCallback {
                override fun onCompleted(user: GraphUser?, response: Response) {
                    if (user != null) {
                        ParseUser.getCurrentUser().put("facebookId", java.lang.Long.valueOf(user!!.getId()))
                        ParseUser.getCurrentUser().saveInBackground(object : SaveCallback {
                            override fun done(e: ParseException?) {
                                if (e != null) {
                                    Crashlytics.logException(e)
                                }
                            }
                        })
                    }
                }
            }).executeAsync()
        }
    }
}
