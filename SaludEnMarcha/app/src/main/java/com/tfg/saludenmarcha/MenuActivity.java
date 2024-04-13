package com.tfg.saludenmarcha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button buttonActividad = findViewById(R.id.buttonActividad);
        buttonActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ActividadMenuActivity.class);
                startActivity(intent);
            }
        });

        Button buttonHistorial = findViewById(R.id.buttonHistorial);
        buttonHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  Intent intent = new Intent(MenuActivity.this, HistorialActivity.class);
             //   startActivity(intent);
            }
        });

        Button buttonPeso = findViewById(R.id.buttonPeso);
        buttonPeso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  Intent intent = new Intent(MenuActivity.this, PesoActivity.class);
              //  startActivity(intent);
            }
        });

        Button buttonAlimentacion = findViewById(R.id.buttonAlimentacion);
        buttonAlimentacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, AlimentacionActivity.class);
                startActivity(intent);
            }
        });

        Button buttonGlucosa = findViewById(R.id.buttonGlucosa);
        buttonGlucosa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Intent intent = new Intent(MenuActivity.this, GlucosaActivity.class);
             //   startActivity(intent);
            }
        });

        Button buttonTensionPulso = findViewById(R.id.buttonTensionPulso);
        buttonTensionPulso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Intent intent = new Intent(MenuActivity.this, TensionPulsoActivity.class);
             //   startActivity(intent);
            }
        });

        Button buttonMedicacion = findViewById(R.id.buttonMedicacion);
        buttonMedicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Intent intent = new Intent(MenuActivity.this, MedicacionActivity.class);
             //   startActivity(intent);
            }
        });

        Button buttonCalendario = findViewById(R.id.buttonCalendario);
        buttonCalendario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Intent intent = new Intent(MenuActivity.this, CalendarioActivity.class);
            //    startActivity(intent);
            }
        });

        Button buttonEmergencia = findViewById(R.id.buttonEmergencia);
        buttonEmergencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Intent intent = new Intent(MenuActivity.this, EmergenciaActivity.class);
             //   startActivity(intent);
            }
        });

        Button buttonResumen = findViewById(R.id.buttonResumen);
        buttonResumen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Intent intent = new Intent(MenuActivity.this, ResumenActivity.class);
             //   startActivity(intent);
            }
        });
    }
}
