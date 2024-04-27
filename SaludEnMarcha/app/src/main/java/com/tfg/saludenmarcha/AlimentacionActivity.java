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

    // Declaración de variables para los EditTexts y los botones
    private EditText breakfastInput;
    private EditText lunchInput;
    private EditText dinnerInput;
    private Button saveButton;
    private Button datePickerButton;
    private Button volverButton;

    private Button botonHistorial;

    // Declaración de variables para Firestore
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private int selectedDay; // Variable para guardar el día seleccionado
    private int selectedMonth; // Variable para guardar el mes seleccionado
    private int selectedYear; // Variable para guardar el año seleccionado
    String idUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alimentacion);

        // Configuración de la vista para adaptarse a los bordes de la pantalla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialización de los EditTexts y los botones
        breakfastInput = findViewById(R.id.breakfast_input);
        lunchInput = findViewById(R.id.lunch_input);
        dinnerInput = findViewById(R.id.dinner_input);
        saveButton = findViewById(R.id.save_button);
        datePickerButton = findViewById(R.id.date_picker_button);
        botonHistorial = findViewById(R.id.buttonHistorial);
        volverButton = findViewById(R.id.volverAlimentacionButton);

        // Inicialización de Firestore
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        idUser = mAuth.getCurrentUser().getUid();

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
                DatePickerDialog datePickerDialog = new DatePickerDialog(AlimentacionActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // Guardado del día, el mes y el año seleccionados en las variables
                                selectedDay = dayOfMonth;
                                selectedMonth = monthOfYear + 1; // Los meses empiezan en 0, por lo que se suma 1
                                selectedYear = year;
                                Toast.makeText(AlimentacionActivity.this, "Fecha seleccionada: " + selectedDay + "/" + selectedMonth + "/" + selectedYear, Toast.LENGTH_SHORT).show();
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        // Establecimiento del OnClickListener para el botón de guardar
        saveButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Verificación de que selectedDay, selectedMonth y selectedYear no estén vacíos
            if (selectedDay == 0 || selectedMonth == 0 || selectedYear == 0) {
                Toast.makeText(AlimentacionActivity.this, "Por favor, selecciona una fecha", Toast.LENGTH_SHORT).show();
                return;
            }

            // Guardado de la información introducida por el usuario
            String breakfast = breakfastInput.getText().toString();
            String lunch = lunchInput.getText().toString();
            String dinner = dinnerInput.getText().toString();

            // Creación de un nuevo documento en la colección "meals"
            Map<String, Object> meal = new HashMap<>();
            meal.put("idUser", idUser);
            meal.put("breakfast", breakfast);
            meal.put("lunch", lunch);
            meal.put("dinner", dinner);
            meal.put("day", selectedDay); // Guardado del día seleccionado en Firebase
            meal.put("month", selectedMonth); // Guardado del mes seleccionado en Firebase
            meal.put("year", selectedYear); // Guardado del año seleccionado en Firebase

            db.collection("meals").add(meal)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(AlimentacionActivity.this, "Alimentación añadida correctamente: " + documentReference.getId(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AlimentacionActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    });

        botonHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlimentacionActivity.this, AlimentacionHistorialActivity.class);
                startActivity(intent);
            }
        });

        volverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlimentacionActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}