package com.example.firebase_location;
import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.location.Location;


import com.example.firebase_location.ForegroundService;
import com.example.firebase_location.R;
import com.example.firebase_location.ReminderBroadcast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


import android.app.AlertDialog;



public class MainActivity extends AppCompatActivity {
    private static final String ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS =
            "android.settings.IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS";

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private static final int MY_PERMISSIONS_REQUEST_CHANGE_NETWORK_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigateToDataUsageSettings(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//         Create location request
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(8 * 60 * 1000); // 10 seconds

        //create location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CHANGE_NETWORK_STATE},
                    MY_PERMISSIONS_REQUEST_CHANGE_NETWORK_STATE);
        } else {
            // Permission is already granted, call the method
            NetworkUtils.enableBackgroundDataUsage(this);
        }


        Button centerButton = findViewById(R.id.Button);
        centerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermission();
                checkAndRequestPermission1();
//                navigateToBatteryOptimizationSettings();
//                navigateToBackgroundDataSettings1();
//                navigateToBackgroundDataSettings();

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
            }
        });


        Button centerButton2 = findViewById(R.id.button2);
        centerButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stopServiceIntent = new Intent(MainActivity.this, ForegroundService.class);
                stopService(stopServiceIntent);
            }
        });

        // Initialize the permission launcher
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                scheduleExactAlarm();
            } else {
                Toast.makeText(MainActivity.this, "Permission denied. Alarm cannot be scheduled.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkAndRequestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            requestPermissionLauncher.launch(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            Toast.makeText(this, "Requesting permission to ignore battery optimizations ", Toast.LENGTH_SHORT).show();
        } else {
            // Permission already granted, schedule the alarm
            scheduleExactAlarm();
        }
    }

    private static final int REQUEST_CODE = 1001;

    private void checkAndRequestPermission1() {
        String[] permissions = {
                Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };


        // Check if permissions are granted
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                // Request permissions
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
                break;
            }
        }

        if (allPermissionsGranted) {
            // All permissions are granted, proceed with starting the foreground service
            startForegroundService(new Intent(this, ForegroundService.class));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            // Check if all permissions are granted
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // All permissions are granted, proceed with starting the foreground service
                startForegroundService(new Intent(this, ForegroundService.class));
            } else {
                // Permissions are not granted, handle accordingly
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == MY_PERMISSIONS_REQUEST_CHANGE_NETWORK_STATE) {
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, call the method
                NetworkUtils.enableBackgroundDataUsage(this);
            } else {
                // Permission is denied, handle accordingly
                // For example, show a message or disable relevant functionality
            }
        }
    }




    /*shedule alarm*/
    private void scheduleExactAlarm() {
        Toast.makeText(MainActivity.this, "Reminder set!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            long timeInMillis = System.currentTimeMillis() + (10*1000); // 1 minute in milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            }

            // Start the foreground service
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

        }
    }



    // location update

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    private void startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
//    private void navigateToBackgroundDataSettings() {
//        Intent intent = new Intent(ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivity(intent);
//        } else {
//            // Provide an alternative action or message to the user
//            showAlternativeMessage();
//        }
//    }
//
//    private void showAlternativeMessage() {
//        // You can show a dialog, another activity, or just a toast to inform the user.
//        // For now, let's just show a toast message.
//        Toast.makeText(this, "Feature not available on this device", Toast.LENGTH_SHORT).show();
//    }
//    private void navigateToBackgroundDataSettings1() {
//        Intent intent = new Intent(Settings.ACTION_DATA_USAGE_SETTINGS);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivity(intent);
//            navigateToBatteryOptimizationSettings();
//        } else {
//            // Provide an alternative action or message to the user
//            showAlternativeMessage1();
//        }
//    }
//
//    private void showAlternativeMessage1() {
//        // You can show a dialog, another activity, or just a toast to inform the user.
//        // For now, let's just show a toast message.
//        Toast.makeText(this, "Feature not available on this device", Toast.LENGTH_SHORT).show();
//    }
//    private void navigateToBatteryOptimizationSettings() {
//        try {
//            // Create an intent with the action for battery optimization settings
//            Intent batteryOptimizationIntent = new Intent();
//            batteryOptimizationIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//
//            // Start the activity with the battery optimization intent
//            startActivity(batteryOptimizationIntent);
//
//            // Create an intent with the action for battery power consumption settings
//            Intent powerConsumptionIntent = new Intent();
//            powerConsumptionIntent.setAction(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS);
//
//            // Start the activity with the power consumption intent
//            startActivity(powerConsumptionIntent);
//        } catch (ActivityNotFoundException e) {
//            // Handle the case where the activity is not found
//            Toast.makeText(this, "Battery optimization settings not available", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }
//    }
private void navigateToDataUsageSettings(Context context) {
    // Intent to open the data usage settings
    Intent intent = new Intent();
    intent.setAction(android.provider.Settings.ACTION_DATA_USAGE_SETTINGS);
    // Check if there is any activity to handle this intent
    if (intent.resolveActivity(context.getPackageManager()) != null) {
        // Start the activity
        context.startActivity(intent);
    } else {
        // If there is no activity to handle the intent, notify the user
        // You can display a toast message or show an alert dialog
        // For simplicity, I'm just logging a message here
        System.err.println("No activity found to handle the intent.");
    }
}
}