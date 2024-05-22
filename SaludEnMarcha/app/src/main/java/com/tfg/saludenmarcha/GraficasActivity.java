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
                .limit(10)  // Limitar a los 20 documentos con los IDs más altos para el usuario específico.
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
                            List<DataEntry> imcEntry = new ArrayList<>();
                            List<DataEntry> imcResultEntry = new ArrayList<>();
                            // Iterar sobre los documentos recuperados.
                            for (QueryDocumentSnapshot document : value) {
                                Log.d("PesoActivity", "Documento recuperado: " + document.getData());

                                // Verificar si el documento tiene los campos 'weight', 'day', 'month', 'year', y 'id'.
                                if (document.contains("weight") && document.contains("day") && document.contains("month") && document.contains("year") && document.contains("id")) {
                                    Long weightNumber = document.getLong("weight");
                                    Long imcNumber = document.getLong("imc");
                                    Long day = document.getLong("day");
                                    Long month = document.getLong("month");
                                    Long year = document.getLong("year");
                                    Long id = document.getLong("id");
                                    String imcResult = document.getString("resultIMC");

                                    Log.d("PesoActivity", "ID: " + id + " Weight: " + weightNumber + " Date: " + day + "/" + month + "/" + year);

                                    if (weightNumber != null && day != null && month != null && year != null) {
                                        // Ensamblar la fecha en un formato legible y diferenciar entre pesos del mismo día usando 'id'.
                                        String date = day + "/" + month + "/" + year + " (" + " IMC: "+imcResult + ")";
                                        entries.add(new ValueDataEntry(date, weightNumber));
                                        imcEntry.add(new ValueDataEntry(date, imcNumber));
                                    }
                                } else {
                                    Log.d("PesoActivity", "Documento sin 'weight', 'day', 'month', 'year' o 'id': " + document.getId());
                                }
                            }
                            Log.d("PesoActivity", "Número de entradas: " + entries.size());
                            if (!entries.isEmpty()) {
                                // Asegurarse de actualizar el gráfico en el hilo principal.
                                runOnUiThread(() -> updateChart(entries, imcEntry));
                            } else {
                                Toast.makeText(GraficasActivity.this, "No se encontraron datos de peso.", Toast.LENGTH_SHORT).show();
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
    private void updateChart(List<DataEntry> entries, List<DataEntry> imcEntries) {

        // Crear una instancia del gráfico de líneas.
        Cartesian cartesian = AnyChart.line();

        // Establecer el título del gráfico.
        cartesian.title("Evolución del Peso");

        // Invertir el eje X
        cartesian.xScale().inverted(true);

        // Crear una serie de líneas y agregar los datos a la serie.
        Line series = cartesian.line(entries);
        series.name("Peso");
        series.stroke("2 #0000FF"); // Establece el color de la línea del peso a azul

        Line imcSeries = cartesian.line(imcEntries);
        imcSeries.name("IMC");
        imcSeries.stroke("2 #FF0000"); // Establece el color de la línea del IMC a rojo

        // Configurar el gráfico con los datos de la serie.
        anyChartWeights.setChart(cartesian);

        // Forzar la invalidación de la vista para asegurar que se actualice visualmente.
        anyChartWeights.invalidate();
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
        cartesian.xScale().inverted(true);

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
