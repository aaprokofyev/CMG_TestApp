package cmg.demo.cmg_testapp.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Home on 04.10.2016.
 */
public class NetworkManager {

    private final String TAG = getClass().getSimpleName();

    private static volatile NetworkManager instance;

    private Context context;
    private static boolean isNetworkAvailable = false;

    private NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();

    private static List<NetworkStateChanged> networkStateListeners = new ArrayList<>();


    private NetworkManager(Context context) {
        this.context = context;
        isNetworkAvailable = checkNetworkAvailable();
        registerNetworkReceiver();
    }


    public static NetworkManager getInstance(Context context) {
        NetworkManager localInstance = instance;
        if (localInstance == null) {
            synchronized (NetworkManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new NetworkManager(context);
                }
            }
        }
        return localInstance;
    }

    public void registerNetworkStateCallback(NetworkStateChanged networkStateChangedCallback) {
        if (!networkStateReceiver.isOrderedBroadcast()) {
            registerNetworkReceiver();
        }
        networkStateListeners.add(networkStateChangedCallback);
    }

    public void removeNetworkStateCallback(NetworkStateChanged networkStateChangedCallback) {
        if (networkStateListeners.isEmpty()) {
            unregisterNetworkReceiver();
        }
        networkStateListeners.remove(networkStateChangedCallback);
    }

    private boolean checkNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public boolean isNetworkAvailable() {
        return isNetworkAvailable;
    }

    public class NetworkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (checkNetworkAvailable()) {
                Log.d(TAG, "Network connection available");
                isNetworkAvailable = true;
                for (NetworkStateChanged listener : networkStateListeners) {
                    listener.onNetworkAvailable();
                }

            } else {
                Log.d(TAG, "Network connection unavailable");
                for (NetworkStateChanged listener : networkStateListeners) {
                    listener.onNetworkUnavailable();
                }
                isNetworkAvailable = false;
            }
        }
    }

    private void registerNetworkReceiver() {
        // Network state listener
        context.registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unregisterNetworkReceiver() {
        context.unregisterReceiver(networkStateReceiver);
    }

    public interface NetworkStateChanged {
        void onNetworkAvailable();

        void onNetworkUnavailable();
    }
}
