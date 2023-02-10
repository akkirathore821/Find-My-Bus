package com.example.findmybus;

import com.google.firebase.firestore.GeoPoint;

public class Bus {
    private String busId;
    private String busNo;
    private String route;
    private String email;
    private String endStop1;
    private String endStop2;
    private GeoPoint endStop1Location;
    private GeoPoint endStop2Location;

    public Bus() {}

    public Bus(String busId, String busNo, String route, String email, String endStop1, String endStop2, GeoPoint endStop1Location, GeoPoint endStop2Location) {
        this.busId = busId;
        this.busNo = busNo;
        this.route = route;
        this.email = email;
        this.endStop1 = endStop1;
        this.endStop2 = endStop2;
        this.endStop1Location = endStop1Location;
        this.endStop2Location = endStop2Location;
    }

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public String getBusNo() {
        return busNo;
    }

    public void setBusNo(String busNo) {
        this.busNo = busNo;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndStop1() {
        return endStop1;
    }

    public void setEndStop1(String endStop1) {
        this.endStop1 = endStop1;
    }

    public String getEndStop2() {
        return endStop2;
    }

    public void setEndStop2(String endStop2) {
        this.endStop2 = endStop2;
    }

    public GeoPoint getEndStop1Location() {
        return endStop1Location;
    }

    public void setEndStop1Location(GeoPoint endStop1Location) {
        this.endStop1Location = endStop1Location;
    }

    public GeoPoint getEndStop2Location() {
        return endStop2Location;
    }

    public void setEndStop2Location(GeoPoint endStop2Location) {
        this.endStop2Location = endStop2Location;
    }
}
