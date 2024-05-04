package com.tfg.saludenmarcha;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;

public class ActividadMenuActivity extends AppCompatActivity {


    Button botonAndar;
    Button botonCorrer;
    Button botonCiclismo;
    Button botonVolver;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // No se han concedido todos los permisos, solicitarlos
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, REQUEST_LOCATION_PERMISSION);
        } else {
            // Permiso concedido
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
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                // Permiso denegado
            }
        }
    }
}