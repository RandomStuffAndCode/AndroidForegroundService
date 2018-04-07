package com.example.foregroundserviceexample.dao.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PositionDao {
    @Query("SELECT * FROM position")
    List<Position> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Position position);
}