package com.master.javi.rutapp.data;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo equivalente al del servidor. Representa un punto en el mapa.
 */
public class Point {

    // Anotación necesaria para mapear la clave "long" del JSON de respuesta a este atributo.
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
