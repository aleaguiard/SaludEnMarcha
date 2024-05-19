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
/**
 * TensionActivity es una actividad que permite al usuario registrar y gestionar sus datos de tensión arterial y pulso.
 * El usuario puede ingresar su presión sistólica, presión diastólica y pulso, seleccionar una fecha específica y guardar los datos en Firebase Firestore.
 * También permite al usuario ver un historial de sus registros y regresar a la actividad principal.
 */
public class TensionActivity extends AppCompatActivity {
    // Variables de interfaz de usuario para los campos de entrada y los botones.
    private EditText sistolicaInput, diastolicaInput, pulsoInput;
    private Button saveButton, datePickerButton, historialButton, volverButton;

    // Variables para la autenticación de Firebase y la base de datos Firestore.
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String idUser;
    private AnyChartView anyChartView;
    private boolean isChartVisible = false;

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
        loadTensionData();  // Cargar los datos de tensión desde Firebase
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
        historialButton = findViewById(R.id.graficaTensionButton);
        volverButton = findViewById(R.id.volverTensionButton);
        anyChartView = findViewById(R.id.any_chart_tension);
        anyChartView.setVisibility(View.GONE);

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
        historialButton.setOnClickListener(v -> showGraphic());
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
        String pulsoStr = pulsoInput.getText().toString();

        // Verifica que los campos no estén vacíos
        if (sistolicaStr.isEmpty() || diastolicaStr.isEmpty() || pulsoStr.isEmpty()) {
            Toast.makeText(this, "Todos los campos deben estar llenos.", Toast.LENGTH_SHORT).show();
            return;
        }

        int sistolica, diastolica, pulso;
        try {
            // Convertir los valores String a int
            sistolica = Integer.parseInt(sistolicaStr);
            diastolica = Integer.parseInt(diastolicaStr);
            pulso = Integer.parseInt(pulsoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa números válidos.", Toast.LENGTH_SHORT).show();
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
        if (pulso < 40 || pulso > 180) {
            Toast.makeText(this, "El pulso debe estar entre 40 y 180.", Toast.LENGTH_LONG).show();
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
        data.put("pulse", pulso);
        data.put("day", selectedDay);
        data.put("month", selectedMonth);
        data.put("year", selectedYear);
        data.put("id", idActividad);

        // Guardar los datos en Firestore
        db.collection("pressure-pulse").add(data)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Datos de tensión guardados correctamente.", Toast.LENGTH_SHORT).show();
                    sistolicaInput.setText("");
                    diastolicaInput.setText("");
                    pulsoInput.setText("");
                    loadTensionData();
                    startActivity(new Intent(this, TensionActivity.class));
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar los datos.", Toast.LENGTH_LONG).show());
    }

    /**
     * Muestra u oculta la gráfica de tensión.
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
     * Obtiene el ID más alto registrado en la colección 'pressure-pulse' y prepara el siguiente ID para una nueva entrada.
     */
    private void obtenerIdMasAltoActividad() {
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
     * Carga los datos de tensión desde Firestore para el usuario actual.
     * Ordena los documentos por el campo 'id' en orden descendente y limita a los 20 documentos más recientes.
     * Cada entrada se agrega a una lista de datos que se utiliza para actualizar el gráfico.
     */
    private void loadTensionData() {
        if (idUser == null || idUser.isEmpty()) {
            Toast.makeText(this, "ID de usuario no encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("TensionActivity", "Cargando datos para el usuario: " + idUser);

        db.collection("pressure-pulse")
                .whereEqualTo("idUser", idUser)
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        if (value != null) {
                            List<DataEntry> diastolicaEntries = new ArrayList<>();
                            List<DataEntry> sistolicaEntries = new ArrayList<>();
                            List<DataEntry> pulseEntries = new ArrayList<>();
                            for (QueryDocumentSnapshot document : value) {
                                Log.d("TensionActivity", "Documento recuperado: " + document.getData());

                                if (document.contains("sistolica") && document.contains("diastolica") && document.contains("pulse") && document.contains("day") && document.contains("month") && document.contains("year") && document.contains("id")) {
                                    Long sistolica = document.getLong("sistolica");
                                    Long diastolica = document.getLong("diastolica");
                                    Long pulse = document.getLong("pulse");
                                    Long day = document.getLong("day");
                                    Long month = document.getLong("month");
                                    Long year = document.getLong("year");
                                    Long id = document.getLong("id");

                                    Log.d("TensionActivity", "ID: " + id + " Sistolica: " + sistolica + " Diastolica: " + diastolica + " Pulse: " + pulse + " Fecha: " + day + "/" + month + "/" + year);

                                    if (sistolica != null && diastolica != null && pulse != null && day != null && month != null && year != null) {
                                        String date = day + "/" + month + "/" + year + " " + id;
                                        diastolicaEntries.add(new ValueDataEntry(date, diastolica));
                                        sistolicaEntries.add(new ValueDataEntry(date, sistolica));
                                        pulseEntries.add(new ValueDataEntry(date, pulse));
                                    }
                                } else {
                                    Log.d("TensionActivity", "Documento sin 'sistolica', 'diastolica', 'pulse', 'day', 'month', 'year' o 'id': " + document.getId());
                                }
                            }
                            Log.d("TensionActivity", "Número de entradas: " + diastolicaEntries.size());
                            if (!diastolicaEntries.isEmpty() && !sistolicaEntries.isEmpty() && !pulseEntries.isEmpty()) {
                                runOnUiThread(() -> updateChart(diastolicaEntries, sistolicaEntries, pulseEntries));
                            } else {
                                Toast.makeText(TensionActivity.this, "No se encontraron datos de tensión.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /**
     * Actualiza el gráfico de líneas con los datos proporcionados.
     *
     * @param diastolicaEntries Lista de entradas de datos para la línea de presión diastólica.
     * @param sistolicaEntries Lista de entradas de datos para la línea de presión sistólica.
     * @param pulseEntries Lista de entradas de datos para la línea de pulso.
     */
    private void updateChart(List<DataEntry> diastolicaEntries, List<DataEntry> sistolicaEntries, List<DataEntry> pulseEntries) {
        AnyChartView anyChartView = findViewById(R.id.any_chart_tension);

        Cartesian cartesian = AnyChart.line();

        cartesian.title("Evolución de la Tensión Arterial y Pulso");
        // Crear las series de líneas y agregar los datos a cada serie.
        Line sistolicaSeries = cartesian.line(sistolicaEntries);
        sistolicaSeries.name("Sistólica");

        Line diastolicaSeries = cartesian.line(diastolicaEntries);
        diastolicaSeries.name("Diastólica");


        Line pulseSeries = cartesian.line(pulseEntries);
        pulseSeries.name("Pulso");

        // Configurar el gráfico con los datos de las series.
        anyChartView.setChart(cartesian);

        // Forzar la invalidación de la vista para asegurar que se actualice visualmente.
        anyChartView.invalidate();
    }
}
