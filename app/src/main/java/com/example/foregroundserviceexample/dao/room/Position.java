package com.example.foregroundserviceexample.dao.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(foreignKeys = {
        @ForeignKey(entity = Route.class,
                parentColumns = "id",
                childColumns = "routeId",
                onDelete = CASCADE)},
        indices = {@Index("routeId")}
)
public class Position {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    long id;
    @ColumnInfo(name = "routeId")
    public long routeId;
    public double latitude;
    public double longitude;
    public Date timestamp;

    @Ignore
    public Position() {
    }

    public Position(long id, long routeId, double latitude, double longitude, Date timestamp) {
        this.id = id;
        this.routeId = routeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}
