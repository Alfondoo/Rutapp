package com.master.javi.rutapp.data;

import com.google.gson.annotations.SerializedName;

public class Point {

    @SerializedName("long")
    private double longitude;
    private double height;
    @SerializedName("lat")
    private double latitude;

    public Point(double longitude, double latitude, double height) {
        this.longitude = longitude;
        this.height = height;
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
