package com.tfg.saludenmarcha;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Locale;
import java.util.Map;

public class DetallesCarreraActivity extends AppCompatActivity {
    private TextView activityTypeTextView;
    private TextView totalDistanceTextView;
    private TextView timeElapsedTextView;
    private TextView startTimeTextView;
    private TextView endTimeTextView;
    private TextView dateTextView;
    private Button volverButton;
    private Button guardarButton;
    CarreraData carreraData;
    private FirebaseAuth mAuth;

    //Variables para altas y consultas de firebase
    FirebaseFirestore db;
    String idUser;
    double totalDistance;
    long timeElapsed;
    int startHour;
    int startMinute;
    int endHour;
    int endMinute;
    int day;
    int month;
    int year;
    private GoogleMap mMap;

    ArrayList<LatLng> rutaGps;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_carrera);
        volverButton = findViewById(R.id.volverDetallesButton);
        guardarButton = findViewById(R.id.GuardarDetallesButton);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Inicializo variables para altas y consultas
        db = FirebaseFirestore.getInstance();
        idUser = mAuth.getCurrentUser().getUid();


        // Recuperar los datos del Intent
        if (getIntent().hasExtra("carreraData")) {
            carreraData = (CarreraData) getIntent().getSerializableExtra("carreraData");
            rutaGps = getIntent().getParcelableArrayListExtra("ruta_gps");

            // Mostrar los datos en la interfaz de usuario
            activityTypeTextView = findViewById(R.id.activityTypeTextView);
            totalDistanceTextView = findViewById(R.id.totalDistanceTextView);
            timeElapsedTextView = findViewById(R.id.timeElapsedTextView);
            startTimeTextView = findViewById(R.id.startTimeTextView);
            endTimeTextView = findViewById(R.id.endTimeTextView);
            dateTextView = findViewById(R.id.dateTextView);

            // Mostrar la ruta en el mapa
            // Verificar si la lista de coordenadas no es nula
            if (rutaGps != null) {
                // Dentro del método onCreate() de tu actividad
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
                final Activity activity = DetallesCarreraActivity.this; // Almacena una referencia a la actividad actual

                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        mMap = googleMap;
                        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mMap.setMyLocationEnabled(true); // Habilitar la capa de mi ubicación
                        mMap.getUiSettings().setZoomControlsEnabled(true); // Habilitar controles de zoom

                        // Agregar las polilíneas al mapa
                        PolylineOptions polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.RED);
                        polylineOptions.width(5);

                        for (LatLng point : rutaGps) {
                            polylineOptions.add(point);
                        }

                        mMap.addPolyline(polylineOptions);

                        // Centrar el mapa en la primera ubicación de la ruta
                        if (!rutaGps.isEmpty()) {
                            LatLng firstLocation = rutaGps.get(0);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 15));
                        }
                    }
                });

            } else {
                // Manejar el caso cuando no se encuentra la lista de coordenadas en el Intent
            }

            // Establecer el texto en los TextView
            if (carreraData != null) {
                activityTypeTextView.setText(carreraData.getActivityType());

                totalDistance = carreraData.getTotalDistance();
                totalDistanceTextView.setText(String.format(Locale.getDefault(), "%.2f km", totalDistance));

                timeElapsed = carreraData.getTimeElapsed();
                timeElapsedTextView.setText(String.format(Locale.getDefault(), "%d ms", timeElapsed));

                startHour = carreraData.getStartHour();
                startMinute = carreraData.getStartMinute();
                startTimeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute));

                endHour = carreraData.getEndHour();
                endMinute = carreraData.getEndMinute();
                endTimeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute));

                day = carreraData.getDay();
                month = carreraData.getMonth();
                year = carreraData.getYear();
                dateTextView.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", day, month, year));
            } else {
                // Manejar el caso cuando no se encuentra el objeto CarreraData en el Intent
            }
        }

        volverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetallesCarreraActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

       guardarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Crear un nuevo objeto para almacenar en el documento
                Map<String, Object> raceData = new HashMap<>();
                raceData.put("idUser", idUser);
                raceData.put("timeElapsed", carreraData.getTimeElapsed());
                raceData.put("totalDistance", carreraData.getTotalDistance()); // Guarda la distancia total
                raceData.put("day", carreraData.getDay());
                raceData.put("month", carreraData.getMonth());
                raceData.put("year", carreraData.getYear());
                raceData.put("startHour", carreraData.getStartHour());
                raceData.put("startMinute", carreraData.getStartMinute());
                raceData.put("endHour", carreraData.getEndHour()); // Guarda la hora de finalización
                raceData.put("endMinute", carreraData.getEndMinute()); // Guarda el minuto de finalización
                raceData.put("activityType", carreraData.getActivityType()); // Guarda el tipo de actividad

                // Guardar los datos en la colección "actividades"
                db.collection("activities").add(raceData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(DetallesCarreraActivity.this, "Actividad Guardada Correctamente", Toast.LENGTH_LONG).show();
                                //resetear los valores
                                activityTypeTextView.setText("");
                                totalDistanceTextView.setText("");
                                timeElapsedTextView.setText("");
                                startTimeTextView.setText("");
                                endTimeTextView.setText("");
                                dateTextView.setText("");
                                // Cambiar a MenuActivity después de que los datos se hayan guardado con éxito
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
        });
    }
}


