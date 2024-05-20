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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


/**
 * AlimentacionHistorialActivity es una actividad que muestra el historial de alimentación del usuario.
 * El usuario puede seleccionar una fecha para ver las comidas registradas ese día.
 */
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

    // Variables para manejar la autenticación y la carga de datos de Firebase
    private String idUser;
    private List<Calendar> activityDates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alimentacion_historial);
        EdgeToEdge.enable(this);

        // Inicialización de los componentes de la interfaz de usuario
        initializeUI();
        initializeFirebase();
        setupListeners();

        // Cargar las fechas de las actividades
        loadActivityDates();
    }

    /**
     * Inicializa los componentes de la interfaz de usuario.
     */
    private void initializeUI() {
        breakfastText = findViewById(R.id.breakfast_text);
        lunchText = findViewById(R.id.lunch_text);
        dinnerText = findViewById(R.id.dinner_text);
        datePickerButton = findViewById(R.id.date_picker_button);
        volverButton = findViewById(R.id.volverHistorialAlimencacionButton);

        // Ajuste de la vista para adaptarse a los bordes de la pantalla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura Firebase y obtiene el ID del usuario actual.
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            idUser = currentUser.getUid();
        } else {
            Toast.makeText(this, "ID de usuario no encontrado.", Toast.LENGTH_SHORT).show();
        }
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Establece los listeners para los componentes de la interfaz de usuario.
     */
    private void setupListeners() {
        datePickerButton.setOnClickListener(this::openDatePicker);
        volverButton.setOnClickListener(v -> navigateToMain());

        // Establecer los OnClickListeners para los TextViews de las comidas
        breakfastText.setOnClickListener(v -> showEditDialog("breakfast"));
        lunchText.setOnClickListener(v -> showEditDialog("lunch"));
        dinnerText.setOnClickListener(v -> showEditDialog("dinner"));
    }

    /**
     * Abre el DatePicker para seleccionar una fecha.
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
            int selectedDay = selectedDate.get(Calendar.DAY_OF_MONTH);
            int selectedMonth = selectedDate.get(Calendar.MONTH) + 1;
            int selectedYear = selectedDate.get(Calendar.YEAR);
            // El usuario ha seleccionado una fecha, se buscan las comidas de esa fecha en la base de datos
            fetchMealsForDate(selectedYear, selectedMonth, selectedDay);
        });

        picker.show(getSupportFragmentManager(), picker.toString());
    }

    /**
     * Navega a la actividad principal.
     */
    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
    }

    /**
     * Busca las comidas de una fecha específica en Firestore.
     */
    private void fetchMealsForDate(int year, int month, int day) {
        // Limpieza de los TextViews antes de realizar la búsqueda
        clearMealDetails();

        db.collection("meals")
                .whereEqualTo("idUser", idUser)
                .whereEqualTo("day", day)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(AlimentacionHistorialActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (value != null && !value.isEmpty()) {
                            DocumentSnapshot document = value.getDocuments().get(0);
                            currentMealDocument = document.getReference();
                            Map<String, Object> meal = document.getData();
                            breakfastText.setText("Desayuno: " + meal.get("breakfast"));
                            lunchText.setText("Comida: " + meal.get("lunch"));
                            dinnerText.setText("Cena: " + meal.get("dinner"));
                        } else {
                            Toast.makeText(AlimentacionHistorialActivity.this, "No hay datos de comidas para esta fecha", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Limpia los detalles de las comidas mostrados en la interfaz de usuario.
     */
    private void clearMealDetails() {
        breakfastText.setText("Desayuno: ");
        lunchText.setText("Comida: ");
        dinnerText.setText("Cena: ");
    }

    /**
     * Muestra un cuadro de diálogo de edición para una comida específica.
     */
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

    /**
     * Actualiza una comida en Firestore.
     */
    private void updateMealInFirebase(String mealType, String newValue) {
        if (currentMealDocument != null) {
            currentMealDocument.update(mealType, newValue)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AlimentacionHistorialActivity.this, "Comida actualizada", Toast.LENGTH_SHORT).show();
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

    /**
     * Carga las fechas de las actividades desde Firestore.
     */
    private void loadActivityDates() {
        if (idUser == null || idUser.isEmpty()) {
            Toast.makeText(this, "ID de usuario no encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("meals")
                .whereEqualTo("idUser", idUser)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(AlimentacionHistorialActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (value != null) {
                            activityDates.clear();
                            for (QueryDocumentSnapshot document : value) {
                                Long day = document.getLong("day");
                                Long month = document.getLong("month");
                                Long year = document.getLong("year");

                                if (day != null && month != null && year != null) {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.set(year.intValue(), month.intValue() - 1, day.intValue());
                                    activityDates.add(calendar);
                                }
                            }
                        }
                    }
                });
    }
}
