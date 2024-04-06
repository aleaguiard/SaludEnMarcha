package com.tfg.saludenmarcha;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class ActividadTerminadaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private String raceId; // El ID de la carrera que quieres mostrar y que hemos pasado del TrackingActivity
    DocumentReference raceRef;

    private String actividadTipo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_terminada);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        db = FirebaseFirestore.getInstance();

        actividadTipo = getIntent().getStringExtra("tipoActividad").toString();
        // Genera un ID único para la carrera

        raceRef = db.collection("pathPoints").document(raceId);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Recupera los pathPoints de SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("pathPoints", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("pathPoints", "");
        Type type = new TypeToken<List<LatLng>>() {}.getType();
        List<LatLng> pathPoints = gson.fromJson(json, type);

        // Crea un PolylineOptions y añade todos los puntos de ruta
        PolylineOptions polylineOptions = new PolylineOptions().width(10).color(Color.RED);
        for (LatLng point : pathPoints) {
            polylineOptions.add(point);
        }

        // Añade el PolylineOptions al mapa
        mMap.addPolyline(polylineOptions);
    }
}