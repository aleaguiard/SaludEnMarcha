package com.tfg.saludenmarcha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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


            // Mostrar los datos en la interfaz de usuario
            activityTypeTextView = findViewById(R.id.activityTypeTextView);
            totalDistanceTextView = findViewById(R.id.totalDistanceTextView);
            timeElapsedTextView = findViewById(R.id.timeElapsedTextView);
            startTimeTextView = findViewById(R.id.startTimeTextView);
            endTimeTextView = findViewById(R.id.endTimeTextView);
            dateTextView = findViewById(R.id.dateTextView);

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
                Intent intent = new Intent(DetallesCarreraActivity.this, MenuActivity.class);
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
                                activityTypeTextView.setText("0");
                                totalDistanceTextView.setText("0");
                                timeElapsedTextView.setText("0");
                                startTimeTextView.setText("0");
                                endTimeTextView.setText("0");
                                dateTextView.setText("0");
                                // Cambiar a MenuActivity después de que los datos se hayan guardado con éxito
                                Intent intent = new Intent(DetallesCarreraActivity.this, MenuActivity.class);
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


