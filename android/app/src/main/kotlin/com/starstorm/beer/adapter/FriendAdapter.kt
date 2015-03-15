package com.starstorm.beer.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.crashlytics.android.Crashlytics
import com.novoda.notils.caster.Views
import com.parse.FunctionCallback
import com.parse.ParseCloud
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseQueryAdapter
import com.parse.ParseUser
import com.starstorm.beer.R
import com.starstorm.beer.util.*

import java.util.Arrays
import java.util.HashMap

/**
 * Created by Conor on 17/09/2014.
 */
public class FriendAdapter(context: Context)// Here we can configure a ParseQuery to our heart's desire.
: ParseQueryAdapter<ParseObject>(context, object : ParseQueryAdapter.QueryFactory<ParseObject> {
    override fun create(): ParseQuery<ParseObject> {
        val friendshipFromQuery = ParseQuery<ParseObject>("Friendship").whereEqualTo("from", ParseUser.getCurrentUser()).whereNotEqualTo("status", "rejected")

        val friendshipToQuery = ParseQuery<ParseObject>("Friendship").whereEqualTo("to", ParseUser.getCurrentUser()).whereNotEqualTo("status", "rejected")

        val friendshipQuery = ParseQuery.or<ParseObject>(Arrays.asList<ParseQuery<ParseObject>>(friendshipFromQuery, friendshipToQuery))

        friendshipQuery.include("from")
        friendshipQuery.include("to")
        return friendshipQuery
    }
}) {

    override fun getItemView(item: ParseObject, recycledView: View?, parent: ViewGroup): View {
        var view : View
        val holder: ViewHolder
        if (recycledView != null) {
            view = recycledView
            holder = view.getTag() as ViewHolder
        } else {
            view = View.inflate(getContext(), R.layout.listitem_friend, null)
            holder = ViewHolder(view)
            view.setTag(holder)
        }

        if (item.getString("status") == "pending") {
            holder.statusText.setText("Pending")
            holder.statusText.setVisibility(View.VISIBLE)
            if (item.getParseUser("to").hasSameId(ParseUser.getCurrentUser())) {
                holder.acceptButton.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        acceptFriendRequest(item.getObjectId(), holder)
                    }
                })
                holder.acceptButton.setVisibility(View.VISIBLE)
            }
        } else {
            holder.acceptButton.setOnClickListener(null)
            holder.acceptButton.setVisibility(View.GONE)
            holder.statusText.setVisibility(View.GONE)
        }

        var friend = item.getParseUser("from")
        if (friend.hasSameId(ParseUser.getCurrentUser())) {
            friend = item.getParseUser("to")
        }
        holder.usernameText.setText(friend.getUsername())

        return view
    }

    public fun getFriendName(index: Int): String {
        val item = getItem(index)
        var friend = item.getParseUser("from")
        if (friend.hasSameId(ParseUser.getCurrentUser())) {
            friend = item.getParseUser("to")
        }
        return friend.getUsername()
    }

    private fun acceptFriendRequest(friendshipId: String, holder: ViewHolder) {
        val params = getDefaultParams()
        params.put("friendshipId", friendshipId)
        ParseCloud.callFunctionInBackground<Any>("acceptfriendrequest", params, object : FunctionCallback<Any> {
            override fun done(o: Any, e: ParseException?) {
                if (e == null) {
                    Toast.makeText(getContext(), "Request accepted", Toast.LENGTH_SHORT).show()
                    loadObjects()
                    holder.statusText.setText("")
                    holder.acceptButton.setVisibility(View.GONE)
                } else {
                    Crashlytics.logException(e)
                    Toast.makeText(getContext(), "Error: ${e.getMessage()}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    class ViewHolder(view: View) {
        val acceptButton: Button
        val usernameText: TextView
        val statusText: TextView

        {
            acceptButton = Views.findById<Button>(view, R.id.accept_friend_button)
            usernameText = Views.findById<TextView>(view, R.id.friend_username_text)
            statusText = Views.findById<TextView>(view, R.id.friendship_status_text)
        }
    }

    class object {

        private val TAG = javaClass<FriendAdapter>().getSimpleName()
    }
}
