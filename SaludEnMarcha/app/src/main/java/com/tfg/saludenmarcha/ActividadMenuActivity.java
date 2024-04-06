package com.tfg.saludenmarcha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;

public class ActividadMenuActivity extends AppCompatActivity {


    Button botonAndar;
    Button botonCorrer;
    Button botonCiclismo;
    Button botonVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_actividad_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        botonAndar = findViewById(R.id.buttonAndar);
        botonAndar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onclickAndar(v);
            }
        });
        botonCorrer = findViewById(R.id.buttonCorrer);
        botonCorrer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onclickCorrer(v);
            }
        });
        botonCiclismo = findViewById(R.id.buttonCiclismo);
        botonCiclismo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onclickCiclismo(v);
            }
        });
        botonVolver = findViewById(R.id.buttonVolver);
        botonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onclickVolver(v);
            }
        });

    }

    public void onclickAndar(View view) {
        Toast.makeText(ActividadMenuActivity.this, "Andar", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, TrackingActivity.class);
        intent.putExtra("tipoActividad",botonAndar.getText().toString());
        startActivity(intent);
    }
    public void onclickCorrer(View view) {
        Toast.makeText(ActividadMenuActivity.this, "Correr", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, TrackingActivity.class);
        intent.putExtra("tipoActividad",botonCorrer.getText().toString());
        startActivity(intent);
    }
    public void onclickCiclismo(View view) {
        Toast.makeText(ActividadMenuActivity.this, "Ciclismo", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, TrackingActivity.class);
        intent.putExtra("tipoActividad",botonCiclismo.getText().toString());
        startActivity(intent);
    }
    public void onclickVolver(View view) {
        Toast.makeText(ActividadMenuActivity.this, "Andar", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, AlimentacionHistorialActivity.class);
        startActivity(intent);
    }
}