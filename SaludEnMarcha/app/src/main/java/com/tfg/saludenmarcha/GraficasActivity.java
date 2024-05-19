package com.tfg.saludenmarcha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

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
        setContentView(R.layout.activity_resumen);
        EdgeToEdge.enable(this);  // Habilita la visualización en pantalla completa

        // Inicialización de los componentes de la interfaz de usuario
        anyChartGlucose = findViewById(R.id.any_chart_glucose);
        anyChartPressurePulse = findViewById(R.id.any_chart_pressure_pulse);
        anyChartWeights = findViewById(R.id.any_chart_weights);
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
        //loadGlucoseData();
        //loadPressurePulseData();
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

                        updateChart(anyChartWeights, "Peso", entries);
                    }
                });
    }

    /**
     * Actualiza el gráfico de AnyChartView con los datos proporcionados.
     *
     * @param anyChartView La vista de AnyChartView a actualizar
     * @param title El título del gráfico
     * @param entries Los datos a mostrar en el gráfico
     */
    private void updateChart(AnyChartView anyChartView, String title, List<DataEntry> entries) {
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

}
