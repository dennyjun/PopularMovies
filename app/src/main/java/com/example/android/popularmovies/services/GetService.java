package com.example.android.popularmovies.services;

import android.app.IntentService;
import android.content.Intent;

import com.example.android.popularmovies.requests.GetRequest;
import com.example.android.popularmovies.requests.GetRequestResult;

/**
 * Created by Denny on 8/19/2015.
 *
 * The GetService takes the GetRequest and downloads whatever it needs using the internet.
 * The JSON result is forwarded to the appropriate receiver specified in the GetRequest.
 */
public class GetService extends IntentService {
    public GetService() {
        super(GetService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final GetRequest getRequest = getDownloadRequestFromIntent(intent);
        final GetRequestResult result = getRequest.handleServiceIntent(getBaseContext(), intent);
        sendResultToReceiver(getRequest, result);
    }

    private GetRequest getDownloadRequestFromIntent(Intent intent) {
        return (GetRequest) intent.getSerializableExtra(
                GetRequest.class.getCanonicalName());
    }

    private void sendResultToReceiver(final GetRequest getRequest, final GetRequestResult result) {
        final Intent broadcastIntent = new Intent(getRequest.getBroadcastReceiverIntent());
        broadcastIntent.putExtra(Intent.EXTRA_STREAM, result);
        sendBroadcast(broadcastIntent);
    }
}
