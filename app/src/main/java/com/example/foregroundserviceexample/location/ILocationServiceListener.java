package com.example.foregroundserviceexample.location;


public interface ILocationServiceListener {
    void onUpdate(LocationListenerBundle bundle);
    void onError(LocationServiceError error);
    void onRouteStopped();
}
