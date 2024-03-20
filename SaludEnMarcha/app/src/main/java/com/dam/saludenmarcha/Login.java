package com.dam.saludenmarcha;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient gsc;
    private ImageView googleBtn;
    private EditText emailText, passText;
    private Button botonLogin;
    private TextView botonSignup, botonForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        configurarGoogleSignIn();
        inicializarVistas();
        verificarUsuarioActual();

        // Configurar la barra de acción
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        botonLogin.setOnClickListener(view -> intentarInicioSesion());
        botonSignup.setOnClickListener(view -> startActivity(new Intent(Login.this, SignUp.class)));
        botonForgot.setOnClickListener(view -> startActivity(new Intent(Login.this, RecoveryMail.class)));
        googleBtn.setOnClickListener(view -> signIn());
    }

    private void configurarGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.defaul_web_client_id))
                .requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
    }

    private void inicializarVistas() {
        googleBtn = findViewById(R.id.google);
        emailText = findViewById(R.id.cajaEmail);
        passText = findViewById(R.id.cajaPassword);
        botonLogin = findViewById(R.id.botonLogin);
        botonSignup = findViewById(R.id.sign_up);
        botonForgot = findViewById(R.id.rememberPass);
    }

    private void verificarUsuarioActual() {
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        }
    }

    private void intentarInicioSesion() {
        String email = emailText.getText().toString().trim();
        String password = passText.getText().toString().trim();

        if (camposVacios(email, password) || !validarFormatoEmail(email) || !tamanoPassword(password)) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Login.this, R.string.autenticacion_fallida,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }


    private boolean camposVacios(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Login.this, R.string.introduce_email_contrasena, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private boolean validarFormatoEmail(String email) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(Login.this, R.string.email_invalido, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean tamanoPassword(String password) {
        if (password.length() < 6) {
            Toast.makeText(Login.this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    void signIn() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    mAuth(account.getIdToken());

                } catch (ApiException e) {
                    Toast.makeText(getApplicationContext(), R.string.fallo_inesperado, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Autenticación con cuenta Google
    private void mAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                });
    }
}