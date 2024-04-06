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

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private String raceId; // El ID de la carrera que quieres mostrar y que hemos pasado del TrackingActivity

    private String actividadTipo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_actividad_terminada2);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        raceId = getIntent().getStringExtra("raceId").toString();
        actividadTipo = getIntent().getStringExtra("tipoActividad").toString();

        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("pathPoints").document(raceId);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Aquí puedes recuperar los puntos de ruta
                        List<Map<String, Object>> pathPointsData = (List<Map<String, Object>>) document.get("pathPoints");

                        // Convertir los datos en objetos LatLng
                        List<LatLng> pathPoints = new ArrayList<>();
                        for (Map<String, Object> data : pathPointsData) {
                            double lat = (double) data.get("latitude");
                            double lng = (double) data.get("longitude");
                            pathPoints.add(new LatLng(lat, lng));
                        }

                        // Crea un PolylineOptions y añade todos los puntos de ruta
                        PolylineOptions polylineOptions = new PolylineOptions().width(10).color(Color.RED);
                        for (LatLng point : pathPoints) {
                            polylineOptions.add(point);
                        }

                        // Añade el PolylineOptions al mapa
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
        mMap = googleMap;
    }
}