package com.example.findmybus;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class BusLocation {
    private GeoPoint geopoint;
    private @ServerTimestamp Date timestamp;

    public BusLocation() {}

    public BusLocation(GeoPoint geopoint, Date timestamp) {
        this.geopoint = geopoint;
        this.timestamp = timestamp;
    }

    public GeoPoint getGeopoint() {
        return geopoint;
    }

    public void setGeopoint(GeoPoint geopoint) {
        this.geopoint = geopoint;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
