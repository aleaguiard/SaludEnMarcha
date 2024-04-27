package com.tfg.saludenmarcha;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportActionBar().hide();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Splash.this , Login.class);
                startActivity(intent);
                finish();
            }
        }, 3000);

        TextView copyright = findViewById(R.id.copyright_text);
        Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.text_animation);
        copyright.startAnimation(anim1);

        ImageView logoInicial = findViewById(R.id.splash_image);
        Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.logo_animation);
        logoInicial.startAnimation(anim2);

    }
}