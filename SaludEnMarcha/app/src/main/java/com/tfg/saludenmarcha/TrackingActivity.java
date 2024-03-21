package com.tfg.saludenmarcha;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private FusedLocationProviderClient fusedLocationClient;
    private PolylineOptions polylineOptions;
    private boolean isTracking = false;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Chronometer chronometer;
    private TextView distanceTextView; // TextView para mostrar la distancia
    private float totalDistance = 0; // Distancia total recorrida
    private Location lastLocation = null; // Última ubicación conocida
    private List<LatLng> pathPoints = new ArrayList<>(); // Lista para almacenar los puntos del camino
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    //Botones
    private Button startButton;
    private Button stopButton;
    private Button pauseButton;
    private Button resumeButton;

    private long timeElapsed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tracking); // Mover esta línea aquí
        /*
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);
        */


        // Solicita los permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            // Continúa con la inicialización de tu actividad si ya tienes los permisos
            /*
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);*/
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync((OnMapReadyCallback) this);

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            polylineOptions = new PolylineOptions().width(10).color(Color.RED);

            locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000); // Establece el intervalo deseado para las actualizaciones de ubicación activas, en milisegundos.
            locationRequest.setFastestInterval(5000); // Establece la tasa más rápida para las actualizaciones de ubicación activas, en milisegundos.
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Establece la prioridad de la solicitud.

            chronometer = findViewById(R.id.chronometer);

            pauseButton = findViewById(R.id.pauseButton);
            pauseButton.setVisibility(View.GONE);

            //Boton RESUME
            resumeButton = findViewById(R.id.resumeButton);
            resumeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isTracking = true;
                    chronometer.setBase(SystemClock.elapsedRealtime() - timeElapsed);
                    chronometer.start();
                    startLocationUpdates();
                    //Desaparece el boton de resume y aparece el de pausa
                    resumeButton.setVisibility(View.GONE);
                    pauseButton.setVisibility(View.VISIBLE);
                    startButton.setVisibility(View.GONE);
                }
            });

            //Boton PAUSA
            pauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isTracking = false;
                    timeElapsed = SystemClock.elapsedRealtime() - chronometer.getBase();
                    chronometer.stop();
                    stopLocationUpdates();
                    //Desaparece el boton de pausa y aparece el de resume
                    pauseButton.setVisibility(View.GONE);
                    resumeButton.setVisibility(View.VISIBLE);
                }
            });

            //Boton START
            startButton = findViewById(R.id.startButton);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(TrackingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(TrackingActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(TrackingActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                    } else {
                        isTracking = true;
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.start();
                        startLocationUpdates();
                        //Desaparece el boton de start y aparece el de pausa
                        startButton.setVisibility(View.GONE);
                        pauseButton.setVisibility(View.VISIBLE);
                    }
                }
            });

            //BOTON PARAR
            stopButton = findViewById(R.id.stopButton);
            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isTracking = false;
                    chronometer.stop();
                    stopLocationUpdates();
                }
            });

            distanceTextView = findViewById(R.id.distanceTextView); // TextView con id 'distanceTextView' en tu layout

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null || !isTracking) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (location != null && isTracking) {
                            LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            pathPoints.add(newLocation);
                            polylineOptions.addAll(pathPoints);
                            mMap.clear();
                            mMap.addPolyline(polylineOptions);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15));

                            // Calcular la distancia
                            if (lastLocation != null) {
                                totalDistance += lastLocation.distanceTo(location) / 1000; // distanceTo() devuelve la distancia en metros, así que la convertimos a kilómetros
                            }
                            lastLocation = location;

                            // Actualizar la distancia en la interfaz de usuario
                            distanceTextView.setText(String.format(Locale.getDefault(), "%.2f km", totalDistance));
                        }
                    }
                }
            };
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                // Permiso denegado
            }
        }
    }

}