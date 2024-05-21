package com.tfg.saludenmarcha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.enums.Anchor;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * GraficasActivity es una actividad que muestra gráficos de AnyChartView para los últimos 10 registros
 * de las colecciones de glucosa, presión y pulso, y peso en Firebase Firestore.
 */
public class GraficasActivity extends AppCompatActivity {

    // Variables para los componentes de la interfaz de usuario
    private AnyChartView anyChartGlucose, anyChartPressurePulse, anyChartWeights;
    private Button volverButton;

    // Variables para manejo de Firebase Firestore y autenticación
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String idUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graficas);
        EdgeToEdge.enable(this);  // Habilita la visualización en pantalla completa

        // Inicialización de los componentes de la interfaz de usuario
        anyChartGlucose = findViewById(R.id.any_chart_glucose_graficas);
        anyChartPressurePulse = findViewById(R.id.any_chart_pressure_pulse_graficas);
        anyChartWeights = findViewById(R.id.any_chart_weights_graficas);
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

                        updateChartWeight(anyChartWeights, "Peso", entries);
                    }
                });
    }

    /**
     * Actualiza el gráfico de AnyChartView con los datos proporcionados.
     *
     * @param anyChartView La vista de AnyChartView a actualizar
     * @param title        El título del gráfico
     * @param entries      Los datos a mostrar en el gráfico
     */
    private void updateChartWeight(AnyChartView anyChartView, String title, List<DataEntry> entries) {
        Cartesian cartesian = AnyChart.line();
        cartesian.title(title);
        cartesian.animation(true);
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.xAxis(0).title("Fecha");
        cartesian.yAxis(0).title(title);
        cartesian.yScale().minimum(0d);
        Line line = cartesian.line(entries);
        line.name(title);
        line.tooltip()
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d);
        anyChartView.setChart(cartesian);
    }


    /*
     ********************************************************************
     */

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
                                runOnUiThread(() -> updateChartGlucose(entries));
                            } else {
                                Toast.makeText(GraficasActivity.this, "No se encontraron datos de glucosa.", Toast.LENGTH_SHORT).show();
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
    private void updateChartGlucose(List<DataEntry> entries) {

        Cartesian cartesian = AnyChart.line();

        cartesian.title("Evolución del Nivel de Glucosa");

        Line series = cartesian.line(entries);
        series.name("Glucosa");

        anyChartGlucose.setChart(cartesian);

        anyChartGlucose.invalidate();
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
                                runOnUiThread(() -> updateChartPulse(diastolicaEntries, sistolicaEntries, pulseEntries));
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
     * @param diastolicaEntries Lista de entradas de datos para la línea de presión diastólica.
     * @param sistolicaEntries  Lista de entradas de datos para la línea de presión sistólica.
     * @param pulseEntries      Lista de entradas de datos para la línea de pulso.
     */
    private void updateChartPulse(List<DataEntry> diastolicaEntries, List<DataEntry> sistolicaEntries, List<DataEntry> pulseEntries) {

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
        anyChartPressurePulse.setChart(cartesian);

        // Forzar la invalidación de la vista para asegurar que se actualice visualmente.
        anyChartPressurePulse.invalidate();
    }

}
