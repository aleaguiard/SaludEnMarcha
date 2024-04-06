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

public class DetallesCarreraActivity extends AppCompatActivity {

    private String raceId; // ID de la carrera que quiero mostrar
    private String timeElapsed;
    private String totalDistance; // Guarda la distancia total
    private String day;
    private String month;
    private String year;
    private String startHour;
    private String startMinute;
    private String endHour; // Guarda la hora de finalización
    private String endMinute; // Guarda el minuto de finalización
    private String activityType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_carrera); // Establezco el layout de la actividad

        // Recupero los datos del Intent
        if(getIntent().hasExtra("raceId")) {
            raceId = getIntent().getStringExtra("raceId");
        }
        if(getIntent().hasExtra("timeElapsed")) {
            timeElapsed = getIntent().getStringExtra("timeElapsed");
        }
        if(getIntent().hasExtra("totalDistance")) {
            totalDistance = getIntent().getStringExtra("totalDistance"); // Guarda la distancia total
        }
        if(getIntent().hasExtra("day")) {
            day = getIntent().getStringExtra("day");
        }
        if(getIntent().hasExtra("month")) {
            month = getIntent().getStringExtra("month");
        }
        if(getIntent().hasExtra("year")) {
            year = getIntent().getStringExtra("year");
        }
        if(getIntent().hasExtra("startHour")) {
            startHour = getIntent().getStringExtra("startHour");
        }
        if(getIntent().hasExtra("startMinute")) {
            startMinute = getIntent().getStringExtra("startMinute");
        }
        if(getIntent().hasExtra("endHour")) {
            endHour = getIntent().getStringExtra("endHour"); // Guarda la hora de finalización
        }
        if(getIntent().hasExtra("endMinute")) {
            endMinute = getIntent().getStringExtra("endMinute"); // Guarda el minuto de finalización
        }
        if(getIntent().hasExtra("activityType")) {
            activityType = getIntent().getStringExtra("activityType"); // Guarda el tipo de actividad
}

        // Muestro los datos en la interfaz de usuario
        // Asegúrate de tener los TextViews correspondientes en tu layout
        ((TextView) findViewById(R.id.activityTypeTextView)).setText(activityType);
        ((TextView) findViewById(R.id.totalDistanceTextView)).setText(String.format(Locale.getDefault(), "%.2f km", Float.parseFloat(totalDistance)));
        ((TextView) findViewById(R.id.timeElapsedTextView)).setText(String.format(Locale.getDefault(), "%d ms", Long.parseLong(timeElapsed)));
        ((TextView) findViewById(R.id.startTimeTextView)).setText(String.format(Locale.getDefault(), "%02d:%02d", Integer.parseInt(startHour), Integer.parseInt(startMinute)));
        ((TextView) findViewById(R.id.endTimeTextView)).setText(String.format(Locale.getDefault(), "%02d:%02d", Integer.parseInt(endHour), Integer.parseInt(endMinute)));
        ((TextView) findViewById(R.id.dateTextView)).setText(String.format(Locale.getDefault(), "%02d/%02d/%d", Integer.parseInt(day), Integer.parseInt(month), Integer.parseInt(year)));
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

