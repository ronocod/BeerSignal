package com.starstorm.beer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.model.GraphUser;
import com.parse.FunctionCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.starstorm.beer.R;
import com.starstorm.beer.service.FriendService;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Conor on 17/09/2014.
 */
public class FacebookFriendAdapter extends ParseQueryAdapter<ParseUser> {

    private static final String TAG = FacebookFriendAdapter.class.getSimpleName();

    public FacebookFriendAdapter(final Context context, final List<GraphUser> friends) {
        super(context, new QueryFactory<ParseUser>() {
            public ParseQuery<ParseUser> create() {

                List<Long> friendsList = new ArrayList<>();
                for (GraphUser user : friends) {
                    friendsList.add(Long.valueOf(user.getId()));
                }

                // Construct a ParseUser query that will find friends whose
                // facebook IDs are contained in the current user's friend list.
                ParseQuery<ParseUser> friendQuery = ParseUser.getQuery();
                friendQuery.whereContainedIn("facebookId", friendsList);
//
//                // Here we can configure a ParseQuery to our heart's desire.
//                ParseQuery<ParseObject> friendshipFromQuery = new ParseQuery<>("Friendship")
//                        .whereEqualTo("from", ParseUser.getCurrentUser())
//                        .whereDoesNotMatchQuery("to", friendQuery);
//
//                ParseQuery<ParseObject> friendshipToQuery = new ParseQuery<>("Friendship")
//                        .whereEqualTo("to", ParseUser.getCurrentUser())
//                        .whereNotEqualTo("status", "rejected");
//
//                ParseQuery<ParseObject> friendshipQuery = ParseQuery.or(Arrays.asList(friendshipFromQuery, friendshipToQuery));

                return friendQuery;
            }
        });
    }

    @Override
    public View getItemView(final ParseUser user, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = View.inflate(getContext(), R.layout.listitem_facebook_friend, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        Picasso.with(getContext())
                .load("https://graph.facebook.com/" + user.get("facebookId") + "/picture?type=square")
                .into(holder.facebookFriendPhoto);

        if (user.getString("status").equals("pending")) {
            holder.statusText.setText("Pending");
            holder.statusText.setVisibility(View.VISIBLE);
            if (user.getParseUser("to").hasSameId(ParseUser.getCurrentUser())) {
                holder.addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendFriendRequest(user.getUsername(), holder);
                    }
                });
                holder.addButton.setVisibility(View.VISIBLE);
            }
        } else {
            holder.addButton.setOnClickListener(null);
            holder.addButton.setVisibility(View.GONE);
            holder.statusText.setVisibility(View.GONE);
        }

        ParseUser friend = user.getParseUser("from");
        if (friend.hasSameId(ParseUser.getCurrentUser())) {
            friend = user.getParseUser("to");
        }
        holder.usernameText.setText(friend.getUsername());

        return view;
    }

    private void sendFriendRequest(String username, final ViewHolder holder) {
        FriendService.sendFriendRequest(username, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
                    Toast.makeText(getContext(), "Request accepted", Toast.LENGTH_SHORT).show();
                    loadObjects();
                    holder.statusText.setText("");
                    holder.addButton.setVisibility(View.GONE);
                } else {
                    Crashlytics.logException(e);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected static class ViewHolder {
        @InjectView(R.id.facebook_friend_photo)
        ImageView facebookFriendPhoto;
        @InjectView(R.id.add_friend_button)
        Button addButton;
        @InjectView(R.id.friend_username_text)
        TextView usernameText;
        @InjectView(R.id.friendship_status_text)
        TextView statusText;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
