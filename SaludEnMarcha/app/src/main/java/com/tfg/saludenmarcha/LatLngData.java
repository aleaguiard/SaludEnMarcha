package com.tfg.saludenmarcha;
//Clase que almacena la latitud y longitud de un punto para ser guardado en Firebase
public class LatLngData {
    public double latitude;
    public double longitude;

    public LatLngData() {
        // Default constructor required for calls to DataSnapshot.getValue(LatLngData.class)
    }

    public LatLngData(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}