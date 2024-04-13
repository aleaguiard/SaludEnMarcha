package com.tfg.saludenmarcha;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Map;

public class CarreraHistorialActivity extends AppCompatActivity {
    // Declaración de variables para los TextViews y el botón
    private TextView tipoActividadText;
    private TextView distanciaTotalText;
    private TextView tiempoText;
    private TextView horaInicioText;
    private TextView horaFinText;
    private Button datePickerButton;
    private Button volverButton;
    private DocumentReference currentMealDocument;

    // Declaración de variables para Firestore
    private FirebaseFirestore db;
    private DocumentReference currentActivityDocument;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carrera_historial);

        // Inicialización de los TextViews y el botón
        tipoActividadText = findViewById(R.id.tipoActividadHistorial);
        distanciaTotalText = findViewById(R.id.totalDistanceHistorial);
        tiempoText = findViewById(R.id.timeElapsedHistorial);
        horaInicioText = findViewById(R.id.startTimeHistorial);
        horaFinText = findViewById(R.id.endTimeHistorial);
        datePickerButton = findViewById(R.id.date_picker_buttonHistorial);
        volverButton = findViewById(R.id.volverHistorialButton);

        // Inicialización de Firestore
        db = FirebaseFirestore.getInstance();

        // Establecimiento del OnClickListener para el botón de selección de fecha
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtención de la fecha actual
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // Creación y visualización de un nuevo DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(CarreraHistorialActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // El usuario ha seleccionado una fecha, por lo que se buscan las comidas de esa fecha en la base de datos
                                fetchActivitiesForDate(year, monthOfYear + 1, dayOfMonth);
                                //fetchMealsForDate(year, monthOfYear + 1, dayOfMonth);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        volverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CarreraHistorialActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Método para buscar las actividades de una fecha específica en la base de datos
    private void fetchActivitiesForDate(int year, int month, int day) {
        // Limpieza de los TextViews antes de realizar la búsqueda
        tipoActividadText.setText("Tipo de actividad: ");
        distanciaTotalText.setText("Distancia total: ");
        tiempoText.setText("Tiempo transcurrido: ");
        horaInicioText.setText("Hora de inicio: ");
        horaFinText.setText("Hora de finalización: ");

        // Búsqueda en la base de datos de las comidas de la fecha seleccionada y actualización de los TextViews con estos datos
        db.collection("actividades")
                .whereEqualTo("day", day)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        currentActivityDocument = document.getReference();
                        Map<String, Object> activity = document.getData();
                        tipoActividadText.setText("Tipo de actividad: " + activity.get("activityType"));
                        distanciaTotalText.setText("Distancia total: " + activity.get("totalDistance") + " km");
                        tiempoText.setText("Tiempo transcurrido: " + activity.get("timeElapsed") + " ms");
                     //   horaInicioText.setText("Hora de inicio: " + activity.get("startHour") + ":" + ((Number) activity.get("startMinute")).intValue());
                      //  horaFinText.setText("Hora de finalización: " + activity.get("endHour") + ":" + ((Number) activity.get("endMinute")).intValue());

                    } else {
                        Toast.makeText(CarreraHistorialActivity.this, "No hay datos de actividades para esta fecha", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CarreraHistorialActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    // Para el historial de comidas
    private void fetchMealsForDate(int year, int month, int day) {
        // Limpieza de los TextViews antes de realizar la búsqueda
        tipoActividadText.setText("Desayuno: ");
        distanciaTotalText.setText("Comida: ");
        tiempoText.setText("Cena: ");

        // Búsqueda en la base de datos de las comidas de la fecha seleccionada y actualización de los TextViews con estos datos
        db.collection("meals")
                .whereEqualTo("day", day)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        currentMealDocument = document.getReference();
                        Map<String, Object> meal = document.getData();
                        tipoActividadText.setText("Desayuno: " + meal.get("breakfast"));
                        distanciaTotalText.setText("Comida: " + meal.get("lunch"));
                        tiempoText.setText("Cena: " + meal.get("dinner"));
                    } else {
                        Toast.makeText(CarreraHistorialActivity.this, "No hay datos de comidas para esta fecha", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CarreraHistorialActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}