package com.example.findmybus;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserLocation {
    private GeoPoint geoPoint;
    private @ServerTimestamp Date timestamp;

    public UserLocation(GeoPoint geoPoint, Date timestamp) {
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
    }

    public UserLocation() {
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
