package com.example.foregroundserviceexample;

import android.app.Application;
import android.content.Intent;
import android.support.v4.content.ContextCompat;

import com.example.foregroundserviceexample.dagger.AppComponent;
import com.example.foregroundserviceexample.dagger.AppModule;
import com.example.foregroundserviceexample.dagger.DaggerAppComponent;
import com.example.foregroundserviceexample.location.LocationService;

public class ForegroundServiceExampleApp extends Application {
    private AppComponent mComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        Intent startBackgroundIntent = new Intent();
        startBackgroundIntent.setClass(this, LocationService.class);
        startService(startBackgroundIntent);
    }

    public AppComponent getDaggerComponent() {
        return mComponent;
    }
}
