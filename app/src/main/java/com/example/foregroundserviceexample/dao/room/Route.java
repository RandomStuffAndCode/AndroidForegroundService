package com.example.foregroundserviceexample.dao.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;

import com.example.foregroundserviceexample.common.DateHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


@Entity
public class Route {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;
    public String userId;
    public Date timeStart;
    public Date timeEnd;
    public float distance;
    public String startAddress;
    public String stopAddress;
    public boolean isCompleted;

    @Ignore
    public Route() {
    }

    public Route(long id, String userId, Date timeStart, Date timeEnd, float distance, String startAddress, String stopAddress, boolean isCompleted) {
        this.id = id;
        this.userId = userId;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.distance = distance;
        this.startAddress = startAddress;
        this.stopAddress = stopAddress;
        this.isCompleted = isCompleted;
    }
}