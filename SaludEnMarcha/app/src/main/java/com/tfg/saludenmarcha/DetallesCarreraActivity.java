package com.tfg.saludenmarcha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_carrera);
        volverButton = findViewById(R.id.volverDetallesButton);
        guardarButton = findViewById(R.id.GuardarDetallesButton);

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

                double totalDistance = carreraData.getTotalDistance();
                totalDistanceTextView.setText(String.format(Locale.getDefault(), "%.2f km", totalDistance));

                long timeElapsed = carreraData.getTimeElapsed();
                timeElapsedTextView.setText(String.format(Locale.getDefault(), "%d ms", timeElapsed));

                int startHour = carreraData.getStartHour();
                int startMinute = carreraData.getStartMinute();
                startTimeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute));

                int endHour = carreraData.getEndHour();
                int endMinute = carreraData.getEndMinute();
                endTimeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute));

                int day = carreraData.getDay();
                int month = carreraData.getMonth();
                int year = carreraData.getYear();
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
                // Inicializar Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Crear un nuevo objeto para almacenar en el documento
                Map<String, Object> raceData = new HashMap<>();
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
                db.collection("actividades").add(raceData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(DetallesCarreraActivity.this, "Actividad Guardada Correctamente", Toast.LENGTH_LONG).show();
                                // Cambiar a MenuActivity después de que los datos se hayan guardado con éxito
                               // Intent intent = new Intent(DetallesCarreraActivity.this, MenuActivity.class);
                              //  startActivity(intent);
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

/*
//BOTON PARAR
            stopButton = findViewById(R.id.stopButton);
            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isTracking = false;
                    chronometer.stop();
                    locationUpdates();
                    startButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.GONE);
                    resumeButton.setVisibility(View.GONE);

                    // Obtén el tiempo transcurrido
                    long timeElapsed = SystemClock.elapsedRealtime() - chronometer.getBase();

                    // Obtén la fecha y hora actual
                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int month = calendar.get(Calendar.MONTH);
                    int year = calendar.get(Calendar.YEAR);
                    int startHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int startMinute = calendar.get(Calendar.MINUTE);

                    // Obtén la hora y minuto de finalización
                    int endHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int endMinute = calendar.get(Calendar.MINUTE);

                    // Crea un nuevo objeto para almacenar en el documento
                    Map<String, Object> raceData = new HashMap<>();
                    raceData.put("timeElapsed", timeElapsed);
                    raceData.put("totalDistance", totalDistance); // Guarda la distancia total
                    raceData.put("day", day);
                    raceData.put("month", month);
                    raceData.put("year", year);
                    raceData.put("startHour", startHour);
                    raceData.put("startMinute", startMinute);
                    raceData.put("endHour", endHour); // Guarda la hora de finalización
                    raceData.put("endMinute", endMinute); // Guarda el minuto de finalización
                    raceData.put("activityType", activityType); // Guarda el tipo de actividad

                    // Guarda el tiempo y la distancia en el documento de la carrera
                    raceRef.update(raceData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot actualizado correctamente!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error actualizando el documento", e);
                                }
                            });

                    // Crea una referencia a la colección "actividades"
                    CollectionReference activitiesRef = db.collection("actividades");

                    // Crea un nuevo documento en la colección "actividades"
                    activitiesRef.add(raceData)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot añadido con ID: " + documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error añadiendo el documento", e);
                                }
                            });

                    Intent intent = new Intent(TrackingActivity.this, DetallesCarreraActivity.class);
                    intent.putExtra("raceId", raceId);
                    intent.putExtra("timeElapsed", timeElapsed);
                    intent.putExtra("totalDistance", totalDistance); // Guarda la distancia total
                    intent.putExtra("day", day);
                    intent.putExtra("month", month);
                    intent.putExtra("year", year);
                    intent.putExtra("startHour", startHour);
                    intent.putExtra("startMinute", startMinute);
                    intent.putExtra("endHour", endHour); // Guarda la hora de finalización
                    intent.putExtra("endMinute", endMinute); // Guarda el minuto de finalización
                    intent.putExtra("activityType", activityType); // Guarda el tipo de actividad
                    startActivity(intent);
                }
            });

 */

