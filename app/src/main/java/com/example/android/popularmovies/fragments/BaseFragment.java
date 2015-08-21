package com.example.android.popularmovies.fragments;


import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;

import com.example.android.popularmovies.receivers.ManagedReceiver;
import com.example.android.popularmovies.receivers.OnConnectReceiver;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Denny on 8/19/2015.
 * Handles registering and unregistering of managed receivers.
 */
public abstract class BaseFragment extends Fragment {
    private OnConnectReceiver onConnectReceiver;
    private List<ManagedReceiver> managedReceivers;

    @Override
    public void onStart() {
        super.onStart();
        onConnectReceiver = new OnConnectReceiver() {
            @Override
            public void run() {
                onInternetConnected();
            }
        };
        final IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        managedReceivers = new LinkedList<>();
        managedReceivers.add(new ManagedReceiver(onConnectReceiver, intentFilter));
        final List<ManagedReceiver> toAdd = getReceiversToManage();
        if(toAdd != null) {
            managedReceivers.addAll(toAdd);
        }

        for(final ManagedReceiver managedReceiver : managedReceivers) {
            managedReceiver.register(getActivity());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        for(final ManagedReceiver managedReceiver : managedReceivers) {
            managedReceiver.unregister(getActivity());
        }
    }

    /**
     * Will register and unregister receivers automatically onStart and onStop
     * @return
     */
    public abstract List<ManagedReceiver> getReceiversToManage();

    /**
     * Do when app connects to the internet
     */
    public abstract void onInternetConnected();
}
