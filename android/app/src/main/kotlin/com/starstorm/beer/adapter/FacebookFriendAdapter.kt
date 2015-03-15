package com.starstorm.beer.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.crashlytics.android.Crashlytics
import com.facebook.model.GraphUser
import com.novoda.notils.caster.Views
import com.parse.FunctionCallback
import com.parse.ParseException
import com.parse.ParseQuery
import com.parse.ParseQueryAdapter
import com.parse.ParseUser
import com.squareup.picasso.Picasso
import com.starstorm.beer.R
import com.starstorm.beer.service.ParseFriendService

import java.util.ArrayList

public class FacebookFriendAdapter(context: Context, friends: List<GraphUser>) : ParseQueryAdapter<ParseUser>(context, object : ParseQueryAdapter.QueryFactory<ParseUser> {
    override fun create(): ParseQuery<ParseUser> {
        val friendsList = ArrayList<Long>()
        for (user in friends) {
            friendsList.add(java.lang.Long.valueOf(user.getId()))
        }
        val friendQuery = ParseUser.getQuery()
        friendQuery.whereContainedIn("facebookId", friendsList)

        return friendQuery
    }
}) {
    private val friendService = ParseFriendService

    override fun getItemView(user: ParseUser, recycledView: View?, parent: ViewGroup): View {
        var view : View
        val holder: ViewHolder
        if (recycledView != null) {
            view = recycledView
            holder = recycledView.getTag() as ViewHolder
        } else {
            view = View.inflate(getContext(), R.layout.listitem_facebook_friend, null) : View
            holder = ViewHolder(view)
            view.setTag(holder)
        }

        Picasso.with(getContext()).load("https://graph.facebook.com/" + user.get("facebookId") + "/picture?type=square").into(holder.facebookFriendPhoto)

        if (user.getString("status") == "pending") {
            holder.statusText.setText("Pending")
            holder.statusText.setVisibility(View.VISIBLE)
            if (user.getParseUser("to").hasSameId(ParseUser.getCurrentUser())) {
                holder.addButton.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        sendFriendRequest(user.getUsername(), holder)
                    }
                })
                holder.addButton.setVisibility(View.VISIBLE)
            }
        } else {
            holder.addButton.setOnClickListener(null)
            holder.addButton.setVisibility(View.GONE)
            holder.statusText.setVisibility(View.GONE)
        }

        var friend = user.getParseUser("from")
        if (friend.hasSameId(ParseUser.getCurrentUser())) {
            friend = user.getParseUser("to")
        }
        holder.usernameText.setText(friend.getUsername())

        return view
    }

    private fun sendFriendRequest(username: String, holder: ViewHolder) {
        friendService.sendFriendRequest(username, object : FunctionCallback<Any> {
            override fun done(o: Any, e: ParseException?) {
                if (e == null) {
                    Toast.makeText(getContext(), "Request accepted", Toast.LENGTH_SHORT).show()
                    loadObjects()
                    holder.statusText.setText("")
                    holder.addButton.setVisibility(View.GONE)
                } else {
                    Crashlytics.logException(e)
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    class ViewHolder(view: View) {
        val facebookFriendPhoto: ImageView
        val addButton: Button
        val usernameText: TextView
        val statusText: TextView

        {
            facebookFriendPhoto = view.findViewById(R.id.facebook_friend_photo) as ImageView
            addButton = Views.findById<Button>(view, R.id.add_friend_button)
            usernameText = Views.findById<TextView>(view, R.id.friend_username_text)
            statusText = Views.findById<TextView>(view, R.id.friendship_status_text)
        }
    }

    class object {

        private val TAG = javaClass<FacebookFriendAdapter>().getSimpleName()
    }
}
