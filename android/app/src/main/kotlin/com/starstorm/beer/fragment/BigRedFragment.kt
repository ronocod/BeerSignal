package com.starstorm.beer.fragment

import android.app.Fragment
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.ListView

import com.crashlytics.android.Crashlytics
import com.novoda.notils.caster.Views
import com.parse.FunctionCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQueryAdapter
import com.starstorm.beer.R
import com.starstorm.beer.adapter.RecipientAdapter
import com.starstorm.beer.service.ParseSignalService
import com.starstorm.beer.util.Toaster

import java.util.ArrayList

public class BigRedFragment : Fragment() {

    private val signalService = ParseSignalService.INSTANCE

    private var sendToAllCheckbox: CheckBox? = null
    private var recipientAdapter: RecipientAdapter? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        recipientAdapter = RecipientAdapter(getActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_big_red, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val swipeLayout = view.findViewById(R.id.swipe_container) as SwipeRefreshLayout

        sendToAllCheckbox = Views.findById<CheckBox>(view, R.id.send_to_all_checkbox)

        val recipientAdapter = recipientAdapter!!
        val recipientListView = Views.findById<ListView>(view, R.id.recipient_listview)
        recipientListView.setAdapter(recipientAdapter)
        recipientAdapter.addOnQueryLoadListener(object : ParseQueryAdapter.OnQueryLoadListener<ParseObject> {
            override fun onLoading() {
            }

            override fun onLoaded(parseUsers: List<ParseObject>, e: Exception) {
                if (getActivity() != null) {
                    swipeLayout.setRefreshing(false)
                }
            }
        })

        swipeLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                recipientAdapter.loadObjects()
            }
        })
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light)

        sendToAllCheckbox!!.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(compoundButton: CompoundButton, checked: Boolean) {
                if (checked) {
                    swipeLayout.setVisibility(View.GONE)
                } else {
                    swipeLayout.setVisibility(View.VISIBLE)
                }
            }
        })

        val bigRedButton = view.findViewById(R.id.big_red_button) as ImageButton
        bigRedButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                fireSignal()
            }
        })
    }

    private fun fireSignal() {
        setMenuWhirrerVisible(true)

        val callback = object : FunctionCallback<Any> {
            override fun done(o: Any, e: ParseException?) {
                setMenuWhirrerVisible(false)
                if (e == null) {
                    Toaster.showShort(getActivity(), "Signal sent")
                } else {
                    Crashlytics.logException(e)
                    Toaster.showShort(getActivity(), "Error: ${e.getMessage()}")
                }
            }
        }

        if (sendToAllCheckbox!!.isChecked()) {
            signalService.fireSignal(callback)
        } else {
            val objectIdSet = recipientAdapter!!.getRecipients().keySet()
            val objectIds = ArrayList<String>(objectIdSet.size())
            for (id in objectIdSet) {
                objectIds.add(id)
            }
            signalService.fireSignal(objectIds, callback)
        }
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

        public fun newInstance(): BigRedFragment {
            return BigRedFragment()
        }
    }
}
