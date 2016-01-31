package net.honarnama.core.utils;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

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

    public boolean isNetworkEnabled(Boolean displayToast) {
        Context context = HonarnamaBaseApp.getInstance();
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        boolean isNetworkEnabled = activeNetworkInfo != null && activeNetworkInfo.isAvailable() &&
                activeNetworkInfo.isConnected();

        if (displayToast) {
            if (activeNetworkInfo == null) {
                Toast.makeText(HonarnamaBaseApp.getInstance(), context.getString(R.string.error_network_is_not_enabled), Toast.LENGTH_SHORT).show();
            }
            if(activeNetworkInfo != null && (!activeNetworkInfo.isConnected() || !activeNetworkInfo.isAvailable()) )
            {
                Toast.makeText(HonarnamaBaseApp.getInstance(), context.getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();
            }
        }
        return isNetworkEnabled;

    }


}
