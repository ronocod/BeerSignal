package com.starstorm.beer.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView

import com.novoda.notils.caster.Views
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseQueryAdapter
import com.parse.ParseUser
import com.starstorm.beer.R

import java.util.Arrays
import java.util.HashMap

/**
 * Created by Conor on 17/09/2014.
 */
public class RecipientAdapter(context: Context) : ParseQueryAdapter<ParseObject>(context, object : ParseQueryAdapter.QueryFactory<ParseObject> {
    override fun create(): ParseQuery<ParseObject> {

        // Here we can configure a ParseQuery to our heart's desire.
        val friendshipFromQuery = ParseQuery<ParseObject>("Friendship")
                .whereEqualTo("from", ParseUser.getCurrentUser())
                .whereEqualTo("status", "accepted")

        val friendshipToQuery = ParseQuery<ParseObject>("Friendship")
                .whereEqualTo("to", ParseUser.getCurrentUser())
                .whereEqualTo("status", "accepted")

        val friendshipQuery = ParseQuery.or<ParseObject>(Arrays.asList<ParseQuery<ParseObject>>(friendshipFromQuery, friendshipToQuery))

        friendshipQuery.include("from")
        friendshipQuery.include("to")
        return friendshipQuery
    }
}) {

    public var recipients: HashMap<String, ParseUser> = HashMap()
        private set

    {
        addOnQueryLoadListener(object : ParseQueryAdapter.OnQueryLoadListener<ParseObject> {
            override fun onLoading() {
                recipients = HashMap<String, ParseUser>()
            }

            override fun onLoaded(parseObjects: List<ParseObject>, e: Exception) {
            }
        })
    }

    override fun getItemView(item: ParseObject, recycledView: View?, parent: ViewGroup): View {
        var view: View
        val holder: ViewHolder
        if (recycledView != null) {
            view = recycledView
            holder = view.getTag() as ViewHolder
        } else {
            view = View.inflate(getContext(), R.layout.listitem_recipient, null)
            holder = ViewHolder(view)
            view.setTag(holder)
        }

        val friend = getFriendFromFriendship(item)
        holder.selectedCheckbox.setChecked(recipients.containsKey(friend.getObjectId()))
        holder.selectedCheckbox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(compoundButton: CompoundButton, checked: Boolean) {
                if (checked) {
                    recipients.put(friend.getObjectId(), friend)
                } else {
                    recipients.remove(friend.getObjectId())
                }
            }
        })

        holder.usernameText.setText(friend.getUsername())

        return view
    }

    private fun getFriendFromFriendship(friendship: ParseObject): ParseUser {
        val friend = friendship.getParseUser("from")
        if (friend.hasSameId(ParseUser.getCurrentUser())) {
            return friendship.getParseUser("to")
        }
        return friend
    }

    public fun getFriendName(index: Int): String {
        val item = getItem(index)
        val friend = getFriendFromFriendship(item)
        return friend.getUsername()
    }

    class ViewHolder(view: View) {
        val selectedCheckbox: CheckBox
        val usernameText: TextView

        {
            selectedCheckbox = Views.findById<CheckBox>(view, R.id.friend_selected_checkbox)
            usernameText = Views.findById<TextView>(view, R.id.friend_username_text)
        }
    }

    class object {

        private val TAG = javaClass<RecipientAdapter>().getSimpleName()
    }
}
