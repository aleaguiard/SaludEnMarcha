package com.tfg.saludenmarcha;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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

    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    CollectionReference pathPointsRef;
    List<LatLngData> pathPointsData;
    String raceId;
    DocumentReference raceRef;
    String activityType;

    private static final String TAG = "TrackingActivity";

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


            //
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();
            //***NOMBRE DE LA COLECCION QUE LE TENGO QUE PONER A LOS PUNTOS DE REFERENCIA PARA GUARDARLOS EN FIREBASE PARA LAS RUTAS
            pathPointsRef = db.collection("pathPoints");

            //**************

            // Genera un ID único para la carrera
            raceId = UUID.randomUUID().toString();
            // Crea una referencia al nuevo documento para esta carrera
            raceRef = db.collection("pathPoints").document(raceId);

            // ****************************

            Intent intent = getIntent();
            activityType = intent.getStringExtra("tipoActividad");


            // Crea una lista de LatLngData para almacenar los pathPoints
            pathPointsData = new ArrayList<>();

            chronometer = findViewById(R.id.chronometer);

            pauseButton = findViewById(R.id.pauseButton);
            //boton pause y resume invisible al principio
            pauseButton.setVisibility(View.GONE);

            //Boton RESUME
            resumeButton = findViewById(R.id.resumeButton);
            resumeButton.setVisibility(View.GONE);
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
                    pauseLocationUpdates();
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
            //BOTON PARAR
            stopButton = findViewById(R.id.stopButton);
            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isTracking = false;
                    chronometer.stop();
                    //locationUpdates();
                    startButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.GONE);
                    resumeButton.setVisibility(View.GONE);

                    // Obtén el tiempo transcurrido
                    long timeElapsed = SystemClock.elapsedRealtime() - chronometer.getBase();

                    // Obtén la fecha y hora actual
                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int month = calendar.get(Calendar.MONTH) + 1; //Calendar.MONTH empieza los meses desde 0 hay que añadir uno
                    int year = calendar.get(Calendar.YEAR);
                    int startHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int startMinute = calendar.get(Calendar.MINUTE);

                    // Obtén la hora y minuto de finalización
                    int endHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int endMinute = calendar.get(Calendar.MINUTE);

                    // Crear un nuevo objeto CarreraData para almacenar los datos de la carrera
                    CarreraData carreraData = new CarreraData();
                    carreraData.setRaceId(raceId);
                    carreraData.setTimeElapsed(timeElapsed);
                    carreraData.setTotalDistance(totalDistance);
                    carreraData.setDay(day);
                    carreraData.setMonth(month);
                    carreraData.setYear(year);
                    carreraData.setStartHour(startHour);
                    carreraData.setStartMinute(startMinute);
                    carreraData.setEndHour(endHour);
                    carreraData.setEndMinute(endMinute);
                    carreraData.setActivityType(activityType);

                    // Crear un Intent para pasar los detalles de la carrera y las coordenadas GPS al otro Activity
                    Intent intent = new Intent(TrackingActivity.this, DetallesCarreraActivity.class);
                    intent.putExtra("carreraData", carreraData);
                    intent.putParcelableArrayListExtra("ruta_gps", (ArrayList<LatLng>) pathPoints);
                    startActivity(intent);
                }
            });


            distanceTextView = findViewById(R.id.distanceTextView); // TextView con id 'distanceTextView' en tu layout
            // define un LocationCallback que se utiliza para recibir actualizaciones de ubicación del FusedLocationProviderClient.
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null || !isTracking) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        // Si la ubicación no es nula y el seguimiento está activado, añade la ubicación a la lista de puntos del camino y actualiza el mapa.
                        if (location != null && isTracking) {
                            LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            pathPoints.add(newLocation);
                            polylineOptions.add(newLocation); // Añadir solo el nuevo punto a la polylineOptions
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

        // Obtiene la última ubicación conocida
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // En algunos casos raros, la ubicación devuelta puede ser nula
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                    }
                });
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

    private void pauseLocationUpdates() {
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


    //guardar todos los pathPoints en un solo documento, guarda cada LatLngData como un documento individual en una colección cuyo nombre es el raceId
 /*   private void locationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);

        // Guarda los pathPoints en Firebase
        for (LatLng point : pathPoints) {
            pathPointsData.add(new LatLngData(point.latitude, point.longitude));
        }

        // Obtén la fecha actual
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        // Crea un nuevo objeto para almacenar en el documento
        Map<String, Object> raceData = new HashMap<>();
        raceData.put("pathPoints", pathPointsData);
        raceData.put("day", day);
        raceData.put("month", month);
        raceData.put("year", year);

        // Guarda los pathPoints en el nuevo documento
        raceRef.set(raceData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot escrito correctamente!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error escribiendo el documento", e);
                    }
                });
    }*/


}