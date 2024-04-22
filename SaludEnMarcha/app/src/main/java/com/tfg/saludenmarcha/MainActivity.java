package com.tfg.saludenmarcha;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

public class MainActivity extends AppCompatActivity {

    private Switch themeSwitch;
    private boolean nightMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private AppCompatDelegate delegate;
    private FirebaseAuth mAuth;
    private String idUser;
    private FirebaseFirestore db;
    private GoogleSignInClient gsc;
    private GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        setupButtonClick(R.id.buttonActividad, ActividadMenuActivity.class);
       /* setupButtonClick(R.id.buttonPeso, PesoActivity.class);
        setupButtonClick(R.id.buttonGlucosa, GlucosaActivity.class);
        setupButtonClick(R.id.buttonMedicacion, MedicacionActivity.class);
        setupButtonClick(R.id.buttonEmergencia, EmergenciaActivity.class);
        setupButtonClick(R.id.buttonHistorial, HistorialActivity.class);
        setupButtonClick(R.id.buttonNutricion, NutricionActivity.class);
        setupButtonClick(R.id.buttonTension, TensionActivity.class);
        setupButtonClick(R.id.buttonCalendario, CalendarioActivity.class);
        setupButtonClick(R.id.buttonResumen, ResumenActivity.class);
      */
        // Configuración del tema
        themeSwitch = findViewById(R.id.themeSwitch);
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("night", false);

        if (nightMode) themeSwitch.setChecked(true);

        themeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nightMode){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("night", false);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("night", true);
                }
                editor.apply();
            }
        });

        // Configuración de inicio de sesión con Google y Firebase Auth
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
        idUser = mAuth.getCurrentUser().getEmail();
        db = FirebaseFirestore.getInstance();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Manejo de las opciones del menú
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            // Cerrar sesión en Firebase y Google
            mAuth.signOut();
            gsc.signOut();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    private void setupButtonClick(int buttonId, Class<?> activityClass) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, activityClass);
            startActivity(intent);
        });
    }
}
