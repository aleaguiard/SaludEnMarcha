package com.tfg.saludenmarcha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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

public class GraficasActivity extends AppCompatActivity {

    private Button volverButton, showGlucoseButton, showPressureButton, showWeightButton;
    private CardView cardView;
    private AnyChartView anyChartView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String idUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graficas);
        EdgeToEdge.enable(this);

        volverButton = findViewById(R.id.volverResumenButton);
        showGlucoseButton = findViewById(R.id.showGraphicGlucosa);
        showPressureButton = findViewById(R.id.showGraphicPulso);
        showWeightButton = findViewById(R.id.showGraphicPeso);
        cardView = findViewById(R.id.cardView);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            idUser = currentUser.getUid();
        } else {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        volverButton.setOnClickListener(v -> finish());
        showGlucoseButton.setOnClickListener(v -> loadGlucoseData());
        showPressureButton.setOnClickListener(v -> loadTensionData());
        showWeightButton.setOnClickListener(v -> loadWeightData());
    }

    private void loadWeightData() {
        Log.d("GraficasActivity", "Cargando datos Peso");

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

                        updateChart("Peso", "Peso en kg", entries);
                    }
                });
    }

    private void loadGlucoseData() {
        Log.d("GraficasActivity", "Cargando datos glucosa");

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
                            Log.w("GraficasActivity", "Listen failed.", e);
                            return;
                        }

                        List<DataEntry> entries = new ArrayList<>();
                        for (QueryDocumentSnapshot document : value) {
                            Long glucose = document.getLong("glucose");
                            Long day = document.getLong("day");
                            Long month = document.getLong("month");
                            Long year = document.getLong("year");
                            String date = day + "/" + month + "/" + year;
                            entries.add(new ValueDataEntry(date, glucose));
                        }

                        updateChart("Glucosa", "Nivel de glucosa", entries);
                    }
                });
    }

    private void loadTensionData() {
        Log.d("GraficasActivity", "Cargando datos Tension");

        db.collection("pressure-pulse")
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

                        List<DataEntry> diastolicaEntries = new ArrayList<>();
                        List<DataEntry> sistolicaEntries = new ArrayList<>();
                        List<DataEntry> pulseEntries = new ArrayList<>();
                        for (QueryDocumentSnapshot document : value) {
                            Long sistolica = document.getLong("sistolica");
                            Long diastolica = document.getLong("diastolica");
                            Long pulse = document.getLong("pulse");
                            Long day = document.getLong("day");
                            Long month = document.getLong("month");
                            Long year = document.getLong("year");
                            String date = day + "/" + month + "/" + year;
                            diastolicaEntries.add(new ValueDataEntry(date, diastolica));
                            sistolicaEntries.add(new ValueDataEntry(date, sistolica));
                            pulseEntries.add(new ValueDataEntry(date, pulse));
                        }

                        updateTensionChart("Tensión Arterial y Pulso", diastolicaEntries, sistolicaEntries, pulseEntries);
                    }
                });
    }

    private void updateChart(String title, String yAxisTitle, List<DataEntry> entries) {
        Log.d("GraficasActivity", "ACTUALIZANDO CHART: " + title + ", " + yAxisTitle + ", " + entries.size() + " entradas");

        // Verificar si el AnyChartView está inicializado
        if (anyChartView != null) {
            // Eliminar la vista existente si ya está creada
            cardView.removeView(anyChartView);
        }

        // Crear una nueva instancia de AnyChartView
        anyChartView = new AnyChartView(this);
        cardView.addView(anyChartView);

        // Crear una instancia de CartesianChart
        Cartesian cartesian = AnyChart.line();

        // Configurar propiedades del gráfico
        cartesian.title(title);
        cartesian.animation(true);
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.xAxis(0).title("Fecha");
        cartesian.yAxis(0).title(yAxisTitle);
        cartesian.yScale().minimum(0d);

        // Agregar las entradas al gráfico
        Line line = cartesian.line(entries);
        line.name(title);
        line.tooltip()
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d);

        // Establecer el gráfico en el AnyChartView
        anyChartView.setChart(cartesian);
    }


    private void updateTensionChart(String title, List<DataEntry> diastolicaEntries, List<DataEntry> sistolicaEntries, List<DataEntry> pulseEntries) {
        Log.d("GraficasActivity", "ACTUALIZANDO TENSION CHART: " + title + ", Diastolica: " + diastolicaEntries.size() + ", Sistolica: " + sistolicaEntries.size() + ", Pulso: " + pulseEntries.size());

        if (anyChartView != null) {
            // Eliminar la vista existente si ya está creada
            cardView.removeView(anyChartView);
        }

        // Crear una nueva instancia de AnyChartView
        anyChartView = new AnyChartView(this);
        cardView.addView(anyChartView);
        // Crear una instancia de CartesianChart
        Cartesian cartesian = AnyChart.line();

        // Configurar propiedades del gráfico
        cartesian.title(title);
        cartesian.animation(true);
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.xAxis(0).title("Fecha");
        cartesian.yAxis(0).title("Valor");

        // Agregar las líneas al gráfico para las diferentes entradas
        Line diastolicaLine = cartesian.line(diastolicaEntries);
        diastolicaLine.name("Diastólica");
        diastolicaLine.tooltip()
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d);

        Line sistolicaLine = cartesian.line(sistolicaEntries);
        sistolicaLine.name("Sistólica");
        sistolicaLine.tooltip()
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d);

        Line pulseLine = cartesian.line(pulseEntries);
        pulseLine.name("Pulso");
        pulseLine.tooltip()
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d);

        // Establecer el gráfico en el AnyChartView
        anyChartView.setChart(cartesian);
    }

}
