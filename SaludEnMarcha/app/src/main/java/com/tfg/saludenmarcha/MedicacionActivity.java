package com.tfg.saludenmarcha;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
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

/**
 * MedicacionActivity es una actividad que permite al usuario registrar y gestionar sus datos de medicación diaria.
 * El usuario puede ingresar información sobre la medicación, la dosis, y seleccionar una fecha específica.
 * Los datos de la medicación se almacenan en Firebase Firestore.
 * También permite al usuario volver a la actividad principal.
 */
public class MedicacionActivity extends AppCompatActivity {
    // Variables para los componentes de la interfaz de usuario
    private EditText medicacionInput, dosisInput;
    private Button saveButton, datePickerButton, graficaButton, volverButton;
    private int selectedDay, selectedMonth, selectedYear;

    // Variables para manejo de Firebase Firestore y autenticación
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String idUser;
    private long idActividad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicacion);
        EdgeToEdge.enable(this);  // Habilita la visualización en pantalla completa

        initializeUI();  // Inicializa los componentes de la interfaz
        initializeFirebase();  // Configura Firebase y obtiene usuario
        setupListeners();  // Configura los listeners de los botones y otros eventos
        obtenerIdMasAltoActividad();  // Obtener el ID más alto al iniciar la actividad
    }

    /**
     * Inicializa componentes de la interfaz de usuario.
     */
    private void initializeUI() {
        medicacionInput = findViewById(R.id.medicacion_text);  // Campo de texto para el nombre de la medicación
        dosisInput = findViewById(R.id.dosis_text);  // Campo de texto para la dosis
        saveButton = findViewById(R.id.buttonSaveMedicacion);  // Botón para guardar la medicación
        datePickerButton = findViewById(R.id.medicacion_picker_button);  // Botón para seleccionar la fecha
        graficaButton = findViewById(R.id.graficaMedicacionButton);  // Botón para mostrar la gráfica
        volverButton = findViewById(R.id.volverMedicacionButton);  // Botón para volver a la actividad principal

        // Ajuste de la vista para adaptarse a los bordes de la pantalla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura Firebase y la autenticación.
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();  // Instancia de FirebaseAuth
        FirebaseUser currentUser = mAuth.getCurrentUser();  // Obtener el usuario actual
        if (currentUser != null) {
            idUser = currentUser.getUid();  // Si hay usuario, obtener su UID
        } else {
            // Aquí podrías redirigir al usuario al login si no está logueado
        }
        db = FirebaseFirestore.getInstance();  // Instancia de FirebaseFirestore
    }

    /**
     * Establece listeners para los componentes de la interfaz.
     */
    private void setupListeners() {
        datePickerButton.setOnClickListener(this::openDatePicker);  // Listener para abrir el selector de fecha
        saveButton.setOnClickListener(this::saveMedication);  // Listener para guardar la medicación
        graficaButton.setOnClickListener(v -> showRecentMedications());  // Listener para mostrar la gráfica
        volverButton.setOnClickListener(v -> navigateToMain());  // Listener para volver a la actividad principal
    }

    /**
     * Abre el DatePicker para seleccionar una fecha.
     */
    private void openDatePicker(View view) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);  // Año actual
        int month = c.get(Calendar.MONTH);  // Mes actual
        int day = c.get(Calendar.DAY_OF_MONTH);  // Día actual

        new DatePickerDialog(this, (datePicker, y, m, d) -> {
            selectedDay = d;
            selectedMonth = m + 1;  // Meses comienzan en 0, por eso se añade 1
            selectedYear = y;
        }, year, month, day).show();  // Muestra el diálogo para seleccionar la fecha
    }

    /**
     * Guarda la medicacion en Firestore.
     */
    private void saveMedication(View view) {
        String medicationName = medicacionInput.getText().toString().trim();  // Nombre de la medicación
        String dose = dosisInput.getText().toString().trim();  // Dosis

        if (medicationName.isEmpty() || dose.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDay == 0 || selectedMonth == 0 || selectedYear == 0) {
            Toast.makeText(this, "Por favor, selecciona una fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();  // Map para almacenar los datos
        data.put("idUser", idUser);
        data.put("medicationName", medicationName);
        data.put("dose", dose);
        data.put("day", selectedDay);
        data.put("month", selectedMonth);
        data.put("year", selectedYear);
        data.put("id", idActividad);

        db.collection("medications").add(data)  // Añade los datos a la colección 'medications'
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Medicación guardada correctamente.", Toast.LENGTH_SHORT).show();
                    medicacionInput.setText("");
                    dosisInput.setText("");
                    obtenerIdMasAltoActividad();  // Obtener el ID más alto después de guardar
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar la medicación.", Toast.LENGTH_LONG).show());
    }

    /**
     * Vuelve a la actividad principal.
     */
    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
    }

    /**
     * Obtiene el ID más alto registrado en la colección 'medications' y prepara el siguiente ID para una nueva entrada.
     */
    private void obtenerIdMasAltoActividad() {
        db.collection("medications")
                .whereEqualTo("idUser", idUser)
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.contains("id")) {
                                    long highestId = document.getLong("id");
                                    if (highestId > Integer.MAX_VALUE) {
                                        System.out.println("El ID excede el máximo valor para un int");
                                    } else {
                                        idActividad = highestId + 1;
                                        System.out.println("El ID de la próxima actividad será: " + idActividad);
                                    }
                                }
                            }
                        } else {
                            idActividad = 1;
                            System.out.println("No se encontraron documentos.");
                        }
                    }
                });
    }

    /**
     * Muestra las 5 últimas medicaciones en un AlertDialog.
     */
    private void showRecentMedications() {
        if (idUser == null || idUser.isEmpty()) {
            Toast.makeText(this, "ID de usuario no encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("MedicacionActivity", "Cargando datos para el usuario: " + idUser);

        db.collection("medications")
                .whereEqualTo("idUser", idUser)
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        if (value != null) {
                            List<String> medications = new ArrayList<>();
                            for (QueryDocumentSnapshot document : value) {
                                Log.d("MedicacionActivity", "Documento recuperado: " + document.getData());

                                if (document.contains("medicationName") && document.contains("dose") && document.contains("day") && document.contains("month") && document.contains("year") && document.contains("id")) {
                                    int day = document.getLong("day").intValue();
                                    int month = document.getLong("month").intValue();
                                    int year = document.getLong("year").intValue();
                                    String medicationName = document.getString("medicationName");
                                    String dose = document.getString("dose");

                                    Log.d("MedicacionActivity", "ID: " + document.getLong("id") + " Medicación: " + medicationName + " Dosis: " + dose + " Fecha: " + day + "/" + month + "/" + year);

                                    medications.add(day + "/" + month + "/" + year + " - " + medicationName + " - " + dose);
                                } else {
                                    Log.d("MedicacionActivity", "Documento sin 'medicationName', 'dose', 'day', 'month', 'year' o 'id': " + document.getId());
                                }
                            }
                            Log.d("MedicacionActivity", "Número de entradas: " + medications.size());
                            if (!medications.isEmpty()) {
                                runOnUiThread(() -> showMedicationsDialog(medications));
                            } else {
                                Toast.makeText(MedicacionActivity.this, "No se encontraron datos de medicación.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /**
     * Muestra un AlertDialog con la lista de medicaciones.
     */
    private void showMedicationsDialog(List<String> medications) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Últimas 5 Medicaciones")
                .setItems(medications.toArray(new String[0]), null)
                .setPositiveButton("OK", null)
                .show();
    }
}
