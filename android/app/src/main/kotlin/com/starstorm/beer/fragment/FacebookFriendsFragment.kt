package com.starstorm.beer.fragment

import android.app.Fragment
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView

import com.crashlytics.android.Crashlytics
import com.facebook.Request
import com.facebook.Response
import com.facebook.model.GraphUser
import com.novoda.notils.caster.Views
import com.parse.FunctionCallback
import com.parse.ParseException
import com.parse.ParseFacebookUtils
import com.parse.ParseQueryAdapter
import com.parse.ParseUser
import com.starstorm.beer.R
import com.starstorm.beer.adapter.FacebookFriendAdapter
import com.starstorm.beer.service.ParseFriendService
import com.starstorm.beer.util.Toaster

import java.util.Arrays

import bolts.Continuation
import bolts.Task

/**
 * A simple {@link android.app.Fragment} subclass.
 * Use the {@link com.starstorm.beer.fragment.FacebookFriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FacebookFriendsFragment// Required empty public constructor
: Fragment() {

    private val friendService = ParseFriendService.INSTANCE

    private var swipeLayout: SwipeRefreshLayout? = null
    private var addFriendNameText: TextView? = null
    private var facebookFriendAdapter: FacebookFriendAdapter? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        val callback = object : Request.GraphUserListCallback {
            override fun onCompleted(users: List<GraphUser>, response: Response) {

                facebookFriendAdapter = FacebookFriendAdapter(getActivity(), users)

                facebookFriendAdapter!!.addOnQueryLoadListener(object : ParseQueryAdapter.OnQueryLoadListener<ParseUser> {
                    override fun onLoading() {
                        if (getActivity() != null && swipeLayout != null) {
                            swipeLayout!!.setRefreshing(true)
                        }
                    }

                    override fun onLoaded(parseUsers: List<ParseUser>, e: Exception) {
                        if (getActivity() != null && swipeLayout != null) {
                            swipeLayout!!.setRefreshing(false)
                        }
                    }
                })
            }
        }

        val permissions = ParseFacebookUtils.getSession().getPermissions()
        if (permissions.contains("user_friends")) {
            // we already have the permission
            performRequest(callback)
        } else {
            // get the permission and then perform the request
            ParseFacebookUtils.linkInBackground(ParseUser.getCurrentUser(), Arrays.asList<String>("user_friends"), getActivity()).onSuccess<Void>(object : Continuation<Void, Void> {
                throws(javaClass<Exception>())
                override fun then(voidTask: Task<Void>): Void? {
                    performRequest(callback)
                    return null
                }
            })
        }
    }

    private fun performRequest(callback: Request.GraphUserListCallback) {
        Request.newMyFriendsRequest(ParseFacebookUtils.getSession(), callback).executeAsync()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeLayout = Views.findById<SwipeRefreshLayout>(view, R.id.swipe_container)
        addFriendNameText = Views.findById<TextView>(view, R.id.add_friend_text)

        val listView = Views.findById<ListView>(view, R.id.friend_listview)
        listView.setAdapter(facebookFriendAdapter)

        swipeLayout!!.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                facebookFriendAdapter!!.loadObjects()
            }
        })

        swipeLayout!!.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light)

        val addFriendButton = Views.findById<ImageButton>(view, R.id.add_friend_button)
        addFriendButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val username = addFriendNameText!!.getText().toString().toLowerCase()
                sendFriendRequest(username)
            }
        })
    }

    private fun sendFriendRequest(username: String) {
        setMenuWhirrerVisible(true)
        friendService.sendFriendRequest(username, object : FunctionCallback<Any> {
            override fun done(o: Any, e: ParseException?) {
                setMenuWhirrerVisible(false)
                if (e == null) {
                    Toaster.showShort(getActivity(), "sendfriendrequest success")
                    facebookFriendAdapter!!.loadObjects()
                    addFriendNameText!!.setText("")
                } else {
                    Crashlytics.logException(e)
                    Toaster.showShort(getActivity(), "Error: " + e!!.getMessage())
                }
            }
        })
    }

    private fun setMenuWhirrerVisible(visible: Boolean) {
        if (visible) {
            progressDialog = ProgressDialog(getActivity())
            progressDialog!!.show()
        } else {
            progressDialog!!.hide()
        }
    }

    class object {

        public fun newInstance(): FacebookFriendsFragment {
            return FacebookFriendsFragment()
        }
    }
}
