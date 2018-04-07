package com.example.foregroundserviceexample.dagger;

import com.example.foregroundserviceexample.MainActivity;
import com.example.foregroundserviceexample.location.LocationService;
import com.example.foregroundserviceexample.location.RouteRecorder;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(MainActivity activity);
    void inject(LocationService service);
    void inject(RouteRecorder recorder);
}
