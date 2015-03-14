package com.starstorm.beer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.novoda.notils.caster.Views;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.starstorm.beer.R;
import com.starstorm.beer.util.ParseHelper;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Conor on 17/09/2014.
 */
public class FriendAdapter extends ParseQueryAdapter<ParseObject> {

    private static final String TAG = FriendAdapter.class.getSimpleName();

    public FriendAdapter(Context context) {
        super(context, new QueryFactory<ParseObject>() {
            public ParseQuery<ParseObject> create() {

                // Here we can configure a ParseQuery to our heart's desire.
                ParseQuery<ParseObject> friendshipFromQuery = new ParseQuery<>("Friendship")
                        .whereEqualTo("from", ParseUser.getCurrentUser())
                        .whereNotEqualTo("status", "rejected");

                ParseQuery<ParseObject> friendshipToQuery = new ParseQuery<>("Friendship")
                        .whereEqualTo("to", ParseUser.getCurrentUser())
                        .whereNotEqualTo("status", "rejected");

                ParseQuery<ParseObject> friendshipQuery = ParseQuery.or(Arrays.asList(friendshipFromQuery, friendshipToQuery));

                friendshipQuery.include("from");
                friendshipQuery.include("to");
                return friendshipQuery;
            }
        });
    }

    @Override
    public View getItemView(final ParseObject object, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = View.inflate(getContext(), R.layout.listitem_friend, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        if (object.getString("status").equals("pending")) {
            holder.statusText.setText("Pending");
            holder.statusText.setVisibility(View.VISIBLE);
            if (object.getParseUser("to").hasSameId(ParseUser.getCurrentUser())) {
                holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        acceptFriendRequest(object.getObjectId(), holder);
                    }
                });
                holder.acceptButton.setVisibility(View.VISIBLE);
            }
        } else {
            holder.acceptButton.setOnClickListener(null);
            holder.acceptButton.setVisibility(View.GONE);
            holder.statusText.setVisibility(View.GONE);
        }

        ParseUser friend = object.getParseUser("from");
        if (friend.hasSameId(ParseUser.getCurrentUser())) {
            friend = object.getParseUser("to");
        }
        holder.usernameText.setText(friend.getUsername());

        return view;
    }

    public String getFriendName(int index) {
        ParseObject item = getItem(index);
        ParseUser friend = item.getParseUser("from");
        if (friend.hasSameId(ParseUser.getCurrentUser())) {
            friend = item.getParseUser("to");
        }
        return friend.getUsername();
    }

    private void acceptFriendRequest(String friendshipId, final ViewHolder holder) {
        HashMap<String, Object> params = ParseHelper.getDefaultParams();
        params.put("friendshipId", friendshipId);
        ParseCloud.callFunctionInBackground("acceptfriendrequest", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
                    Toast.makeText(getContext(), "Request accepted", Toast.LENGTH_SHORT).show();
                    loadObjects();
                    holder.statusText.setText("");
                    holder.acceptButton.setVisibility(View.GONE);
                } else {
                    Crashlytics.logException(e);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    static class ViewHolder {
        final Button acceptButton;
        final TextView usernameText;
        final TextView statusText;

        public ViewHolder(View view) {
            acceptButton = Views.findById(view, R.id.accept_friend_button);
            usernameText = Views.findById(view, R.id.friend_username_text);
            statusText = Views.findById(view, R.id.friendship_status_text);
        }
    }
}
