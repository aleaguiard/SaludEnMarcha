package com.tfg.saludenmarcha;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class PesoActivity extends AppCompatActivity {
    // Variables de interfaz de usuario para entrada y botones
    private EditText pesoInput;
    private Button saveButton, datePickerButton, volverButton, botonHistorial;

    // Variables para manejo de Firebase Firestore y autenticación
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String idUser;

    // Variables para almacenar la fecha seleccionada
    private int selectedDay, selectedMonth, selectedYear;

    // Variable para almacenar el peso y el ID de la actividad
    private Long weight = null;
    private long idActividad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peso);
        EdgeToEdge.enable(this);

        initializeUI();
        initializeFirebase();
        setupListeners();

        //Pie pie = AnyChart.pie(); //grafico  circular
        // Cargar los datos de peso desde Firebase
        loadWeightData();
    }

    /**
     * Inicializa componentes de la interfaz de usuario.
     */
    private void initializeUI() {
        pesoInput = findViewById(R.id.peso_text);
        saveButton = findViewById(R.id.buttonSavePeso);
        datePickerButton = findViewById(R.id.peso_picker_button);
        botonHistorial = findViewById(R.id.peso_historial_button);
        volverButton = findViewById(R.id.volverPesoButton);

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
            //
            // obtenerIdMasAltoActividad();  // Llama a obtener el ID más alto al iniciar
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
        saveButton.setOnClickListener(this::saveWeight);
        botonHistorial.setOnClickListener(v -> navigateToHistorial());
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

        new DatePickerDialog(PesoActivity.this, (datePicker, y, m, d) -> {
            selectedDay = d;
            selectedMonth = m + 1;  // Ajustar por índice base 0
            selectedYear = y;
            Toast.makeText(this, "Fecha seleccionada: " + selectedDay + "/" + selectedMonth + "/" + selectedYear, Toast.LENGTH_SHORT).show();
        }, year, month, day).show();
    }

    /**
     * Guarda el peso en Firestore.
     */
    private void saveWeight(View view) {
        String text = pesoInput.getText().toString();

        if (text.isEmpty()) {
            Toast.makeText(this, "El campo de peso no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        long peso = 0;
        try {
            peso = Long.parseLong(text);
            if (peso < 1 || peso > 250) {
                Toast.makeText(this, "El peso debe estar entre 1 y 250.", Toast.LENGTH_SHORT).show();
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

        Map<String, Object> pesoData = new HashMap<>();
        pesoData.put("idUser", idUser);
        pesoData.put("weight", peso);
        pesoData.put("day", selectedDay);
        pesoData.put("month", selectedMonth);
        pesoData.put("year", selectedYear);
        pesoData.put("id", idActividad);

        db.collection("weights").add(pesoData)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Peso añadido correctamente.", Toast.LENGTH_SHORT).show();
                    // Actualizar el gráfico después de guardar los datos
                    loadWeightData();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar el peso.", Toast.LENGTH_LONG).show();
                });
    }


    /**
     * Navega a la actividad de historial.
     */
    //TODO: Implementar la navegación al historial de pesos
    private void navigateToHistorial() {
        startActivity(new Intent(this, AlimentacionHistorialActivity.class));
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
        db.collection("weights")
                .whereEqualTo("idUser", idUser)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        CollectionReference activitiesRef = db.collection("weights");
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
    private void loadWeightData() {
        if (idUser == null || idUser.isEmpty()) {
            Toast.makeText(this, "ID de usuario no encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("PesoActivity", "Cargando datos para el usuario: " + idUser);

        db.collection("weights")
                .whereEqualTo("idUser", idUser)
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(10)  // Limitar a los 10 documentos con los IDs más altos para el usuario específico
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        if (value != null) {
                            List<DataEntry> entries = new ArrayList<>();
                            for (QueryDocumentSnapshot document : value) {
                                Log.d("PesoActivity", "Documento recuperado: " + document.getData());

                                // Verifica si el documento tiene los campos 'weight', 'day', 'month', 'year'
                                if (document.contains("weight") && document.contains("day") && document.contains("month") && document.contains("year") && document.contains("id")) {
                                    Long weightNumber = document.getLong("weight");
                                    Long day = document.getLong("day");
                                    Long month = document.getLong("month");
                                    Long year = document.getLong("year");
                                    Long id = document.getLong("id");

                                    Log.d("PesoActivity", "ID: " + id + " Weight: " + weightNumber + " Date: " + day + "/" + month + "/" + year);

                                    if (weightNumber != null && day != null && month != null && year != null) {
                                        // Ensamblar la fecha en un formato legible
                                        String date = day + "/" + month + "/" + year;
                                        entries.add(new ValueDataEntry(date, weightNumber));
                                    }
                                } else {
                                    Log.d("PesoActivity", "Documento sin 'weight', 'day', 'month', 'year' o 'id': " + document.getId());
                                }
                            }
                            Log.d("PesoActivity", "Número de entradas: " + entries.size());
                            if (!entries.isEmpty()) {
                                // Asegurarse de actualizar el gráfico en el hilo principal
                                runOnUiThread(() -> updateChart(entries));
                            } else {
                                Toast.makeText(PesoActivity.this, "No se encontraron datos de peso.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }




    private void updateChart(List<DataEntry> entries) {
        AnyChartView anyChartView = findViewById(R.id.any_chart_peso);

        // Crear el gráfico de líneas
        Cartesian cartesian = AnyChart.line();

        // Establecer el título del gráfico
        cartesian.title("Evolución del Peso");

        // Crear la serie y agregar los datos
        Line series = cartesian.line(entries);
        series.name("Peso");

        // Configurar el gráfico con los datos
        anyChartView.setChart(cartesian);
        anyChartView.invalidate(); // Forzar la invalidación de la vista
    }




}
