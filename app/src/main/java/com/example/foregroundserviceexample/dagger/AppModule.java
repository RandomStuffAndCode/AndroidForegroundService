package com.example.foregroundserviceexample.dagger;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.example.foregroundserviceexample.dao.room.MyRoomDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private final Context _context;

    public AppModule(Context context) {
        _context = context;
    }

    @Provides
    @Singleton
    MyRoomDatabase provideMyRoomDatabase() {
        return Room.databaseBuilder(_context, MyRoomDatabase.class, "route-room-db")
                .fallbackToDestructiveMigration()
                .build();
    }
}
