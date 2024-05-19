package com.tfg.saludenmarcha;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * CalendarioActivity es una actividad que permite al usuario seleccionar una fecha y ver los registros
 * correspondientes de varias colecciones de Firebase Firestore para esa fecha.
 * Las colecciones incluidas son: actividades, glucosa, alimentación, medicación, tensión y pulso, y peso.
 */
public class CalendarioActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_calendario);
        EdgeToEdge.enable(this);  // Habilita la visualización en pantalla completa

        // Inicialización de los componentes de la interfaz de usuario
        datePickerButton = findViewById(R.id.datepicker_button);
        resultText = findViewById(R.id.result_text);
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
    }

    /**
     * Abre un diálogo de DatePicker para que el usuario seleccione una fecha.
     *
     * @param view La vista que activa el método.
     */
    private void openDatePicker(View view) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (datePicker, y, m, d) -> {
            selectedDay = d;
            selectedMonth = m + 1;  // Meses comienzan en 0, por eso se añade 1
            selectedYear = y;
            fetchDataForDate(selectedDay, selectedMonth, selectedYear);
        }, year, month, day).show();
    }

    /**
     * Consulta Firestore para obtener los datos correspondientes a la fecha seleccionada.
     *
     * @param day Día seleccionado
     * @param month Mes seleccionado
     * @param year Año seleccionado
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
                            } else {
                                // Si no hay documentos, agregar un mensaje indicando que no hay registros
                                result.append(collectionNames.get(collection)).append(": No hay registros.\n");
                            }
                        } else {
                            // Si hubo un error en la consulta, agregar un mensaje indicando el error
                            result.append(collectionNames.get(collection)).append(": Error al obtener datos.\n");
                        }

                        pendingQueries--;  // Decrementar el contador de consultas pendientes
                        if (pendingQueries == 0) {
                            // Si todas las consultas han terminado, mostrar los resultados
                            displayResults(result);
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
            result.append("\nDetalles de ").append(details.collectionName).append(":\n");
            switch (details.collectionName) {
                case "Actividades":
                    result.append("Tipo de actividad: ").append(details.document.getString("activityType")).append("\n");

                    Long timeElapsed = details.document.getLong("timeElapsed");
                    if (timeElapsed != null) {
                        long hours = TimeUnit.MILLISECONDS.toHours(timeElapsed);
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeElapsed) % 60;
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeElapsed) % 60;
                        result.append("Tiempo: ").append(String.format("%02d:%02d:%02d", hours, minutes, seconds)).append("\n");
                    } else {
                        result.append("Tiempo: N/A\n");
                    }

                    result.append("Distancia total: ").append(details.document.getDouble("totalDistance")).append("\n");
                    break;

                case "Glucosa":
                    result.append("Valor de glucosa: ").append(details.document.getLong("glucose")).append("\n");
                    break;

                case "Tensión y Pulso":
                    result.append("Sistólica: ").append(details.document.getLong("sistolica")).append("\n")
                            .append("Diastólica: ").append(details.document.getLong("diastolica")).append("\n")
                            .append("Pulso: ").append(details.document.getLong("pulse")).append("\n");
                    break;

                case "Medicación":
                    result.append("Nombre de la medicación: ").append(details.document.getString("medicationName")).append("\n")
                            .append("Dosis: ").append(details.document.getString("dose")).append("\n");
                    break;

                case "Alimentación":
                    result.append("Desayuno: ").append(details.document.getString("breakfast")).append("\n")
                            .append("Comida: ").append(details.document.getString("lunch")).append("\n")
                            .append("Cena: ").append(details.document.getString("dinner")).append("\n");
                    break;

                case "Peso":
                    result.append("Peso: ").append(details.document.getLong("weight")).append("\n");
                    break;
            }
        }

        runOnUiThread(() -> resultText.setText(result.toString()));
    }

}
