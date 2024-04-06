package com.tfg.saludenmarcha;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class DetallesCarreraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_carrera);

        // Recuperar los datos del Intent
        if (getIntent().hasExtra("carreraData")) {
            CarreraData carreraData = (CarreraData) getIntent().getSerializableExtra("carreraData");

            // Mostrar los datos en la interfaz de usuario
            TextView activityTypeTextView = findViewById(R.id.activityTypeTextView);
            TextView totalDistanceTextView = findViewById(R.id.totalDistanceTextView);
            TextView timeElapsedTextView = findViewById(R.id.timeElapsedTextView);
            TextView startTimeTextView = findViewById(R.id.startTimeTextView);
            TextView endTimeTextView = findViewById(R.id.endTimeTextView);
            TextView dateTextView = findViewById(R.id.dateTextView);

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

