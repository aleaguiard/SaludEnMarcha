package com.tfg.saludenmarcha;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AlimentacionActivity extends AppCompatActivity {

    // Variables de la interfaz de usuario para entrada de datos y botones
    private EditText breakfastInput, lunchInput, dinnerInput;
    private Button saveButton, datePickerButton, botonHistorial, volverButton;

    // Variables para manejar la autenticación y operaciones de base de datos con Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String idUser;

    // Variables para almacenar la fecha seleccionada por el usuario
    private int selectedDay, selectedMonth, selectedYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alimentacion);
        EdgeToEdge.enable(this);

        // Inicialización de componentes de la interfaz de usuario
        initUI();

        // Configuración de las instancias de Firebase para autenticación y base de datos
        initFirebase();

        // Configuración de los listeners para los botones
        setupListeners();
    }

    /**
     * Inicializa las vistas de la interfaz de usuario.
     */
    private void initUI() {
        // Obtención de referencias a los componentes de la interfaz de usuario desde el layout
        breakfastInput = findViewById(R.id.breakfast_input);
        lunchInput = findViewById(R.id.lunch_input);
        dinnerInput = findViewById(R.id.dinner_input);
        saveButton = findViewById(R.id.save_button);
        datePickerButton = findViewById(R.id.date_picker_button);
        botonHistorial = findViewById(R.id.buttonHistorial);
        volverButton = findViewById(R.id.volverAlimentacionButton);

        // Ajuste de la vista principal para adaptarse a los bordes de la pantalla, considerando los insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura las instancias de Firebase Auth y Firestore.
     */
    private void initFirebase() {
        // Inicialización de FirebaseAuth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        idUser = mAuth.getCurrentUser().getUid();
    }

    /**
     * Establece listeners para los botones de la interfaz de usuario.
     */
    private void setupListeners() {
        // Listener para el botón de selección de fecha que abre un DatePickerDialog
        datePickerButton.setOnClickListener(v -> openDatePicker());

        // Listener para el botón de guardar que guarda los datos de las comidas en Firestore
        saveButton.setOnClickListener(v -> saveMealData());

        // Listener para el botón de historial que abre la actividad de historial de alimentación
        botonHistorial.setOnClickListener(v -> startActivity(new Intent(AlimentacionActivity.this, AlimentacionHistorialActivity.class)));

        // Listener para el botón de volver que regresa a la actividad principal
        volverButton.setOnClickListener(v -> startActivity(new Intent(AlimentacionActivity.this, MainActivity.class)));
    }

    /**
     * Abre un DatePickerDialog para seleccionar la fecha.
     */
    private void openDatePicker() {
        // Configuración del DatePickerDialog con la fecha actual como predeterminada
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            selectedDay = dayOfMonth;
            selectedMonth = monthOfYear + 1;  // Ajuste porque Calendar.MONTH es 0-indexed
            selectedYear = year;
            Toast.makeText(this, "Fecha seleccionada: " + selectedDay + "/" + selectedMonth + "/" + selectedYear, Toast.LENGTH_SHORT).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    /**
     * Guarda los datos de las comidas ingresados en Firestore.
     */
    private void saveMealData() {
        // Verificación de que la fecha esté correctamente seleccionada
        if (selectedDay == 0 || selectedMonth == 0 || selectedYear == 0) {
            Toast.makeText(this, "Por favor, selecciona una fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recolección y preparación de los datos para su almacenamiento
        String breakfast = breakfastInput.getText().toString();
        String lunch = lunchInput.getText().toString();
        String dinner = dinnerInput.getText().toString();

        Map<String, Object> meal = new HashMap<>();
        meal.put("idUser", idUser);
        meal.put("breakfast", breakfast);
        meal.put("lunch", lunch);
        meal.put("dinner", dinner);
        meal.put("day", selectedDay);
        meal.put("month", selectedMonth);
        meal.put("year", selectedYear);

        // Adición del documento a la colección de Firestore
        db.collection("meals").add(meal)
                .addOnSuccessListener(documentReference -> Toast.makeText(this, "Alimentación añadida correctamente: ", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar la alimentación", Toast.LENGTH_LONG).show());
    }
}
