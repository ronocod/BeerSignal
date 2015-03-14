package com.starstorm.beer.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.novoda.notils.caster.Views;
import com.parse.FunctionCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;
import com.starstorm.beer.R;
import com.starstorm.beer.adapter.RecipientAdapter;
import com.starstorm.beer.service.ParseSignalService;
import com.starstorm.beer.util.Toaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BigRedFragment extends Fragment {

    private final ParseSignalService signalService = ParseSignalService.INSTANCE;

    private CheckBox sendToAllCheckbox;
    private SwipeRefreshLayout swipeLayout;
    private RecipientAdapter recipientAdapter;
    private ProgressDialog progressDialog;

    public static BigRedFragment newInstance() {
        return new BigRedFragment();
    }

    public BigRedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recipientAdapter = new RecipientAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_big_red, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeLayout = Views.findById(view, R.id.swipe_container);
        sendToAllCheckbox = Views.findById(view, R.id.send_to_all_checkbox);

        ListView recipientListView = Views.findById(view, R.id.recipient_listview);
        recipientListView.setAdapter(recipientAdapter);
        recipientAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            @Override
            public void onLoading() {
            }

            @Override
            public void onLoaded(List<ParseObject> parseUsers, Exception e) {
                if (getActivity() != null && swipeLayout != null) {
                    swipeLayout.setRefreshing(false);
                }
            }
        });

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recipientAdapter.loadObjects();
            }
        });
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        sendToAllCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    swipeLayout.setVisibility(View.GONE);
                } else {
                    swipeLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        ImageButton bigRedButton = Views.findById(view, R.id.big_red_button);
        bigRedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireSignal();
            }
        });
    }

    private void fireSignal() {
        setMenuWhirrerVisible(true);

        FunctionCallback<Object> callback = new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                setMenuWhirrerVisible(false);
                if (e == null) {
                    Toaster.showShort(getActivity(), "Signal sent");
                } else {
                    Crashlytics.logException(e);
                    Toaster.showShort(getActivity(), "Error: " + e.getMessage());
                }
            }
        };

        if (sendToAllCheckbox.isChecked()) {
            signalService.fireSignal(callback);
        } else {
            Set<String> objectIdSet = recipientAdapter.getRecipients().keySet();
            List<String> objectIds = new ArrayList<>(objectIdSet.size());
            for (String id : objectIdSet) {
                objectIds.add(id);
            }
            signalService.fireSignal(objectIds, callback);
        }
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
