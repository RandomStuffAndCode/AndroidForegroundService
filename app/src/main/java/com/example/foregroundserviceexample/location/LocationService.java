package com.example.foregroundserviceexample.location;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.foregroundserviceexample.ForegroundServiceExampleApp;
import com.example.foregroundserviceexample.MainActivity;
import com.example.foregroundserviceexample.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import javax.inject.Inject;


public class LocationService extends Service {
    private static final int NOTIFICATION_ID = 123;
    private static final int REQUEST_CODE = 123;
    private static final String ROUTE_CHANNEL_ID = "LocationService.ROUTE_CHANNEL_ID";

    private RouteRecorder _recorder;
    private List<ILocationServiceListener> _listeners = new ArrayList<>();

    private Timer _timer;
    private LocationTimerTask _timerTask;

    private FusedLocationProviderClient _fusedLocationClient;
    private NotificationCompat.Builder _notificationBuilder;

    public void notifyListener(Location location) {
        for(ILocationServiceListener listener : _listeners) {
            listener.onUpdate(new LocationListenerBundle(
                    String.valueOf(System.currentTimeMillis() - _recorder.getRoute().timeStart.getTime()),
                    String.valueOf(_recorder.getDistance()),
                    location));
        }

        if(_recorder.isRecording()) {
            _notificationBuilder.setContentText(notificationContentTextFactory());
            _notificationBuilder.setWhen(System.currentTimeMillis());
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, _notificationBuilder.build());
        }
    }

    public void notifyListenerError(LocationServiceError error) {
        for(ILocationServiceListener listener : _listeners) {
            listener.onError(error);
        }
    }

    private LocationCallback _locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }

            for (final Location location : locationResult.getLocations()) {
                _recorder.insertLocation(location, new IRouteRecorderCallback() {
                    @Override
                    public void onComplete() {
                        notifyListener(location);
                    }
                });
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        ForegroundServiceExampleApp app = (ForegroundServiceExampleApp) getApplication();
        app.getDaggerComponent().inject(this);

        _recorder = new RouteRecorder(app.getDaggerComponent(), this);
        _fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        registerNotificationChannel();
    }

    private void registerNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.locationservice_notificationchannel_name);
            String description = getString(R.string.locationservice_notificationchannel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(ROUTE_CHANNEL_ID, name, importance);
            mChannel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(_notificationBuilder == null) {
            Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, REQUEST_CODE, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            _notificationBuilder = new NotificationCompat.Builder(this, ROUTE_CHANNEL_ID);
            _notificationBuilder.setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(getString(R.string.locationservice_notification_isrecording))
                    .setContentText("")
                    .setOnlyAlertOnce(true)
                    .setContentIntent(contentIntent);
        }

        _notificationBuilder.setWhen(System.currentTimeMillis());

        startForeground(NOTIFICATION_ID, _notificationBuilder.build());
        return START_NOT_STICKY;
    }

    private LocationRequest locationRequestFactory() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000); // 5 sec
        request.setFastestInterval(5000); // 5 sec
        return request;
    }


    private String notificationContentTextFactory() {
        String result = String.valueOf(System.currentTimeMillis() - _recorder.getRoute().timeStart.getTime()) + " - " + String.valueOf(_recorder.getDistance());
        return result;
    }

    @Override
    public void onDestroy() {
        Analytics.trackEvent("LocationService onDestroy()");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocationServiceBinder();
    }

    public class LocationServiceBinder extends Binder {

        public void registerListener(ILocationServiceListener listener) {
            if(_listeners.contains(listener) == false) {
                LocationService.this._listeners.add(listener);
            }
        }

        public void unregisterListener(ILocationServiceListener listener) {
            if(_listeners.contains(listener) == true) {
                _listeners.remove(listener);
            }
        }

        @SuppressLint("MissingPermission")
        public void startRecording() {
            startForeground(NOTIFICATION_ID, _notificationBuilder.build());
            _fusedLocationClient.requestLocationUpdates(locationRequestFactory(), _locationCallback, null);
            _recorder.startRoute();
            startTimer();
        }

        private void startTimer() {
            if(_timer != null) {
                _timer.cancel();
            }

            _timer = new Timer();
            _timerTask = new LocationTimerTask(new ILocationTimerTaskCallback() {
                @Override
                public void onTimeTrigger() {
                    notifyListener(null);
                }
            });
            _timer.schedule(_timerTask, 0, 1000); // Every 1 sec
        }

        public void stopRecording() {
            stop();
        }

        private void stop() {
            _timer.cancel();
            stopForeground(true);
            _fusedLocationClient.removeLocationUpdates(_locationCallback);

            boolean isSuccess = _recorder.stopRoute();
            for(ILocationServiceListener listener : _listeners) {
                if(isSuccess == false) {
                    listener.onError(LocationServiceError.CannotSaveRoute);
                } else {
                    listener.onRouteStopped();
                }
            }
        }
    }
}