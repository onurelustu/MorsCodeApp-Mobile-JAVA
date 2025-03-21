package com.onurelustu.morsecodeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Elemanları tanımla
        TextView welcomeTitle = findViewById(R.id.welcomeTitle);
        MaterialButton writeMorseButton = findViewById(R.id.writeMorseButton);
        MaterialButton learnMorseButton = findViewById(R.id.learnMorseButton);

        // Animasyonları uygula
        animateEntrance(welcomeTitle, 0);
        animateEntrance(writeMorseButton, 200);
        animateEntrance(learnMorseButton, 400);

        // Buton tıklama olayları
        writeMorseButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        learnMorseButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LearnActivity.class);
            startActivity(intent);
        });
    }

    private void animateEntrance(View view, long delay) {
        view.setAlpha(0f); // Başlangıçta görünmez
        view.setTranslationY(50f); // 50dp aşağıda
        view.animate()
                .alpha(1f) // Görünür hale gel
                .translationY(0f) // Normal pozisyona dön
                .setDuration(500) // 0.5 saniye
                .setStartDelay(delay) // Gecikme (başlık, butonlar sırayla gelecek)
                .start();
    }
}