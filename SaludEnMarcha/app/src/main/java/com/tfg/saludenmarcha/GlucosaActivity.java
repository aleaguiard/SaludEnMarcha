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
public class GlucosaActivity extends AppCompatActivity {
    // Variables de interfaz de usuario para entrada y botones
    private EditText glucosaInput;
    private Button saveButton, datePickerButton, volverButton, botonHistorial;

    // Variables para manejo de Firebase Firestore y autenticación
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String idUser;

    // Variables para almacenar la fecha seleccionada
    private int selectedDay, selectedMonth, selectedYear;

    // Variable para almacenar la glucosa y el ID de la actividad
    private Long glucoseLevel = null;
    private long idActividad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glucosa);
        EdgeToEdge.enable(this);

        initializeUI();
        initializeFirebase();
        setupListeners();
    }

    /**
     * Inicializa componentes de la interfaz de usuario.
     */
    private void initializeUI() {
        glucosaInput = findViewById(R.id.glucosa_text);
        saveButton = findViewById(R.id.buttonSaveGlucosa);
        datePickerButton = findViewById(R.id.glucosa_picker_button);
        volverButton = findViewById(R.id.volverGlucosaButton);

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
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            idUser = currentUser.getUid();
        } else {
            // Redirigir al usuario a la pantalla de login o mostrar un mensaje adecuado
        }
        db = FirebaseFirestore.getInstance();
        obtenerIdMasAltoActividad();
    }

    /**
     * Establece listeners para los componentes de la interfaz.
     */
    private void setupListeners() {
        datePickerButton.setOnClickListener(this::openDatePicker);
        saveButton.setOnClickListener(this::saveGlucoseLevel);
        volverButton.setOnClickListener(v -> navigateToMain());
    }

    /**
     * Abre el DatePicker para seleccionar una fecha.
     */
    private void openDatePicker(View view) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(GlucosaActivity.this, (datePicker, y, m, d) -> {
            selectedDay = d;
            selectedMonth = m + 1;  // Ajustar por índice base 0
            selectedYear = y;
            Toast.makeText(this, "Fecha seleccionada: " + selectedDay + "/" + selectedMonth + "/" + selectedYear, Toast.LENGTH_SHORT).show();
        }, year, month, day).show();
    }

    /**
     * Guarda el nivel de glucosa en Firestore.
     */
    private void saveGlucoseLevel(View view) {
        String text = glucosaInput.getText().toString();

        if (text.isEmpty()) {
            Toast.makeText(this, "El campo de glucosa no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        long glucose = 0;
        try {
            glucose = Long.parseLong(text);
            if (glucose < 1 || glucose > 400) {
                Toast.makeText(this, "El nivel de glucosa debe estar entre 1 y 400.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa un número válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDay == 0 || selectedMonth == 0 || selectedYear == 0) {
            Toast.makeText(this, "Por favor, selecciona una fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> glucosaData = new HashMap<>();
        glucosaData.put("idUser", idUser);
        glucosaData.put("glucose", glucose);
        glucosaData.put("day", selectedDay);
        glucosaData.put("month", selectedMonth);
        glucosaData.put("year", selectedYear);
        glucosaData.put("id", idActividad);

        db.collection("glucose").add(glucosaData)
                .addOnSuccessListener(docRef -> Toast.makeText(this, "Nivel de glucosa añadido correctamente.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar el nivel de glucosa.", Toast.LENGTH_LONG).show());
    }


    /**
     * Vuelve a la actividad principal.
     */
    private void navigateToMain() {

        startActivity(new Intent(this, MainActivity.class));
    }


    /**
     * Obtiene el ID más alto registrado en la colección 'glucose' y prepara el siguiente ID para una nueva entrada.
     */
    private void obtenerIdMasAltoActividad() {
        // Consulta a la colección 'activities' para encontrar el documento con el ID más alto.
        db.collection("glucose")
                .whereEqualTo("idUser", idUser)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        CollectionReference activitiesRef = db.collection("glucose");
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
