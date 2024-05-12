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

public class TensionActivity extends AppCompatActivity {
    // Variables de interfaz de usuario para los campos de entrada y los botones.
    private EditText tensionInput, pulsoInput,sistolicaInput, diastolicaInput;
    private Button saveButton, datePickerButton, historialButton, volverButton;

    // Variables para la autenticación de Firebase y la base de datos Firestore.
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String idUser;

    // Variables para almacenar la fecha seleccionada por el usuario.
    private int selectedDay, selectedMonth, selectedYear;
    private long idActividad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tension);
        EdgeToEdge.enable(this);

        // Métodos para inicializar la interfaz de usuario y configurar Firebase.
        initializeUI();
        initializeFirebase();
        setupListeners();
    }

    /**
     * Inicializa los componentes de la interfaz de usuario y ajusta los insets para la visualización correcta.
     */
    private void initializeUI() {
        // Asigna cada vista a su variable correspondiente.
        sistolicaInput = findViewById(R.id.tension_sistolica_text);
        diastolicaInput = findViewById(R.id.tension_diastolica_text);
        pulsoInput = findViewById(R.id.pulso_text);
        saveButton = findViewById(R.id.buttonSaveTension);
        datePickerButton = findViewById(R.id.tension_picker_button);
        volverButton = findViewById(R.id.volverTensionButton);

        // Ajusta la vista para adaptarse a los bordes de la pantalla.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura Firebase y obtiene la instancia actual del usuario.
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        idUser = (currentUser != null) ? currentUser.getUid() : null;
        db = FirebaseFirestore.getInstance();
        obtenerIdMasAltoActividad();
    }

    /**
     * Configura los listeners para los botones y el selector de fecha.
     */
    private void setupListeners() {
        datePickerButton.setOnClickListener(this::openDatePicker);
        saveButton.setOnClickListener(this::saveTension);
        volverButton.setOnClickListener(v -> finish());
    }

    /**
     * Abre un diálogo de DatePicker para que el usuario seleccione una fecha.
     */
    private void openDatePicker(View view) {
        final Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(TensionActivity.this, (datePicker, year, month, day) -> {
            selectedDay = day;
            selectedMonth = month + 1;  // Meses en Calendar están basados en 0.
            selectedYear = year;
            Toast.makeText(this, "Fecha seleccionada: " + selectedDay + "/" + selectedMonth + "/" + selectedYear, Toast.LENGTH_SHORT).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Guarda los datos de tensión y pulso en Firestore.
     */
    private void saveTension(View view) {
        // Obtener los valores de los EditText como String
        String sistolicaStr = sistolicaInput.getText().toString();
        String diastolicaStr = diastolicaInput.getText().toString();

        // Verifica que los campos no estén vacíos
        if (sistolicaStr.isEmpty() || diastolicaStr.isEmpty()) {
            Toast.makeText(this, "Los campos de tensión no pueden estar vacíos.", Toast.LENGTH_SHORT).show();
            return;
        }

        int sistolica = 0, diastolica = 0;
        try {
            // Convertir los valores String a int
            sistolica = Integer.parseInt(sistolicaStr);
            diastolica = Integer.parseInt(diastolicaStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa números válidos para la tensión.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar los rangos de presión sistólica y diastólica con valores realistas
        if (sistolica < 90 || sistolica > 180) {
            Toast.makeText(this, "La presión sistólica debe estar entre 90 y 180 mmHg.", Toast.LENGTH_LONG).show();
            return;
        }
        if (diastolica < 60 || diastolica > 120) {
            Toast.makeText(this, "La presión diastólica debe estar entre 60 y 120 mmHg.", Toast.LENGTH_LONG).show();
            return;
        }

        // Verifica que la fecha haya sido seleccionada
        if (selectedDay == 0 || selectedMonth == 0 || selectedYear == 0) {
            Toast.makeText(this, "Por favor, selecciona una fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        // Preparar los datos para guardar en Firestore
        Map<String, Object> data = new HashMap<>();
        data.put("idUser", idUser);
        data.put("sistolica", sistolica); // Firebase Firestore espera estos como números
        data.put("diastolica", diastolica);
        data.put("day", selectedDay);
        data.put("month", selectedMonth);
        data.put("year", selectedYear);
        data.put("id", idActividad);

        // Guardar los datos en Firestore
        db.collection("pressure-pulse").add(data)
                .addOnSuccessListener(docRef -> Toast.makeText(this, "Datos de tensión guardados correctamente.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar los datos.", Toast.LENGTH_LONG).show());
    }

    /**
     * Obtiene el ID más alto registrado en la colección 'activities' y prepara el siguiente ID para una nueva entrada.
     * Este método asume que la colección 'activities' utiliza un campo llamado 'id' para almacenar un identificador numérico.
     */
    private void obtenerIdMasAltoActividad() {
        // Consulta a la colección 'activities' para encontrar el documento con el ID más alto.
        db.collection("pressure-pulse")
                .whereEqualTo("idUser", idUser)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        CollectionReference activitiesRef = db.collection("pressure-pulse");
                        Query query = activitiesRef.orderBy("id", Query.Direction.DESCENDING).limit(1);
                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        idActividad = 0;
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if (document.contains("id")) {
                                                long highestId = document.getLong("id");
                                                if (highestId > Integer.MAX_VALUE) {
                                                    System.out.println("El ID excede el máximo valor para un int");
                                                } else {
                                                    idActividad = (int) highestId + 1;
                                                    System.out.println("El ID de la próxima actividad será: " + idActividad);
                                                }
                                            } else {
                                                System.out.println("Documento no contiene el campo 'id'.");
                                            }
                                        }
                                    } else {
                                        System.out.println("No se encontraron documentos.");
                                    }
                                } else {
                                    System.out.println("Error obteniendo documentos: " + task.getException());
                                }
                            }
                        });
                    }
                });
    }

}
