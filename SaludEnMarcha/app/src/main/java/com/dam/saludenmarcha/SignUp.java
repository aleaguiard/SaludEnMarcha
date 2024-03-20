package com.dam.saludenmarcha;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUp extends AppCompatActivity {

    private Button botonCreate;
    private ImageButton botonBack;
    private FirebaseAuth mAuth;
    private EditText nameText, emailText, passText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        inicializarVariables();

        botonCreate.setOnClickListener(view -> crearUsuario());
        botonBack.setOnClickListener(view -> regresarALogin());
    }

    private void inicializarVariables() {
        nameText = findViewById(R.id.cajaNombreCrear);
        emailText = findViewById(R.id.cajaEmailCrear);
        passText = findViewById(R.id.cajaPasswordCrear);
        botonCreate = findViewById(R.id.botonCrear);
        botonBack = findViewById(R.id.backBoton);
    }

    private void crearUsuario() {
        String nombre = nameText.getText().toString().trim();
        String email = emailText.getText().toString().trim();
        String password = passText.getText().toString().trim();

        if (camposVacios(nombre, email, password) || !validarFormatoEmail(email) || !tamanoPassword(password)) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                      //  StyleableToast.makeText(SignUp.this, "Usuario creado", Toast.LENGTH_SHORT,
                        //        R.style.usuarioRegistrado).show();
                       // FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(SignUp.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(SignUp.this, R.string.fallo_inesperado,
                                Toast.LENGTH_LONG).show();
                    }
                });


    }

    private boolean camposVacios(String nombre, String email, String password) {
        boolean camposVacios = false;

        if (nombre.isEmpty()) {
            nameText.setError(getString(R.string.campo_obligatorio));
            camposVacios = true;
        }

        if (email.isEmpty()) {
            emailText.setError(getString(R.string.campo_obligatorio));
            camposVacios = true;
        }

        if (password.isEmpty()) {
            passText.setError(getString(R.string.campo_obligatorio));
            camposVacios = true;
        }

        return camposVacios;
    }

    private boolean validarFormatoEmail(String email) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getString(R.string.email_invalido));
            return false;
        }
        return true;
    }
    private boolean tamanoPassword(String password) {
        if (password.length() < 6) {
            Toast.makeText(SignUp.this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void regresarALogin() {
        Intent intent = new Intent(SignUp.this, Login.class);
        startActivity(intent);
    }
}