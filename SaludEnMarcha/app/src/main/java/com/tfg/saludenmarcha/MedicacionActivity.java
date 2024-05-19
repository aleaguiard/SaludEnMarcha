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
/**
 * MedicacionActivity es una actividad que permite al usuario registrar y gestionar sus datos de medicación diaria.
 * El usuario puede ingresar información sobre la medicación, la dosis, y seleccionar una fecha específica.
 * Los datos de la medicación se almacenan en Firebase Firestore.
 * También permite al usuario volver a la actividad principal.
 */
public class MedicacionActivity extends AppCompatActivity {
    // Variables para los componentes de la interfaz de usuario
    private EditText medicacionInput, dosisInput;
    private Button saveButton, datePickerButton, volverButton;
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
    }

    /**
     * Inicializa componentes de la interfaz de usuario.
     */
    private void initializeUI() {
        medicacionInput = findViewById(R.id.medicacion_text);  // Campo de texto para el nombre de la medicación
        dosisInput = findViewById(R.id.dosis_text);  // Campo de texto para la dosis
        saveButton = findViewById(R.id.buttonSaveMedicacion);  // Botón para guardar la medicación
        datePickerButton = findViewById(R.id.medicacion_picker_button);  // Botón para seleccionar la fecha
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
        obtenerIdMasAltoActividad();  // Obtener el ID más alto después de inicializar Firebase

    }

    /**
     * Establece listeners para los componentes de la interfaz.
     */
    private void setupListeners() {
        datePickerButton.setOnClickListener(this::openDatePicker);  // Listener para abrir el selector de fecha
        saveButton.setOnClickListener(this::saveMedication);  // Listener para guardar la medicación
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
                .addOnSuccessListener(docRef -> Toast.makeText(this, "Medicación guardada correctamente.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar la medicación.", Toast.LENGTH_LONG).show());
    }

    /**
     * Vuelve a la actividad principal.
     */
    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
    }

    /**
     * Obtiene el ID más alto registrado en la colección 'activities' y prepara el siguiente ID para una nueva entrada.
     * Este método asume que la colección 'activities' utiliza un campo llamado 'id' para almacenar un identificador numérico.
     */
    private void obtenerIdMasAltoActividad() {
        // Consulta a la colección 'activities' para encontrar el documento con el ID más alto.
        db.collection("medications")
                .whereEqualTo("idUser", idUser)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        CollectionReference activitiesRef = db.collection("medications");
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
