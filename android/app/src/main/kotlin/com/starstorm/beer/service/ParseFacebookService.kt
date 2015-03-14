package com.starstorm.beer.service

import android.app.Activity

import com.facebook.Request
import com.facebook.Response
import com.facebook.model.GraphUser
import com.parse.FindCallback
import com.parse.ParseFacebookUtils
import com.parse.ParseQuery
import com.parse.ParseUser

import java.util.ArrayList
import java.util.Arrays

import bolts.Continuation
import bolts.Task

/**
 * Created by Conor on 16/10/2014.
 */
public enum class ParseFacebookService {

    INSTANCE

    public fun fetchFacebookFriendUsers(activity: Activity, callback: FindCallback<ParseUser>) {

        if (ParseFacebookUtils.getSession().getPermissions().contains("user_friends")) {
            // we already have the permission
            performRequest(callback)
        } else {
            // get the permission and then perform the request
            ParseFacebookUtils.linkInBackground(ParseUser.getCurrentUser(), Arrays.asList<String>("user_friends"), activity).onSuccess<Void>(object : Continuation<Void, Void> {
                throws(javaClass<Exception>())
                override fun then(voidTask: Task<Void>): Void? {
                    performRequest(callback)
                    return null
                }
            })
        }
    }

    private fun performRequest(callback: FindCallback<ParseUser>) {
        Request.newMyFriendsRequest(ParseFacebookUtils.getSession(), object : Request.GraphUserListCallback {
            override fun onCompleted(users: List<GraphUser>?, response: Response) {
                if (users == null) {
                    return
                }
                val friendsList = ArrayList<Long>()
                for (user in users) {
                    friendsList.add(java.lang.Long.valueOf(user.getId()))
                }

                // Construct a ParseUser query that will find friends whose
                // facebook IDs are contained in the current user's friend list.
                val friendQuery = ParseUser.getQuery()
                friendQuery.whereContainedIn("facebookId", friendsList)

                // findObjects will return a list of ParseUsers that are friends with
                // the current user
                friendQuery.findInBackground(callback)
            }
        }).executeAsync()
    }
}
