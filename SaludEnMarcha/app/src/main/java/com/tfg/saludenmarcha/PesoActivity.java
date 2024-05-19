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
    private Button saveButton, datePickerButton, volverButton, graficaPesoButton;

    // Variables para manejo de Firebase Firestore y autenticación
    private FirebaseFirestore db;
    private AnyChartView anyChartView;
    private FirebaseAuth mAuth;
    private String idUser;

    // Variables para almacenar la fecha seleccionada
    private int selectedDay, selectedMonth, selectedYear;

    // Variable para almacenar el peso y el ID de la actividad
    private Long weight = null;
    private long idActividad;
    private boolean isChartVisible = false;

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
        graficaPesoButton = findViewById(R.id.graficaPesoButton);
        volverButton = findViewById(R.id.volverPesoButton);
        anyChartView = findViewById(R.id.any_chart_peso);
        anyChartView.setVisibility(View.GONE);

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
        graficaPesoButton.setOnClickListener(v -> showGraphic());
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
                    pesoInput.setText("");
                    loadWeightData();
                    // Esperar 3 segundos antes de actualizar el gráfico, para permitir que los datos se guarden en Firestore
                    //new Handler().postDelayed(this::loadWeightData, 19000);
                    startActivity(new Intent(this, PesoActivity.class));

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar el peso.", Toast.LENGTH_LONG).show();
                });
    }


    /**
     * Muestra u oculta la gráfica de peso.
     */
    private void showGraphic() {
        if (isChartVisible) {
            anyChartView.setVisibility(View.GONE);
            isChartVisible = false;
        } else {
            anyChartView.setVisibility(View.VISIBLE);
            isChartVisible = true;
        }
    }

    /**
     * Vuelve a la actividad principal.
     */
    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
    }

    /**
     * Obtiene el ID más alto registrado en la colección 'weights' y prepara el siguiente ID para una nueva entrada.
     * Este método asume que la colección 'weights' utiliza un campo llamado 'id' para almacenar un identificador numérico.
     */
    private void obtenerIdMasAltoActividad() {
        // Consulta a la colección 'weights' para encontrar el documento con el ID más alto para el usuario actual.
        db.collection("weights")
                .whereEqualTo("idUser", idUser)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        // Verificar si hay un error al escuchar los cambios en la base de datos.
                        if (e != null) {
                            return;
                        }

                        // Crear una referencia a la colección 'weights'.
                        CollectionReference activitiesRef = db.collection("weights");

                        // Crear una consulta para obtener el documento con el ID más alto, ordenando de forma descendente y limitando a 1 resultado.
                        Query query = activitiesRef.orderBy("id", Query.Direction.DESCENDING).limit(1);

                        // Ejecutar la consulta y manejar el resultado.
                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Verificar si se encontraron documentos.
                                    if (!task.getResult().isEmpty()) {
                                        idActividad = 0;
                                        // Iterar sobre los documentos recuperados (en este caso solo uno).
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            // Verificar si el documento contiene el campo 'id'.
                                            if (document.contains("id")) {
                                                // Obtener el valor del campo 'id' y preparar el próximo ID.
                                                long highestId = document.getLong("id");
                                                if (highestId > Integer.MAX_VALUE) {
                                                    System.out.println("El ID excede el máximo valor para un int");
                                                } else {
                                                    idActividad = highestId + 1;
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

    /**
     * Carga los datos de peso desde Firestore para el usuario actual.
     * Ordena los documentos por el campo 'id' en orden descendente y limita a los 20 documentos más recientes.
     * Cada entrada se agrega a una lista de datos que se utiliza para actualizar el gráfico.
     */
    private void loadWeightData() {
        // Verificar si el ID de usuario está disponible.
        if (idUser == null || idUser.isEmpty()) {
            Toast.makeText(this, "ID de usuario no encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("PesoActivity", "Cargando datos para el usuario: " + idUser);

        // Consulta a la colección 'weights' para obtener los datos de peso del usuario actual, ordenados por 'id' en orden descendente.
        db.collection("weights")
                .whereEqualTo("idUser", idUser)
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(20)  // Limitar a los 20 documentos con los IDs más altos para el usuario específico.
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        // Verificar si hay un error al escuchar los cambios en la base de datos.
                        if (e != null) {
                            return;
                        }

                        // Si hay resultados, procesar los documentos recuperados.
                        if (value != null) {
                            List<DataEntry> entries = new ArrayList<>();
                            // Iterar sobre los documentos recuperados.
                            for (QueryDocumentSnapshot document : value) {
                                Log.d("PesoActivity", "Documento recuperado: " + document.getData());

                                // Verificar si el documento tiene los campos 'weight', 'day', 'month', 'year', y 'id'.
                                if (document.contains("weight") && document.contains("day") && document.contains("month") && document.contains("year") && document.contains("id")) {
                                    Long weightNumber = document.getLong("weight");
                                    Long day = document.getLong("day");
                                    Long month = document.getLong("month");
                                    Long year = document.getLong("year");
                                    Long id = document.getLong("id");

                                    Log.d("PesoActivity", "ID: " + id + " Weight: " + weightNumber + " Date: " + day + "/" + month + "/" + year);

                                    if (weightNumber != null && day != null && month != null && year != null) {
                                        // Ensamblar la fecha en un formato legible y diferenciar entre pesos del mismo día usando 'id'.
                                        String date = day + "/" + month + "/" + year + " " + id;
                                        entries.add(new ValueDataEntry(date, weightNumber));
                                    }
                                } else {
                                    Log.d("PesoActivity", "Documento sin 'weight', 'day', 'month', 'year' o 'id': " + document.getId());
                                }
                            }
                            Log.d("PesoActivity", "Número de entradas: " + entries.size());
                            if (!entries.isEmpty()) {
                                // Asegurarse de actualizar el gráfico en el hilo principal.
                                runOnUiThread(() -> updateChart(entries));
                            } else {
                                Toast.makeText(PesoActivity.this, "No se encontraron datos de peso.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }


    /**
     * Actualiza el gráfico de líneas con los datos proporcionados.
     *
     * @param entries Lista de entradas de datos que se utilizarán para actualizar el gráfico.
     */
    private void updateChart(List<DataEntry> entries) {
        // Obtener la vista del gráfico desde el layout.
        AnyChartView anyChartView = findViewById(R.id.any_chart_peso);

        // Crear una instancia del gráfico de líneas.
        Cartesian cartesian = AnyChart.line();

        // Establecer el título del gráfico.
        cartesian.title("Evolución del Peso");

        // Crear una serie de líneas y agregar los datos a la serie.
        Line series = cartesian.line(entries);
        series.name("Peso");

        // Configurar el gráfico con los datos de la serie.
        anyChartView.setChart(cartesian);

        // Forzar la invalidación de la vista para asegurar que se actualice visualmente.
        anyChartView.invalidate();
    }


}
