package com.example.foregroundserviceexample.dao.room;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface RouteDao {
    @Query("SELECT * FROM route WHERE route.isCompleted = 1 ORDER BY timeEnd desc")
    List<Route> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Route route);

    @Delete
    void delete(Route route);
}
