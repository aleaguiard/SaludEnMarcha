package com.tfg.saludenmarcha;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Map;

public class AlimentacionHistorialActivity extends AppCompatActivity {

    // Declaración de variables para los TextViews y el botón
    private TextView breakfastText;
    private TextView lunchText;
    private TextView dinnerText;
    private Button datePickerButton;
    private Button volverButton;

    // Declaración de variables para Firestore
    private FirebaseFirestore db;
    private DocumentReference currentMealDocument;
    private FirebaseAuth mAuth;

    //Variables para altas y consultas de firebase
    String idUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alimentacion_historial);

        // Inicialización de los TextViews y el botón
        breakfastText = findViewById(R.id.breakfast_text);
        lunchText = findViewById(R.id.lunch_text);
        dinnerText = findViewById(R.id.dinner_text);
        datePickerButton = findViewById(R.id.date_picker_button);
        volverButton = findViewById(R.id.volverHistorialAlimencacionButton);
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(AlimentacionHistorialActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // El usuario ha seleccionado una fecha, por lo que se buscan las comidas de esa fecha en la base de datos
                                fetchMealsForDate(year, monthOfYear + 1, dayOfMonth);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        volverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlimentacionHistorialActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Establecimiento de los OnClickListener para los TextViews de las comidas
        breakfastText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog("breakfast");
            }
        });

        lunchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog("lunch");
            }
        });

        dinnerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog("dinner");
            }
        });
    }

    // Método para buscar las comidas de una fecha específica en la base de datos
    private void fetchMealsForDate(int year, int month, int day) {
        // Limpieza de los TextViews antes de realizar la búsqueda
        breakfastText.setText("Desayuno: ");
        lunchText.setText("Comida: ");
        dinnerText.setText("Cena: ");

        // Búsqueda en la base de datos de las comidas de la fecha seleccionada y actualización de los TextViews con estos datos
        db.collection("meals")
                .whereEqualTo("idUser", idUser)
                .whereEqualTo("day", day)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        currentMealDocument = document.getReference();
                        Map<String, Object> meal = document.getData();
                        breakfastText.setText("Desayuno: " + meal.get("breakfast"));
                        lunchText.setText("Comida: " + meal.get("lunch"));
                        dinnerText.setText("Cena: " + meal.get("dinner"));
                    } else {
                        Toast.makeText(AlimentacionHistorialActivity.this, "No hay datos de comidas para esta fecha", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AlimentacionHistorialActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Método para mostrar un cuadro de diálogo de edición cuando se hace clic en un TextView de comida
    private void showEditDialog(String mealType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar " + mealType);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        String currentText = "";
        switch (mealType) {
            case "breakfast":
                currentText = breakfastText.getText().toString().replace("Desayuno: ", "");
                break;
            case "lunch":
                currentText = lunchText.getText().toString().replace("Comida: ", "");
                break;
            case "dinner":
                currentText = dinnerText.getText().toString().replace("Cena: ", "");
                break;
        }
        input.setText(currentText);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newValue = input.getText().toString();
                updateMealInFirebase(mealType, newValue);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Método para actualizar una comida en Firebase
    private void updateMealInFirebase(String mealType, String newValue) {
        if (currentMealDocument != null) {
            currentMealDocument.update(mealType, newValue)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AlimentacionHistorialActivity.this, "Comida actualizada", Toast.LENGTH_SHORT).show();
                            // Actualización del TextView con el nuevo valor
                            switch (mealType) {
                                case "breakfast":
                                    breakfastText.setText("Desayuno: " + newValue);
                                    break;
                                case "lunch":
                                    lunchText.setText("Comida: " + newValue);
                                    break;
                                case "dinner":
                                    dinnerText.setText("Cena: " + newValue);
                                    break;
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AlimentacionHistorialActivity.this, "Error al actualizar la comida: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }

    }
}