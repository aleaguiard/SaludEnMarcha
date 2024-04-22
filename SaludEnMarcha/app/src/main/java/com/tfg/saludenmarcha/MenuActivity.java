package com.tfg.saludenmarcha;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;


public class MenuActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String idUser;
    private FirebaseFirestore db;
    GoogleSignInClient gsc;
    GoogleSignInOptions gso;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
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
                Intent intent = new Intent(MenuActivity.this, CarreraHistorialActivity.class);
                startActivity(intent);
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

        Button buttonAlimentacion = findViewById(R.id.buttonNutricion);
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

        Button buttonTensionPulso = findViewById(R.id.buttonTension);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.logout) {
            //Cerrar sesión en Firebase y Google
            mAuth.signOut();
            gsc.signOut();
            startActivity(new Intent(MenuActivity.this, Login.class));
            finish();
            return true;
    }
        return false;
    }
}

