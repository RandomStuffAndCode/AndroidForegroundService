package com.example.foregroundserviceexample.location;

import java.util.TimerTask;

public class LocationTimerTask extends TimerTask {
    private final ILocationTimerTaskCallback _callback;

    public LocationTimerTask(ILocationTimerTaskCallback callback) {
        _callback = callback;
    }

    @Override
    public void run() {
        if(_callback != null) {
            _callback.onTimeTrigger();
        }
    }
}
