package com.example.foregroundserviceexample.location;

import android.location.Location;


public class LocationListenerBundle {
    private String drivingTime;
    private String distance;
    private Location location;

    public LocationListenerBundle(String drivingTime, String distance, Location location) {
        this.drivingTime = drivingTime;
        this.distance = distance;
        this.location = location;
    }

    public String getDrivingTime() {
        return drivingTime;
    }

    public String getDistance() {
        return distance;
    }


    public Location getLocation() {
        return location;
    }
}
