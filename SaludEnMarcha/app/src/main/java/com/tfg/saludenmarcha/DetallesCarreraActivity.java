package com.tfg.saludenmarcha;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * DetallesCarreraActivity es una actividad que muestra los detalles de una carrera específica.
 * También permite guardar los detalles de la carrera en la base de datos de Firebase Firestore.
 */
public class DetallesCarreraActivity extends AppCompatActivity {
    // Variables de la interfaz de usuario
    private TextView activityTypeTextView;
    private TextView totalDistanceTextView;
    private TextView timeElapsedTextView;
    private TextView startTimeTextView;
    private TextView endTimeTextView;
    private TextView dateTextView;
    private Button volverButton;
    private Button guardarButton;

    // Variables para almacenar los datos de la carrera
    private CarreraData carreraData;
    private ArrayList<LatLng> rutaGps;

    // Variables para la autenticación y la base de datos de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseFirestore miBaseDatos;
    private String idUser;

    // Variables para almacenar los detalles de la carrera
    private double totalDistance;
    private long timeElapsed;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private int day;
    private int month;
    private int year;

    // Variables para el mapa de Google
    private GoogleMap mMap;

    // Variables para el ID de la actividad
    private long idActividad;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_carrera);

        // Inicializar las variables de la interfaz de usuario
        initUI();

        // Inicializar Firebase Auth y Firestore
        initFirebase();

        // Obtener el ID más alto de la colección 'activities'
        obtenerIdMasAltoActividad();

        // Recuperar los datos de la carrera del Intent
        recuperarDatosCarrera();

        // Configurar los botones
        configurarBotones();
    }

    /**
     * Inicializa las variables de la interfaz de usuario.
     */
    private void initUI() {
        activityTypeTextView = findViewById(R.id.activityTypeTextView);
        totalDistanceTextView = findViewById(R.id.totalDistanceTextView);
        timeElapsedTextView = findViewById(R.id.timeElapsedTextView);
        startTimeTextView = findViewById(R.id.startTimeTextView);
        endTimeTextView = findViewById(R.id.endTimeTextView);
        dateTextView = findViewById(R.id.dateTextView);
        volverButton = findViewById(R.id.volverDetallesButton);
        guardarButton = findViewById(R.id.GuardarDetallesButton);
    }

    /**
     * Inicializa Firebase Auth y Firestore.
     */
    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        miBaseDatos = FirebaseFirestore.getInstance();
        idUser = mAuth.getCurrentUser().getUid();
    }

    /**
     * Recupera los datos de la carrera del Intent.
     */
    private void recuperarDatosCarrera() {
        if (getIntent().hasExtra("carreraData")) {
            carreraData = (CarreraData) getIntent().getSerializableExtra("carreraData");
            rutaGps = getIntent().getParcelableArrayListExtra("ruta_gps");

            // Mostrar los datos en la interfaz de usuario
            mostrarDatosCarrera();

            // Mostrar la ruta en el mapa
            mostrarRutaMapa();
        }
    }

    /**
     * Muestra los datos de la carrera en la interfaz de usuario.
     */
    private void mostrarDatosCarrera() {
        if (carreraData != null) {
            activityTypeTextView.setText(carreraData.getActivityType());

            totalDistance = carreraData.getTotalDistance();
            totalDistanceTextView.setText(String.format(Locale.getDefault(), "Distancia: %.2f km", totalDistance));

            //timeElapsed = carreraData.getTimeElapsed();
            //timeElapsedTextView.setText(String.format(Locale.getDefault(), "%d ms", timeElapsed));
            // Obtiene el tiempo en milisegundos
            long timeElapsed = carreraData.getTimeElapsed();

            // Calcula horas, minutos y segundos
            int hours = (int) (timeElapsed / 3600000);
            int minutes = (int) ((timeElapsed % 3600000) / 60000);
            int seconds = (int) ((timeElapsed % 60000) / 1000);

            // Actualiza el TextView con el tiempo formateado
            // Formatear y mostrar el tiempo transcurrido
            timeElapsedTextView.setText(String.format(Locale.getDefault(), "Tiempo transcurrido: %02d:%02d:%02d", hours, minutes, seconds));

            // Obtener y mostrar la hora de inicio
            startHour = carreraData.getStartHour();
            startMinute = carreraData.getStartMinute();
            startTimeTextView.setText(String.format(Locale.getDefault(), "Hora de inicio: %02d:%02d", startHour, startMinute));

            // Obtener y mostrar la hora de finalización
            endHour = carreraData.getEndHour();
            endMinute = carreraData.getEndMinute();
            endTimeTextView.setText(String.format(Locale.getDefault(), "Hora de finalización: %02d:%02d", endHour, endMinute));

            // Obtener y mostrar la fecha
            day = carreraData.getDay();
            month = carreraData.getMonth();
            year = carreraData.getYear();
            dateTextView.setText(String.format(Locale.getDefault(), "Fecha: %02d/%02d/%d", day, month, year));

        }
    }

    /**
     * Muestra la ruta de la carrera en el mapa.
     */
    private void mostrarRutaMapa() {
        if (rutaGps != null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
            final Activity activity = DetallesCarreraActivity.this;

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setZoomControlsEnabled(true);

                    agregarPolilineasMapa();

                    if (!rutaGps.isEmpty()) {
                        LatLng firstLocation = rutaGps.get(0);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 15));
                    }
                }
            });
        }
    }

    /**
     * Agrega las polilíneas al mapa.
     */
    private void agregarPolilineasMapa() {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(5);

        for (LatLng point : rutaGps) {
            polylineOptions.add(point);
        }

        mMap.addPolyline(polylineOptions);
    }

    /**
     * Configura los botones de la interfaz de usuario.
     */
    private void configurarBotones() {
        volverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetallesCarreraActivity.this, ActividadMenuActivity.class);
                startActivity(intent);
            }
        });

        guardarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarDatosCarrera();
            }
        });
    }

    /**
     * Guarda los datos de la carrera en la base de datos de Firebase Firestore.
     */
    private void guardarDatosCarrera() {
        Map<String, Object> raceData = new HashMap<>();
        raceData.put("idUser", idUser);
        raceData.put("id", idActividad);
        raceData.put("timeElapsed", carreraData.getTimeElapsed());
        raceData.put("totalDistance", carreraData.getTotalDistance());
        raceData.put("day", carreraData.getDay());
        raceData.put("month", carreraData.getMonth());
        raceData.put("year", carreraData.getYear());
        raceData.put("startHour", carreraData.getStartHour());
        raceData.put("startMinute", carreraData.getStartMinute());
        raceData.put("endHour", carreraData.getEndHour());
        raceData.put("endMinute", carreraData.getEndMinute());
        raceData.put("activityType", carreraData.getActivityType());

        ArrayList<GeoPoint> geoPoints = new ArrayList<>();
        if (rutaGps != null) {
            for (LatLng latLng : rutaGps) {
                GeoPoint geoPoint = new GeoPoint(latLng.latitude, latLng.longitude);
                geoPoints.add(geoPoint);
            }
        }
        raceData.put("routeGps", geoPoints);

        db.collection("activities").add(raceData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(DetallesCarreraActivity.this, "Actividad Guardada Correctamente", Toast.LENGTH_LONG).show();
                        resetearValores();
                        Intent intent = new Intent(DetallesCarreraActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DetallesCarreraActivity.this, "Error al guardar la actividad", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Resetea los valores de la interfaz de usuario.
     */
    private void resetearValores() {
        activityTypeTextView.setText("");
        totalDistanceTextView.setText("");
        timeElapsedTextView.setText("");
        startTimeTextView.setText("");
        endTimeTextView.setText("");
        dateTextView.setText("");
    }

    /**
     * Obtiene el ID más alto de la colección 'activities' y asigna el siguiente ID a la nueva actividad.
     * Solo para el usuario que está logeado.
     */
    public void obtenerIdMasAltoActividad() {
        miBaseDatos.collection("Tareas")
                .whereEqualTo("idUser", idUser)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        CollectionReference activitiesRef = db.collection("activities");
                        Query query = activitiesRef.orderBy("id", Query.Direction.DESCENDING).limit(1);
                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        idActividad = 0;
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if (document.contains("id")) {
                                                long highestId = document.getLong("id");
                                                if (highestId > Integer.MAX_VALUE) {
                                                    System.out.println("El ID excede el máximo valor para un int");
                                                } else {
                                                    idActividad = (int) highestId + 1;
                                                    System.out.println("El ID de la próxima actividad será: " + idActividad);
                                                }
                                            } else {
                                                System.out.println("Documento no contiene el campo 'id'.");
                                            }
                                        }
                                    } else {
                                        System.out.println("No se encontraron documentos.");
                                    }
                                } else {
                                    System.out.println("Error obteniendo documentos: " + task.getException());
                                }
                            }
                        });
                    }
                });
    }
}