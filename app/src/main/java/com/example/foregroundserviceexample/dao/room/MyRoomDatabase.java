package com.example.foregroundserviceexample.dao.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;


@Database(entities = {Route.class, Position.class}, version = 10)
@TypeConverters({DateConverters.class})
public abstract class MyRoomDatabase extends RoomDatabase {
    public abstract RouteDao routeDao();
    public abstract PositionDao positionDao();
}
