package com.starstorm.beer.fragment;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.FunctionCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.starstorm.beer.R;
import com.starstorm.beer.adapter.FacebookFriendAdapter;
import com.starstorm.beer.service.FriendService;
import com.starstorm.beer.util.Toaster;

import java.util.Arrays;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A simple {@link android.app.Fragment} subclass.
 * Use the {@link com.starstorm.beer.fragment.FacebookFriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class FacebookFriendsFragment extends BaseFragment {

    static final String TAG = FacebookFriendsFragment.class.getSimpleName();

    @InjectView(R.id.swipe_container)
    SwipeRefreshLayout mSwipeLayout;
    @InjectView(R.id.friend_listview)
    ListView mListView;
    @InjectView(R.id.add_friend_text)
    TextView mAddFriendNameText;
    @InjectView(R.id.add_friend_button)
    ImageButton mAddFriendButton;
    private FacebookFriendAdapter facebookFriendAdapter;

    public static FacebookFriendsFragment newInstance() {
        return new FacebookFriendsFragment();
    }

    public FacebookFriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Request.GraphUserListCallback callback = new Request.GraphUserListCallback() {
            @Override
            public void onCompleted(List<GraphUser> users, Response response) {

                facebookFriendAdapter = new FacebookFriendAdapter(getActivity(), users);

                facebookFriendAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseUser>() {
                    @Override
                    public void onLoading() {
                        if (getActivity() != null && mSwipeLayout != null) {
                            mSwipeLayout.setRefreshing(true);
                        }
                    }

                    @Override
                    public void onLoaded(List<ParseUser> parseUsers, Exception e) {
                        if (getActivity() != null && mSwipeLayout != null) {
                            mSwipeLayout.setRefreshing(false);
                        }
                    }
                });
            }
        };

        final List<String> permissions = ParseFacebookUtils.getSession().getPermissions();
        if (permissions.contains("user_friends")) {
            // we already have the permission
            performRequest(callback);
        } else {
            // get the permission and then perform the request
            ParseFacebookUtils.linkInBackground(ParseUser.getCurrentUser(), Arrays.asList("user_friends"), getActivity())
                .onSuccess(new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> voidTask) throws Exception {
                        performRequest(callback);
                        return null;
                    }
                });
        }
    }

    private void performRequest(Request.GraphUserListCallback callback) {
        Request.newMyFriendsRequest(ParseFacebookUtils.getSession(), callback).executeAsync();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.inject(this, view);

        mListView.setAdapter(facebookFriendAdapter);

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                facebookFriendAdapter.loadObjects();
            }
        });

        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mAddFriendNameText.getText().toString().toLowerCase();
                sendFriendRequest(username);
            }
        });
    }

    private void sendFriendRequest(final String username) {
        setMenuWhirrerVisible(true);
        FriendService.INSTANCE.sendFriendRequest(username, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                setMenuWhirrerVisible(false);
                if (e == null) {
                    Toaster.showShort(getActivity(), "sendfriendrequest success");
                    facebookFriendAdapter.loadObjects();
                    mAddFriendNameText.setText("");
                } else {
                    Crashlytics.logException(e);
                    Toaster.showShort(getActivity(), "Error: " + e.getMessage());
                }
            }
        });
    }
}
