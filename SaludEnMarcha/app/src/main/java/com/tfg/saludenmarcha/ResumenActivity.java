package com.tfg.saludenmarcha;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ResumenActivity es una actividad que permite al usuario seleccionar una fecha y obtener un resumen
 * de los datos registrados en varias colecciones de Firestore para la fecha seleccionada.
 */
public class ResumenActivity extends AppCompatActivity {

    // Variables para los componentes de la interfaz de usuario
    private Button datePickerButton, volverButton;
    private TextView resultText;

    // Variables para manejo de Firebase Firestore y autenticación
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String idUser;

    // Variables para almacenar la fecha seleccionada
    private int selectedDay, selectedMonth, selectedYear;

    // Mapa para traducir nombres de colecciones
    private final Map<String, String> collectionNames = new HashMap<String, String>() {{
        put("activities", "Actividades");
        put("glucose", "Glucosa");
        put("meals", "Alimentación");
        put("medications", "Medicación");
        put("pressure-pulse", "Tensión y Pulso");
        put("weights", "Peso");
    }};

    // Lista para almacenar las fechas de las actividades
    private List<Calendar> activityDates = new ArrayList<>();

    // Clase interna para almacenar los detalles de los documentos
    private class DocumentDetails {
        QueryDocumentSnapshot document;
        String collectionName;

        DocumentDetails(QueryDocumentSnapshot document, String collectionName) {
            this.document = document;
            this.collectionName = collectionName;
        }
    }

    // Lista para almacenar detalles de los documentos
    private List<DocumentDetails> documentDetailsList = new ArrayList<>();
    private int pendingQueries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen);
        EdgeToEdge.enable(this);  // Habilita la visualización en pantalla completa

        // Inicialización de los componentes de la interfaz de usuario
        datePickerButton = findViewById(R.id.datepicker_button);
        resultText = findViewById(R.id.result_text);
        resultText.setVisibility(View.GONE);
        volverButton = findViewById(R.id.volverCalendarioButton);

        // Configuración de Firebase y autenticación
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            idUser = currentUser.getUid();
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configuración de los listeners para los botones
        datePickerButton.setOnClickListener(this::openDatePicker);
        volverButton.setOnClickListener(v -> finish());

        // Ajuste de la vista para adaptarse a los bordes de la pantalla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });

        // Cargar las fechas de las actividades
        loadActivityDates();
    }

    /**
     * Abre un diálogo de DatePicker para que el usuario seleccione una fecha.
     *
     * @param view La vista que activa el método.
     */
    private void openDatePicker(View view) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Selecciona una fecha");

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(new CalendarConstraints.DateValidator() {
            @Override
            public boolean isValid(long date) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(date);
                for (Calendar activityDate : activityDates) {
                    if (activityDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                            activityDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                            activityDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(android.os.Parcel dest, int flags) {
                // No need to write anything to the parcel
            }
        });

        builder.setCalendarConstraints(constraintsBuilder.build());

        final MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(selection);
            selectedDay = selectedDate.get(Calendar.DAY_OF_MONTH);
            selectedMonth = selectedDate.get(Calendar.MONTH) + 1;
            selectedYear = selectedDate.get(Calendar.YEAR);
            fetchDataForDate(selectedDay, selectedMonth, selectedYear);
        });

        picker.show(getSupportFragmentManager(), picker.toString());
    }

    /**
     * Consulta Firestore para obtener los datos correspondientes a la fecha seleccionada.
     *
     * @param day   Día seleccionado
     * @param month Mes seleccionado
     * @param year  Año seleccionado
     */
    private void fetchDataForDate(int day, int month, int year) {
        // Lista de colecciones a consultar en Firestore
        List<String> collections = new ArrayList<>();
        collections.add("activities");
        collections.add("glucose");
        collections.add("meals");
        collections.add("medications");
        collections.add("pressure-pulse");
        collections.add("weights");

        // StringBuilder para acumular los resultados de las consultas
        StringBuilder result = new StringBuilder("Datos para la fecha " + day + "/" + month + "/" + year + ":\n");
        documentDetailsList.clear();  // Limpiar la lista de detalles de documentos antes de realizar nuevas consultas
        pendingQueries = collections.size();  // Inicializar el contador de consultas pendientes

        // Iterar sobre cada colección y realizar la consulta correspondiente
        for (String collection : collections) {
            db.collection(collection)
                    .whereEqualTo("idUser", idUser)  // Filtrar por ID de usuario
                    .whereEqualTo("day", day)        // Filtrar por día seleccionado
                    .whereEqualTo("month", month)    // Filtrar por mes seleccionado
                    .whereEqualTo("year", year)      // Filtrar por año seleccionado
                    .get()  // Realizar la consulta
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Si la consulta fue exitosa y no es nula, obtener los resultados
                            QuerySnapshot value = task.getResult();
                            if (!value.isEmpty()) {
                                // Si hay documentos en los resultados, añadirlos a documentDetailsList
                                for (QueryDocumentSnapshot document : value) {
                                    documentDetailsList.add(new DocumentDetails(document, collectionNames.get(collection)));
                                }
                            }
                        } else {
                            // Si hubo un error en la consulta, agregar un mensaje indicando el error
                            result.append(collectionNames.get(collection)).append(": Error al obtener datos.\n");
                        }

                        pendingQueries--;  // Decrementar el contador de consultas pendientes
                        if (pendingQueries == 0) {
                            // Si todas las consultas han terminado, mostrar los resultados
                            if (documentDetailsList.isEmpty()) {
                                resultText.setVisibility(View.GONE);
                                Toast.makeText(this, "No hay datos disponibles para la fecha seleccionada.", Toast.LENGTH_SHORT).show();
                            } else {
                                resultText.setVisibility(View.VISIBLE);
                                displayResults(result);
                            }
                        }
                    });
        }
    }


    /**
     * Muestra los resultados en el TextView resultText.
     *
     * @param result StringBuilder que contiene los resultados acumulados
     */
    private void displayResults(StringBuilder result) {
    for (DocumentDetails details : documentDetailsList) {
        result.append("<br><u><b>Detalles de ").append(details.collectionName).append(":</b></u><br>");
        switch (details.collectionName) {
            case "Actividades":
                result.append("<b>Tipo de actividad:</b> ").append(details.document.getString("activityType")).append("<br>");

                Long timeElapsed = details.document.getLong("timeElapsed");
                if (timeElapsed != null) {
                    long hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(timeElapsed);
                    long minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(timeElapsed) % 60;
                    long seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(timeElapsed) % 60;
                    result.append("<b>Tiempo:</b> ").append(String.format("%02d:%02d:%02d", hours, minutes, seconds)).append("<br>");
                } else {
                    result.append("<b>Tiempo:</b> N/A<br>");
                }

                result.append("<b>Distancia total:</b> ").append(details.document.getDouble("totalDistance")).append("<br>");
                break;

            case "Glucosa":
                result.append("<b>Valor de glucosa:</b> ").append(details.document.getLong("glucose")).append("<br>");
                break;

            case "Tensión y Pulso":
                result.append("<b>Sistólica:</b> ").append(details.document.getLong("sistolica")).append("<br>")
                        .append("<b>Diastólica:</b> ").append(details.document.getLong("diastolica")).append("<br>")
                        .append("<b>Pulso:</b> ").append(details.document.getLong("pulse")).append("<br>");
                break;

            case "Medicación":
                result.append("<b>Nombre de la medicación:</b> ").append(details.document.getString("medicationName")).append("<br>")
                        .append("<b>Dosis:</b> ").append(details.document.getString("dose")).append("<br>");
                break;

            case "Alimentación":
                result.append("<b>Desayuno:</b> ").append(details.document.getString("breakfast")).append("<br>")
                        .append("<b>Comida:</b> ").append(details.document.getString("lunch")).append("<br>")
                        .append("<b>Cena:</b> ").append(details.document.getString("dinner")).append("<br>");
                break;

            case "Peso":
                result.append("<b>Peso:</b> ").append(details.document.getLong("weight")).append("<br>");
                break;
        }
    }

    runOnUiThread(() -> resultText.setText(Html.fromHtml(result.toString())));
}

    /**
     * Carga las fechas de las actividades desde todas las colecciones de Firestore.
     */
    private void loadActivityDates() {
        if (idUser == null || idUser.isEmpty()) {
            Toast.makeText(this, "ID de usuario no encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> collections = new ArrayList<>();
        collections.add("activities");
        collections.add("glucose");
        collections.add("meals");
        collections.add("medications");
        collections.add("pressure-pulse");
        collections.add("weights");

        pendingQueries = collections.size();

        for (String collection : collections) {
            db.collection(collection)
                    .whereEqualTo("idUser", idUser)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Toast.makeText(ResumenActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                return;
                            }

                            if (value != null) {
                                for (QueryDocumentSnapshot document : value) {
                                    Long day = document.getLong("day");
                                    Long month = document.getLong("month");
                                    Long year = document.getLong("year");

                                    if (day != null && month != null && year != null) {
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.set(year.intValue(), month.intValue() - 1, day.intValue());
                                        if (!activityDates.contains(calendar)) {
                                            activityDates.add(calendar);
                                        }
                                    }
                                }
                            }

                            pendingQueries--;
                            if (pendingQueries == 0) {
                                // All queries are done, you can update the DatePicker or UI here if needed
                            }
                        }
                    });
        }
    }
}
