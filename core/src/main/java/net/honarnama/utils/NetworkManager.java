package net.honarnama.utils;

import net.honarnama.HonarNamaBaseApp;
import net.honarnama.base.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by elnaz on 7/25/15.
 */
public class NetworkManager {

    public static NetworkManager mNetworkManagerInstance;

    public synchronized static NetworkManager getInstance()
    {
        if (mNetworkManagerInstance == null)
        {
            mNetworkManagerInstance = new NetworkManager();
        }
        return mNetworkManagerInstance;
    }

    public boolean isNetworkEnabled(Context context, Boolean displayToast) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        boolean isNetworkEnabled = activeNetworkInfo != null && activeNetworkInfo.isAvailable() &&
                activeNetworkInfo.isConnected();

        if (displayToast) {
            if (activeNetworkInfo == null) {
                Toast.makeText(context, context.getString(R.string.error_network_is_not_enabled), Toast.LENGTH_LONG).show();
            }
            if(activeNetworkInfo != null && (!activeNetworkInfo.isConnected() || !activeNetworkInfo.isAvailable()) )
            {
                Toast.makeText(context, context.getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();
            }
        }
        return isNetworkEnabled;

    }


}
