package com.starstorm.beer.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
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
import com.novoda.notils.caster.Views;
import com.parse.FunctionCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.starstorm.beer.R;
import com.starstorm.beer.adapter.FacebookFriendAdapter;
import com.starstorm.beer.service.ParseFriendService;
import com.starstorm.beer.util.Toaster;

import java.util.Arrays;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * A simple {@link android.app.Fragment} subclass.
 * Use the {@link com.starstorm.beer.fragment.FacebookFriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FacebookFriendsFragment extends Fragment {

    private final ParseFriendService friendService = ParseFriendService.INSTANCE;

    private SwipeRefreshLayout swipeLayout;
    private TextView addFriendNameText;
    private FacebookFriendAdapter facebookFriendAdapter;
    private ProgressDialog progressDialog;

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
                        if (getActivity() != null && swipeLayout != null) {
                            swipeLayout.setRefreshing(true);
                        }
                    }

                    @Override
                    public void onLoaded(List<ParseUser> parseUsers, Exception e) {
                        if (getActivity() != null && swipeLayout != null) {
                            swipeLayout.setRefreshing(false);
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

        swipeLayout = Views.findById(view, R.id.swipe_container);
        addFriendNameText = Views.findById(view, R.id.add_friend_text);

        ListView listView = Views.findById(view, R.id.friend_listview);
        listView.setAdapter(facebookFriendAdapter);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                facebookFriendAdapter.loadObjects();
            }
        });

        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        ImageButton addFriendButton = Views.findById(view, R.id.add_friend_button);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = addFriendNameText.getText().toString().toLowerCase();
                sendFriendRequest(username);
            }
        });
    }

    private void sendFriendRequest(final String username) {
        setMenuWhirrerVisible(true);
        friendService.sendFriendRequest(username, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                setMenuWhirrerVisible(false);
                if (e == null) {
                    Toaster.showShort(getActivity(), "sendfriendrequest success");
                    facebookFriendAdapter.loadObjects();
                    addFriendNameText.setText("");
                } else {
                    Crashlytics.logException(e);
                    Toaster.showShort(getActivity(), "Error: " + e.getMessage());
                }
            }
        });
    }

    private void setMenuWhirrerVisible(boolean visible) {
        if (visible) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.show();
        } else {
            progressDialog.hide();
        }
    }
}
