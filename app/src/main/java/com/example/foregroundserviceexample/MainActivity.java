package com.example.foregroundserviceexample;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.foregroundserviceexample.location.ILocationServiceListener;
import com.example.foregroundserviceexample.location.LocationListenerBundle;
import com.example.foregroundserviceexample.location.LocationService;
import com.example.foregroundserviceexample.location.LocationServiceError;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 999;

    private LocationService.LocationServiceBinder _serviceBinder;
    private ILocationServiceListener _serviceListener = new ILocationServiceListener() {
        @Override
        public void onUpdate(final LocationListenerBundle bundle) {
        }

        @Override
        public void onError(LocationServiceError error) {

        }

        @Override
        public void onRouteStopped() {
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            _serviceBinder = (LocationService.LocationServiceBinder) service;
            _serviceBinder.registerListener(_serviceListener);
            _serviceBinder.startRecording();
        }

        public void onServiceDisconnected(ComponentName className) {
            _serviceBinder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askForPermissions();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bindService(new Intent(MainActivity.this, LocationService.class),
                            mConnection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
                }
                return;
            }
        }
    }

    private void askForPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST);
    }
}
