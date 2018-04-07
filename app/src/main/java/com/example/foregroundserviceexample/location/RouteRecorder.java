package com.example.foregroundserviceexample.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.example.foregroundserviceexample.dagger.AppComponent;
import com.example.foregroundserviceexample.dao.room.MyRoomDatabase;
import com.example.foregroundserviceexample.dao.room.Position;
import com.example.foregroundserviceexample.dao.room.PositionDao;
import com.example.foregroundserviceexample.dao.room.Route;
import com.example.foregroundserviceexample.dao.room.RouteDao;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class RouteRecorder {
    private static final float LOCATION_TOO_CLOSE_THRESHOLD = 50; // Meters

    private final Context _context;
    private boolean _isRecording = false;
    private List<Location> _locations = new ArrayList<>();
    private Route _route;

    @Inject
    MyRoomDatabase roomDb;

    public RouteRecorder(AppComponent component, Context appContext) {
        component.inject(this);
        _context = appContext;
    }

    public void startRoute() {
        _isRecording = true;
        clear();

        _route = new Route(0L,
                "123",
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis()),
                0F,
                "",
                "",
                false);

        insertRoute();
    }

    public void stopAndDeleteRoute() {
        _isRecording = false;

        Observable.just(roomDb.routeDao())
                .subscribeOn(Schedulers.io())
                .subscribe(new DisposableObserver<RouteDao>() {
                    @Override
                    public void onNext(RouteDao routeDao) {
                        routeDao.delete(_route);
                        clear();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @SuppressLint("MissingPermission")
    public boolean stopRoute() {
        _isRecording = false;

        if (_locations.size() < 1) {
            // Cannot use routes with no locations
            Observable.just(roomDb.routeDao())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DisposableObserver<RouteDao>() {
                        @Override
                        public void onNext(RouteDao routeDao) {
                            routeDao.delete(_route);
                            clear();
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onComplete() {
                        }
                    });

            return false;
        }

        _route.timeEnd = new Date(System.currentTimeMillis());
        _route.isCompleted = true;
        _route.distance = getDistance();

        Geocoder geocoder = new Geocoder(_context);
        try {
            Location startLocation = _locations.get(0);
            Location stopLocation = _locations.get(_locations.size() - 1);

            Address startAddr = geocoder.getFromLocation(startLocation.getLatitude(), startLocation.getLongitude(), 1).get(0);
            Address endAddr = geocoder.getFromLocation(stopLocation.getLatitude(), stopLocation.getLongitude(), 1).get(0);
            _route.startAddress = startAddr.getAddressLine(0);
            _route.stopAddress = endAddr.getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get a final "stop" location
        Task<Location> locationTask = LocationServices.getFusedLocationProviderClient(_context).getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    location.setTime(System.currentTimeMillis());
                    insertLocation(location, new IRouteRecorderCallback() {
                        @Override
                        public void onComplete() {
                            insertRoute();
                        }
                    });
                }
                insertRoute();
            }
        });
        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Unable to get a final stop location, insert the route as-is
                insertRoute();
            }
        });

        return true;
    }

    private void insertRoute() {
        Observable.just(roomDb.routeDao())
                .subscribeOn(Schedulers.io())
                .subscribe(new DisposableObserver<RouteDao>() {
                    @Override
                    public void onNext(RouteDao routeDao) {
                        _route.id = routeDao.insert(_route);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void insertLocation(final Location location, final IRouteRecorderCallback callback) {
        if (_route.id == 0) {
            // Resolve crash caused by trying to insert a location quickly
            // after starting a route, meaning the route has not yet been successfully
            // inserted in the db yet. Ignore the location
            return;
        }

        // Ignore location updates that are very close to eachother
        if (_locations.size() > 0) {
            Location previousLocation = _locations.get(_locations.size() - 1);
            float distance = previousLocation.distanceTo(location);
            if (distance < LOCATION_TOO_CLOSE_THRESHOLD) return;
        }

        _locations.add(location);

        Observable.just(roomDb.positionDao())
                .subscribeOn(Schedulers.io())
                .subscribe(new DisposableObserver<PositionDao>() {
                    @Override
                    public void onNext(PositionDao dao) {
                        Position position = new Position(0,
                                _route.id,
                                location.getLatitude(),
                                location.getLongitude(),
                                new Date(System.currentTimeMillis()));

                        dao.insert(position);

                        if (callback != null) {
                            callback.onComplete();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void clear() {
        _locations.clear();
        _route = null;
    }

    public boolean isRecording() {
        return _isRecording;
    }

    // Return distance in km
    public float getDistance() {
        float distance = 0;
        if (_locations.size() < 2) return distance;

        for (int i = 1; i < _locations.size(); i++) {
            Location a = _locations.get(i - 1);
            Location b = _locations.get(i);

            distance += a.distanceTo(b);
        }

        float theDistance = distance / 1000; // Convert meters to kilometers
        return theDistance;
    }

    public List<Location> getLocations() {
        return _locations;
    }

    public Route getRoute() {
        return _route;
    }
}
