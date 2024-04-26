package com.example.firebase_location;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

public class NetworkUtils {

    public static void enableBackgroundDataUsage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Request the CHANGE_NETWORK_STATE permission if not already granted
            if (!hasChangeNetworkStatePermission(context)) {
                // Request the permission from the user
                // Implement code to request the permission here
                return;
            }
        }

        // Get the ConnectivityManager
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            // Create a NetworkRequest to specify the network capabilities
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);

            // Request a network with the specified capabilities
            connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback());
        }
    }

    private static boolean hasChangeNetworkStatePermission(Context context) {
        return context.checkSelfPermission(android.Manifest.permission.CHANGE_NETWORK_STATE) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }
}



