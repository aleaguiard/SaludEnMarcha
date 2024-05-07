package com.tfg.saludenmarcha;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

public class MainActivity extends AppCompatActivity {

    private ImageButton themeBtn;
    private TextView themeText;
    private boolean nightMode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private FirebaseAuth mAuth;
    private String idUser;
    private FirebaseFirestore db;
    private GoogleSignInClient gsc;
    private GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Obtener el estado del último tema y aplicarlo antes del oncreate
        //para evitar delay en el cambio de tema
        sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("nightMode", false);
        editor = sharedPreferences.edit();
        themeText = findViewById(R.id.themeText);

        // Configuración del tema basada en el estado actual
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            themeText.setTextColor(getColor(R.color.white));
            themeText.setText("Dark Mode");
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            themeText.setTextColor(getColor(R.color.black));
            themeText.setText("Light Mode");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Configurar el botón de cambio de tema
        themeBtn = findViewById(R.id.themeBtn);
        themeBtn.setOnClickListener(v -> {
            // Cambiar el estado del modo nocturno
            nightMode = !nightMode;

            // Guardar el estado del modo nocturno en SharedPreferences
            editor.putBoolean("nightMode", nightMode);
            editor.apply();

            // Aplicar el nuevo tema según el estado del modo nocturno
            if (nightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                themeText.setText("Dark Mode");
                themeText.setTextColor(getColor(R.color.white));
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                themeText.setText("Light Mode");
                themeText.setTextColor(getColor(R.color.black));
            }
        });

        // Configuración de inicio de sesión con Google y Firebase Auth
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
        idUser = mAuth.getCurrentUser().getEmail();
        db = FirebaseFirestore.getInstance();

        // Añadir ajustes a la vista
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Manejar clics en los botones
        setupButtonClick(R.id.buttonActividad, ActividadMenuActivity.class);
       // setupButtonClick(R.id.buttonPeso, PesoActivity.class);
       // setupButtonClick(R.id.buttonGlucosa, GlucosaActivity.class);
       // setupButtonClick(R.id.buttonMedicacion, MedicacionActivity.class);
        setupButtonClick(R.id.buttonEmergencia, EmergencyActivity.class);
        setupButtonClick(R.id.buttonHistorial, CarreraHistorialActivity.class);
        setupButtonClick(R.id.buttonNutricion, AlimentacionActivity.class);
        //setupButtonClick(R.id.buttonTension, TensionActivity.class);
        //setupButtonClick(R.id.buttonCalendario, CalendarioActivity.class);
        //setupButtonClick(R.id.buttonResumen, ResumenActivity.class);*/
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
