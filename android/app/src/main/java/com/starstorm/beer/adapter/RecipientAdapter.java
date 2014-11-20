package com.starstorm.beer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.starstorm.beer.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Conor on 17/09/2014.
 */
public class RecipientAdapter extends ParseQueryAdapter<ParseObject> {

    private static final String TAG = RecipientAdapter.class.getSimpleName();

    private HashMap<String, ParseUser> recipients;

    public RecipientAdapter(Context context) {
        super(context, new QueryFactory<ParseObject>() {
            public ParseQuery<ParseObject> create() {

                // Here we can configure a ParseQuery to our heart's desire.
                ParseQuery<ParseObject> friendshipFromQuery = new ParseQuery<>("Friendship")
                        .whereEqualTo("from", ParseUser.getCurrentUser())
                        .whereEqualTo("status", "accepted");

                ParseQuery<ParseObject> friendshipToQuery = new ParseQuery<>("Friendship")
                        .whereEqualTo("to", ParseUser.getCurrentUser())
                        .whereEqualTo("status", "accepted");

                ParseQuery<ParseObject> friendshipQuery = ParseQuery.or(Arrays.asList(friendshipFromQuery, friendshipToQuery));

                friendshipQuery.include("from");
                friendshipQuery.include("to");
                return friendshipQuery;
            }
        });
        recipients = new HashMap<>();
        addOnQueryLoadListener(new OnQueryLoadListener<ParseObject>() {
            @Override
            public void onLoading() {
                recipients = new HashMap<>();
            }

            @Override
            public void onLoaded(List<ParseObject> parseObjects, Exception e) {

            }
        });
    }

    public HashMap<String, ParseUser> getRecipients() {
        return recipients;
    }

    @Override
    public View getItemView(final ParseObject object, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = View.inflate(getContext(), R.layout.listitem_recipient, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        final ParseUser friend = getFriendFromFriendship(object);
        holder.selectedCheckbox.setChecked(recipients.containsKey(friend.getObjectId()));
        holder.selectedCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    recipients.put(friend.getObjectId(), friend);
                } else {
                    recipients.remove(friend.getObjectId());
                }
            }
        });

        holder.usernameText.setText(friend.getUsername());

        return view;
    }

    private ParseUser getFriendFromFriendship(ParseObject object) {
        ParseUser friend = object.getParseUser("from");
        if (friend.hasSameId(ParseUser.getCurrentUser())) {
            return object.getParseUser("to");
        }
        return friend;
    }

    public String getFriendName(int index) {
        ParseObject item = getItem(index);
        ParseUser friend = getFriendFromFriendship(item);
        return friend.getUsername();
    }

    protected static class ViewHolder {
        @InjectView(R.id.friend_selected_checkbox)
        CheckBox selectedCheckbox;
        @InjectView(R.id.friend_username_text)
        TextView usernameText;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
