package com.onurelustu.morsecodeapp;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;

public class LearnActivity extends AppCompatActivity {

    private TextView morseLessonText;
    private Button playMorseButton, nextLessonButton;
    private HashMap<Character, String> morseCodeMap;
    private char[] lessons = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private int currentLessonIndex = 0;
    private double selectedFrequency = 800.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        morseLessonText = findViewById(R.id.morseLessonText);
        playMorseButton = findViewById(R.id.playMorseButton);
        nextLessonButton = findViewById(R.id.nextLessonButton);

        // Animasyonları uygula
        animateEntrance(morseLessonText, 0);
        animateEntrance(playMorseButton, 200);
        animateEntrance(nextLessonButton, 400);

        selectedFrequency = getIntent().getDoubleExtra("frequency", 800.0);

        initializeMorseCodeMap();
        updateLesson();

        playMorseButton.setOnClickListener(v -> {
            String morseCode = morseCodeMap.get(lessons[currentLessonIndex]);
            playMorseCode(morseCode);
        });

        nextLessonButton.setOnClickListener(v -> {
            currentLessonIndex = (currentLessonIndex + 1) % lessons.length;
            updateLesson();
        });
    }

    private void animateEntrance(View view, long delay) {
        view.setAlpha(0f); // Başlangıçta görünmez
        view.setTranslationY(50f); // 50dp aşağıda
        view.animate()
                .alpha(1f) // Görünür hale gel
                .translationY(0f) // Normal pozisyona dön
                .setDuration(500) // 0.5 saniye (WelcomeActivity ile aynı)
                .setStartDelay(delay) // Gecikme
                .start();
    }

    private void initializeMorseCodeMap() {
        morseCodeMap = new HashMap<>();
        String[][] morseData = {
                {"A", ".-"}, {"B", "-..."}, {"C", "-.-."}, {"D", "-.."}, {"E", "."},
                {"F", "..-."}, {"G", "--."}, {"H", "...."}, {"I", ".."}, {"J", ".---"},
                {"K", "-.-"}, {"L", ".-.."}, {"M", "--"}, {"N", "-."}, {"O", "---"},
                {"P", ".--."}, {"Q", "--.-"}, {"R", ".-."}, {"S", "..."}, {"T", "-"},
                {"U", "..-"}, {"V", "...-"}, {"W", ".--"}, {"X", "-..-"}, {"Y", "-.--"},
                {"Z", "--.."}, {"0", "-----"}, {"1", ".----"}, {"2", "..---"}, {"3", "...--"},
                {"4", "....-"}, {"5", "....."}, {"6", "-...."}, {"7", "--..."}, {"8", "---.."},
                {"9", "----."}
        };
        for (String[] pair : morseData) {
            morseCodeMap.put(pair[0].charAt(0), pair[1]);
        }
    }

    private void updateLesson() {
        char currentChar = lessons[currentLessonIndex];
        String morseCode = morseCodeMap.get(currentChar);
        morseLessonText.setText(currentChar + " = " + morseCode);
    }

    private void playMorseCode(String morseCode) {
        final int SAMPLE_RATE = 44100;
        final int DOT_DURATION = 200;
        final int DASH_DURATION = DOT_DURATION * 3;
        final int SILENCE_DURATION = DOT_DURATION;

        new Thread(() -> {
            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, SAMPLE_RATE, AudioTrack.MODE_STREAM);
            audioTrack.play();
            for (char c : morseCode.toCharArray()) {
                if (c == '.') {
                    playTone(audioTrack, selectedFrequency, DOT_DURATION, SAMPLE_RATE);
                    silence(audioTrack, SILENCE_DURATION, SAMPLE_RATE);
                } else if (c == '-') {
                    playTone(audioTrack, selectedFrequency, DASH_DURATION, SAMPLE_RATE);
                    silence(audioTrack, SILENCE_DURATION, SAMPLE_RATE);
                }
            }
            audioTrack.stop();
            audioTrack.release();
        }).start();
    }

    private void playTone(AudioTrack audioTrack, double frequency, int duration, int sampleRate) {
        int numSamples = duration * sampleRate / 1000;
        short[] buffer = new short[numSamples];
        for (int i = 0; i < numSamples; i++) {
            buffer[i] = (short) (Math.sin(2 * Math.PI * i * frequency / sampleRate) * Short.MAX_VALUE);
        }
        audioTrack.write(buffer, 0, numSamples);
    }

    private void silence(AudioTrack audioTrack, int duration, int sampleRate) {
        int numSamples = duration * sampleRate / 1000;
        short[] buffer = new short[numSamples];
        audioTrack.write(buffer, 0, numSamples);
    }
}