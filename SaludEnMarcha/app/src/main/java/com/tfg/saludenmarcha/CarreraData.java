package com.tfg.saludenmarcha;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.List;

/**
 * CarreraData es una clase que representa los datos de una carrera o actividad física.
 * Implementa la interfaz Serializable para permitir que los objetos de esta clase se puedan serializar.
 * Almacena información como el ID de la carrera, tiempo transcurrido, distancia total, fecha y hora de inicio y finalización,
 * tipo de actividad.
 */
public class CarreraData implements Serializable {
    private String raceId;
    private long timeElapsed;
    private double totalDistance;
    private int day;
    private int month;
    private int year;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private String activityType;
    private List<GeoPoint> routeGps;

    // Constructor de la clase
    public CarreraData() {
    }

    // Getters y setters de los atributos de la clase
    public String getRaceId() {
        return raceId;
    }

    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public List<GeoPoint> getRouteGps() {
        return routeGps;
    }

    public void setRouteGps(List<GeoPoint> routeGps) {
        this.routeGps = routeGps;
    }
}
