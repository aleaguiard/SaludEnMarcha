package com.tfg.saludenmarcha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.anychart.chart.common.dataentry.DataEntry;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Align;
import com.anychart.enums.Anchor;
import com.anychart.enums.LegendLayout;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.text.HAlign;
import com.anychart.graphics.vector.text.VAlign;
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
 * GraficasActivity es una actividad que muestra gráficos de AnyChartView para los últimos 10 registros
 * de las colecciones de glucosa, presión y pulso, y peso en Firebase Firestore.
 */
public class GraficasActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_graficas);
        EdgeToEdge.enable(this);

        // Inicialización de los componentes de la interfaz de usuario
        anyChartView = findViewById(R.id.any_chart_vertical);
        volverButton = findViewById(R.id.volverResumenButton);

        // Configuración de Firebase y autenticación
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            idUser = currentUser.getUid();
        } else {
            // Redirigir al usuario al login si no está autenticado
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        // Configuración de los listeners para los botones
        volverButton.setOnClickListener(v -> finish());

        // Cargar datos para cada gráfico
        loadGlucoseData();
        loadTensionData();
        loadWeightData();
    }

    /**
     * Carga los datos de glucosa desde Firestore para el usuario actual.
     * Ordena los documentos por el campo 'id' en orden descendente y limita a los 20 documentos más recientes.
     * Cada entrada se agrega a una lista de datos que se utiliza para actualizar el gráfico.
     */
    private void loadGlucoseData() {
        if (idUser == null || idUser.isEmpty()) {
            Toast.makeText(this, "ID de usuario no encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("GraficasActivity", "Cargando datos para el usuario: " + idUser);

        db.collection("glucose")
                .whereEqualTo("idUser", idUser)
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        if (value != null) {
                            List<DataEntry> entries = new ArrayList<>();
                            for (QueryDocumentSnapshot document : value) {
                                Log.d("GraficasActivity", "Documento recuperado: " + document.getData());

                                if (document.contains("glucose") && document.contains("day") && document.contains("month") && document.contains("year") && document.contains("id")) {
                                    Long glucoseNumber = document.getLong("glucose");
                                    Long day = document.getLong("day");
                                    Long month = document.getLong("month");
                                    Long year = document.getLong("year");
                                    Long id = document.getLong("id");

                                    Log.d("GraficasActivity", "ID: " + id + " Glucosa: " + glucoseNumber + " Fecha: " + day + "/" + month + "/" + year);

                                    if (glucoseNumber != null && day != null && month != null && year != null) {
                                        String date = day + "/" + month + "/" + year + " " + id;
                                        entries.add(new ValueDataEntry(date, glucoseNumber));
                                    }
                                } else {
                                    Log.d("GraficasActivity", "Documento sin 'glucose', 'day', 'month', 'year' o 'id': " + document.getId());
                                }
                            }
                            Log.d("GraficasActivity", "Número de entradas: " + entries.size());
                            if (!entries.isEmpty()) {
                                updateChart("Glucosa", entries);
                            } else {
                                Toast.makeText(GraficasActivity.this, "No se encontraron datos de glucosa.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /**
     * Carga los datos de peso desde Firestore y los muestra en el gráfico de AnyChartView.
     */
    private void loadWeightData() {
        db.collection("weights")
                .whereEqualTo("idUser", idUser)
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("GraficasActivity", "Listen failed.", e);
                            return;
                        }

                        List<DataEntry> entries = new ArrayList<>();
                        for (QueryDocumentSnapshot document : value) {
                            Long weight = document.getLong("weight");
                            Long day = document.getLong("day");
                            Long month = document.getLong("month");
                            Long year = document.getLong("year");
                            String date = day + "/" + month + "/" + year;
                            entries.add(new ValueDataEntry(date, weight));
                        }
                        Log.d("GraficasActivity", "Número de entradas de peso: " + entries.size());
                        if (!entries.isEmpty()) {
                            updateChart("Peso", entries);
                        } else {
                            Toast.makeText(GraficasActivity.this, "No se encontraron datos de peso.", Toast.LENGTH_SHORT).show();
                        }
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
                .limit(10)
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
                                updateChart("Diastólica", diastolicaEntries);
                                updateChart("Sistólica", sistolicaEntries);
                                updateChart("Pulso", pulseEntries);
                            } else {
                                Toast.makeText(GraficasActivity.this, "No se encontraron datos de tensión.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /**
     * Actualiza el gráfico de líneas con los datos proporcionados.
     *
     * @param title   El título de la serie de datos
     * @param entries Lista de entradas de datos que se utilizarán para actualizar el gráfico.
     */
    private void updateChart(String title, List<DataEntry> entries) {
        Cartesian cartesian = anyChartView.getChart() != null ? (Cartesian) anyChartView.getChart() : AnyChart.cartesian();

        Line line = cartesian.line(entries);
        line.name(title);

        cartesian.animation(true);
        cartesian.title("Evolución de la Salud");
        cartesian.yScale().minimum(0d);
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT).anchor(Anchor.LEFT_CENTER).offsetX(0d).offsetY(5d).format("{%Value}{groupsSeparator: }");
        cartesian.xAxis(0).title("Fecha");
        cartesian.yAxis(0).title("Valores");

        cartesian.legend().enabled(true);
        cartesian.legend().align(Align.CENTER).itemsLayout(LegendLayout.HORIZONTAL);

        anyChartView.setChart(cartesian);
        anyChartView.invalidate();
    }
}
