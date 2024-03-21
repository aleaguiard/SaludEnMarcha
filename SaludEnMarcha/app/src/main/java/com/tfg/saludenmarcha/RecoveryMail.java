package com.tfg.saludenmarcha;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class RecoveryMail extends AppCompatActivity {

    Button botonRecovery;
    ImageButton botonBack;
    EditText editEmail;
    FirebaseAuth mAuth;
    String strEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_mail);

        mAuth = FirebaseAuth.getInstance();
        editEmail = findViewById(R.id.cajaEmailRestablecer);

        botonRecovery = findViewById(R.id.botonRecuperar);
        botonRecovery.setOnClickListener(view -> {
            strEmail = editEmail.getText().toString().trim();

            if (!TextUtils.isEmpty(strEmail)) {
                ResetPassword();
            } else {
                editEmail.setError("El email no puede estar vacío");
            }
        });

        botonBack = findViewById(R.id.backBoton);
        botonBack.setOnClickListener(view -> {
            // Return
            Intent intent = new Intent(RecoveryMail.this, Login.class);
            startActivity(intent);
        });
    }

    private void ResetPassword() {
        if (!validateEmail()) {
            return;
        }
        /* Envío del link para password olvidada.
         Se podría mejorar haciendo una comprobación previa en la BBDD
         para corroborar si el usuario está o no registrado
        */
        mAuth.sendPasswordResetEmail(strEmail)

                .addOnSuccessListener(unused -> {
                    Toast.makeText(RecoveryMail.this, R.string.link_enviado, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RecoveryMail.this, Login.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RecoveryMail.this,
                        "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private boolean validateEmail() {
        strEmail = editEmail.getText().toString().trim();
        if (TextUtils.isEmpty(strEmail)) {
            editEmail.setError(getString(R.string.error_mail_vacio));
            return false;
        } else if (!isValidEmail(strEmail)) {
            editEmail.setError(getString(R.string.formato_correo_incorrecto));
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        // Expresión regular para verificar el email
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

}