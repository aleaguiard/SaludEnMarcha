package com.tfg.saludenmarcha;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * ActividadMenuActivity es una actividad que muestra un menú de actividades disponibles para el usuario.
 * El usuario puede elegir entre andar, correr o ciclismo.
 * Antes de iniciar la actividad, se solicitan los permisos de ubicación necesarios.
 */
public class ActividadMenuActivity extends AppCompatActivity {

    // Botones para las diferentes actividades
    Button botonAndar;
    Button botonCorrer;
    Button botonCiclismo;
    Button botonVolver;

    // Código de solicitud para los permisos de ubicación
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    // Variable para controlar si el diálogo de explicación ya se ha mostrado
    private boolean explanationShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Comprueba si todos los permisos necesarios están concedidos
        if (todosLosPermisosConcedidos()) {
            // Si todos los permisos están concedidos, inicializa la actividad
            inicializarActividad();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permiso no concedido, solicitarlo
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            }
            // Si no, solicita los permisos
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, REQUEST_LOCATION_PERMISSION);
        }
    }

    /**
     * Inicializa la actividad.
     * Configura la interfaz de usuario y los botones.
     */
    private void inicializarActividad() {
        // Habilita el diseño de borde a borde
        EdgeToEdge.enable(this);

        // Establece el contenido de la vista
        setContentView(R.layout.activity_actividad_menu);

        // Configura los insets de la ventana
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa los botones y configura sus onClickListeners
        botonAndar = findViewById(R.id.buttonAndar);
        botonAndar.setOnClickListener(this::onclickAndar);

        botonCorrer = findViewById(R.id.buttonCorrer);
        botonCorrer.setOnClickListener(this::onclickCorrer);

        botonCiclismo = findViewById(R.id.buttonCiclismo);
        botonCiclismo.setOnClickListener(this::onclickCiclismo);

        botonVolver = findViewById(R.id.buttonVolver);
        botonVolver.setOnClickListener(this::onclickVolver);
    }

    /**
     * Maneja el evento onClick del botón "Andar".
     * Inicia la actividad de seguimiento con el tipo de actividad "Andar".
     */
    public void onclickAndar(View view) {
        iniciarActividadSeguimiento(botonAndar.getText().toString());
    }

    /**
     * Maneja el evento onClick del botón "Correr".
     * Inicia la actividad de seguimiento con el tipo de actividad "Correr".
     */
    public void onclickCorrer(View view) {
        iniciarActividadSeguimiento(botonCorrer.getText().toString());
    }

    /**
     * Maneja el evento onClick del botón "Ciclismo".
     * Inicia la actividad de seguimiento con el tipo de actividad "Ciclismo".
     */
    public void onclickCiclismo(View view) {
        iniciarActividadSeguimiento(botonCiclismo.getText().toString());
    }

    /**
     * Maneja el evento onClick del botón "Volver".
     * Inicia la actividad principal.
     */
    public void onclickVolver(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Inicia la actividad de seguimiento con el tipo de actividad especificado.
     */
    private void iniciarActividadSeguimiento(String tipoActividad) {
        Toast.makeText(ActividadMenuActivity.this, tipoActividad, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, TrackingActivity.class);
        intent.putExtra("tipoActividad", tipoActividad);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // Si todos los permisos están concedidos, inicializa la actividad
            if (todosLosPermisosConcedidos()) {
                inicializarActividad();
            } else {
                // Si los permisos no fueron concedidos y no se ha mostrado la explicación, muestra la explicación
                if (!explanationShown && (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                        || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                        || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION))) {
                    explanationShown = true;
                    showExplanationAndRequestPermissions();
                } else {
                    // Si los permisos fueron denegados definitivamente, muestra un mensaje de Toast
                    Toast.makeText(this, "Los permisos fueron denegados definitivamente. Por favor, habilite los permisos desde ajustes.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Muestra un diálogo de explicación y solicita los permisos de ubicación.
     */
    private void showExplanationAndRequestPermissions() {
        new AlertDialog.Builder(this)
                .setTitle("Permiso de Ubicación Necesario")
                .setMessage("Esta aplicación necesita el permiso de ubicación para funcionar correctamente. Por favor, concede los permisos necesarios.")
                .setPositiveButton("OK", (dialog, which) -> {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    }, REQUEST_LOCATION_PERMISSION);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Comprueba si todos los permisos de ubicación están concedidos.
     */
    private boolean todosLosPermisosConcedidos() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}