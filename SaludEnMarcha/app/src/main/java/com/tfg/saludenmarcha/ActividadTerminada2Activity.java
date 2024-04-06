package com.tfg.saludenmarcha;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActividadTerminada2Activity extends AppCompatActivity implements OnMapReadyCallback {

    // Declaración de variables para GoogleMap, Firestore y el ID de la carrera
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private String raceId; // El ID de la carrera que quieres mostrar y que hemos pasado del TrackingActivity

    // Variable para guardar el tipo de actividad
    private String actividadTipo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_terminada2);

        // Configuración de la vista para adaptarse a los bordes de la pantalla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialización del fragmento del mapa y configuración para que se cargue de forma asíncrona
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Recuperación del ID de la carrera y el tipo de actividad del Intent
        raceId = getIntent().getStringExtra("raceId").toString();
        actividadTipo = getIntent().getStringExtra("tipoActividad").toString();

        // Inicialización de Firestore y obtención de la referencia del documento en Firestore
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("pathPoints").document(raceId);

        // Recuperación de los puntos de ruta de Firestore
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Recuperación de los puntos de ruta
                        List<Map<String, Object>> pathPointsData = (List<Map<String, Object>>) document.get("pathPoints");

                        // Conversión de los datos en objetos LatLng
                        List<LatLng> pathPoints = new ArrayList<>();
                        for (Map<String, Object> data : pathPointsData) {
                            double lat = (double) data.get("latitude");
                            double lng = (double) data.get("longitude");
                            pathPoints.add(new LatLng(lat, lng));
                        }

                        // Creación de un PolylineOptions y adición de todos los puntos de ruta
                        PolylineOptions polylineOptions = new PolylineOptions().width(10).color(Color.RED);
                        for (LatLng point : pathPoints) {
                            polylineOptions.add(point);
                        }

                        // Adición del PolylineOptions al mapa
                        mMap.addPolyline(polylineOptions);
                    } else {
                        Log.d("ActividadTerminada2", "No such document");
                    }
                } else {
                    Log.d("ActividadTerminada2", "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Inicialización del mapa cuando está listo
        mMap = googleMap;
    }
}