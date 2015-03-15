package com.starstorm.beer.fragment

import android.app.AlertDialog
import android.app.Fragment
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView

import com.crashlytics.android.Crashlytics
import com.novoda.notils.caster.Views
import com.parse.FunctionCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQueryAdapter
import com.starstorm.beer.R
import com.starstorm.beer.adapter.FriendAdapter
import com.starstorm.beer.service.ParseFriendService
import com.starstorm.beer.util.Toaster

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment// Required empty public constructor
: Fragment() {

    private val friendService = ParseFriendService

    private var swipeLayout: SwipeRefreshLayout? = null
    private var addFriendNameText: TextView? = null

    private var friendAdapter: FriendAdapter? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        friendAdapter = FriendAdapter(getActivity())

        friendAdapter!!.addOnQueryLoadListener(object : ParseQueryAdapter.OnQueryLoadListener<ParseObject> {
            override fun onLoading() {
            }

            override fun onLoaded(parseUsers: List<ParseObject>, e: Exception) {
                if (getActivity() != null && swipeLayout != null) {
                    swipeLayout!!.setRefreshing(false)
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeLayout = Views.findById<SwipeRefreshLayout>(view, R.id.swipe_container)
        addFriendNameText = Views.findById<TextView>(view, R.id.add_friend_text)

        val listView = Views.findById<ListView>(view, R.id.friend_listview)
        listView.setAdapter(friendAdapter)

        swipeLayout!!.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                friendAdapter!!.loadObjects()
            }
        })
        swipeLayout!!.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light)

        listView.setOnItemLongClickListener(object : AdapterView.OnItemLongClickListener {
            override fun onItemLongClick(adapterView: AdapterView<*>, view: View, i: Int, l: Long): Boolean {

                val items = array("Unfriend", "Cancel")
                val builder = AlertDialog.Builder(getActivity()).setTitle(friendAdapter!!.getFriendName(i)).setItems(items, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        when (which) {
                            0 -> sendUnfriendRequest(friendAdapter!!.getItem(i))
                            1 -> dialog.cancel()
                        }
                    }
                })
                builder.show()
                return false
            }
        })

        val addFriendButton = Views.findById<ImageButton>(view, R.id.add_friend_button)
        addFriendButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val username = addFriendNameText!!.getText().toString().toLowerCase()
                sendFriendRequest(username)
            }
        })
    }

    private fun sendUnfriendRequest(friendship: ParseObject) {
        setMenuWhirrerVisible(true)
        friendService.sendUnfriendRequest(friendship, object : FunctionCallback<Any> {
            override fun done(o: Any, e: ParseException?) {
                setMenuWhirrerVisible(false)
                if (e == null) {
                    Toaster.showShort(getActivity(), "unfriend success")
                    friendAdapter!!.loadObjects()
                } else {
                    Crashlytics.logException(e)
                    Toaster.showShort(getActivity(), "Error: " + e.getMessage())
                }
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
                    friendAdapter!!.loadObjects()
                    addFriendNameText!!.setText("")
                } else {
                    Crashlytics.logException(e)
                    Toaster.showShort(getActivity(), "Error: " + e.getMessage())
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

        public fun newInstance(): FriendsFragment {
            return FriendsFragment()
        }
    }
}
