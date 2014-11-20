package com.starstorm.beer.fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.parse.FunctionCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;
import com.starstorm.beer.R;
import com.starstorm.beer.adapter.FriendAdapter;
import com.starstorm.beer.service.FriendService;
import com.starstorm.beer.util.Toaster;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class FriendsFragment extends BaseFragment {

    static final String TAG = FriendsFragment.class.getSimpleName();

    @InjectView(R.id.swipe_container)
    SwipeRefreshLayout mSwipeLayout;
    @InjectView(R.id.friend_listview)
    ListView mListView;
    @InjectView(R.id.add_friend_text)
    TextView mAddFriendNameText;
    @InjectView(R.id.add_friend_button)
    ImageButton mAddFriendButton;
    private FriendAdapter friendAdapter;

    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        friendAdapter = new FriendAdapter(getActivity());

        friendAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            @Override
            public void onLoading() {
            }

            @Override
            public void onLoaded(List<ParseObject> parseUsers, Exception e) {
                if (getActivity() != null && mSwipeLayout != null) {
                    mSwipeLayout.setRefreshing(false);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.inject(this, view);

        mListView.setAdapter(friendAdapter);

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                friendAdapter.loadObjects();
            }
        });
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {

                String[] items = {"Unfriend", "Cancel"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(friendAdapter.getFriendName(i))
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                switch (which) {
                                    case 0:
                                        sendUnfriendRequest(friendAdapter.getItem(i));
                                        break;
                                    case 1:
                                        dialog.cancel();
                                        break;
                                }
                            }
                        });
                builder.show();
                return false;
            }
        });

        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mAddFriendNameText.getText().toString().toLowerCase();
                sendFriendRequest(username);
            }
        });
    }

    private void sendUnfriendRequest(final ParseObject friendship) {
        setMenuWhirrerVisible(true);
        FriendService.sendUnfriendRequest(friendship, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                setMenuWhirrerVisible(false);
                if (e == null) {
                    Toaster.showShort(getActivity(), "unfriend success");
                    friendAdapter.loadObjects();
                } else {
                    Crashlytics.logException(e);
                    Toaster.showShort(getActivity(), "Error: " + e.getMessage());
                }
            }
        });
    }

    private void sendFriendRequest(final String username) {
        setMenuWhirrerVisible(true);
        FriendService.sendFriendRequest(username, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                setMenuWhirrerVisible(false);
                if (e == null) {
                    Toaster.showShort(getActivity(), "sendfriendrequest success");
                    friendAdapter.loadObjects();
                    mAddFriendNameText.setText("");
                } else {
                    Crashlytics.logException(e);
                    Toaster.showShort(getActivity(), "Error: " + e.getMessage());
                }
            }
        });
    }
}