package com.onurelustu.morsecodeapp;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText inputText;
    private TextView outputText, frequencyText;
    private MaterialButton translateButton, playButton, downloadButton, copyButton, shareButton;
    private RadioGroup translationMode;
    private RadioButton textToMorse, morseToText;
    private SeekBar frequencySeekBar;
    private Spinner themeSpinner;
    private HashMap<Character, String> morseCodeMap;
    private HashMap<String, Character> morseToTextMap;
    private String morseCodeResult;
    private double selectedFrequency = 800.0;
    private AudioTrack audioTrack; // AudioTrack sınıf değişkeni
    private Thread audioThread; // Ses çalma thread'ini takip etmek için

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Tanımlamalar
        inputText = findViewById(R.id.inputText);
        outputText = findViewById(R.id.outputText);
        frequencyText = findViewById(R.id.frequencyText);
        translateButton = findViewById(R.id.translateButton);
        playButton = findViewById(R.id.playButton);
        downloadButton = findViewById(R.id.downloadButton);
        copyButton = findViewById(R.id.copyButton);
        shareButton = findViewById(R.id.shareButton);
        translationMode = findViewById(R.id.translationMode);
        textToMorse = findViewById(R.id.textToMorse);
        morseToText = findViewById(R.id.morseToText);
        frequencySeekBar = findViewById(R.id.frequencySeekBar);
        themeSpinner = findViewById(R.id.themeSpinner);
        TextInputLayout inputLayout = findViewById(R.id.inputLayout);

        // Varsayılan tema: Koyu Gri Tema
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        findViewById(R.id.mainLayout).setBackgroundResource(R.drawable.gradient_background_main);
        themeSpinner.setSelection(1);

        // Animasyonlar
        animateEntrance(themeSpinner, 0);
        animateEntrance(inputLayout, 100);
        animateEntrance(translationMode, 200);
        animateEntrance(translateButton, 300);
        animateEntrance(outputText, 400);
        animateEntrance(copyButton, 500);
        animateEntrance(shareButton, 600);
        animateEntrance(playButton, 700);
        animateEntrance(frequencySeekBar, 800);
        animateEntrance(frequencyText, 900);
        animateEntrance(downloadButton, 1000);

        // themeSpinner null kontrolü
        if (themeSpinner == null) {
            Toast.makeText(this, "Tema seçici bulunamadı!", Toast.LENGTH_LONG).show();
            return;
        }

        // Mors kodu haritalarını başlat
        initializeMorseCodeMaps();

        // Tema seçimi
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // Açık Tema
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        findViewById(R.id.mainLayout).setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.light_background));
                        outputText.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.light_output));
                        outputText.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.black));
                        break;
                    case 1: // Koyu Gri Tema
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        findViewById(R.id.mainLayout).setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.dark_gray_background));
                        outputText.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.dark_gray_output));
                        outputText.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.white));
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Ses frekansı ayarı
        frequencySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedFrequency = progress;
                frequencyText.setText("Ses Frekansı: " + progress + " Hz");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Çeviri butonu
        translateButton.setOnClickListener(v -> {
            String input = inputText.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Lütfen bir şeyler girin!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (textToMorse.isChecked()) {
                morseCodeResult = textToMorse(input.toUpperCase());
                outputText.setText(morseCodeResult);
                playButton.setEnabled(true);
                downloadButton.setEnabled(true);
            } else {
                morseCodeResult = null;
                String result = morseToText(input);
                outputText.setText(result);
                playButton.setEnabled(false);
                downloadButton.setEnabled(false);
            }
        });

        // Ses çalma butonu
        playButton.setOnClickListener(v -> {
            if (morseCodeResult != null && !morseCodeResult.isEmpty()) {
                stopAudioTrack(); // Önceki sesi durdur
                playMorseCode(morseCodeResult);
            } else {
                Toast.makeText(this, "Önce metni Morsa çevirin!", Toast.LENGTH_SHORT).show();
            }
        });

        // İndirme butonu
        downloadButton.setOnClickListener(v -> {
            if (morseCodeResult != null && !morseCodeResult.isEmpty()) {
                saveMorseCodeAudio(morseCodeResult);
            } else {
                Toast.makeText(this, "Önce metni Morsa çevirin!", Toast.LENGTH_SHORT).show();
            }
        });

        // Kopyalama butonu
        copyButton.setOnClickListener(v -> {
            String result = outputText.getText().toString();
            if (!result.equals("Sonuç burada görünecek") && !result.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("MorseCodeResult", result);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Sonuç panoya kopyalandı!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Kopyalanacak bir sonuç yok!", Toast.LENGTH_SHORT).show();
            }
        });

        // Paylaşma butonu
        shareButton.setOnClickListener(v -> {
            String result = outputText.getText().toString();
            if (!result.equals("Sonuç burada görünecek") && !result.isEmpty()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, result);
                startActivity(Intent.createChooser(shareIntent, "Sonucu Paylaş"));
            } else {
                Toast.makeText(this, "Paylaşılacak bir sonuç yok!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void animateEntrance(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(delay)
                .start();
    }

    private void initializeMorseCodeMaps() {
        morseCodeMap = new HashMap<>();
        morseToTextMap = new HashMap<>();
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
            morseToTextMap.put(pair[1], pair[0].charAt(0));
        }
    }

    private String textToMorse(String text) {
        StringBuilder morse = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (morseCodeMap.containsKey(c)) {
                morse.append(morseCodeMap.get(c)).append(" ");
            } else if (c == ' ') {
                morse.append("  ");
            }
        }
        return morse.toString().trim();
    }

    private String morseToText(String morse) {
        StringBuilder text = new StringBuilder();
        String[] words = morse.split("  ");
        for (int i = 0; i < words.length; i++) {
            String[] letters = words[i].trim().split(" ");
            for (String letter : letters) {
                if (morseToTextMap.containsKey(letter)) {
                    text.append(morseToTextMap.get(letter));
                }
            }
            if (i < words.length - 1) text.append(" ");
        }
        return text.toString();
    }

    private void playMorseCode(String morseCode) {
        final int SAMPLE_RATE = 44100;
        final int DOT_DURATION = 200;
        final int DASH_DURATION = DOT_DURATION * 3;
        final int SILENCE_DURATION = DOT_DURATION;

        // Yeni AudioTrack oluşturmadan önce temizle
        stopAudioTrack();

        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, SAMPLE_RATE, AudioTrack.MODE_STREAM);

        // Ses çalma thread'i
        audioThread = new Thread(() -> {
            try {
                if (audioTrack != null) {
                    audioTrack.play();
                    for (char c : morseCode.toCharArray()) {
                        if (c == '.') {
                            playTone(audioTrack, selectedFrequency, DOT_DURATION, SAMPLE_RATE);
                            silence(audioTrack, SILENCE_DURATION, SAMPLE_RATE);
                        } else if (c == '-') {
                            playTone(audioTrack, selectedFrequency, DASH_DURATION, SAMPLE_RATE);
                            silence(audioTrack, SILENCE_DURATION, SAMPLE_RATE);
                        } else if (c == ' ') {
                            silence(audioTrack, SILENCE_DURATION * 2, SAMPLE_RATE);
                        }
                    }
                    stopAudioTrack(); // Çalma bitince temizle
                }
            } catch (IllegalStateException e) {
                e.printStackTrace(); // Hata durumunda logla
            }
        });
        audioThread.start();
    }

    private void playTone(AudioTrack audioTrack, double frequency, int duration, int sampleRate) {
        int numSamples = duration * sampleRate / 1000;
        short[] buffer = new short[numSamples];
        for (int i = 0; i < numSamples; i++) {
            buffer[i] = (short) (Math.sin(2 * Math.PI * i * frequency / sampleRate) * Short.MAX_VALUE);
        }
        if (audioTrack != null) {
            audioTrack.write(buffer, 0, numSamples);
        }
    }

    private void silence(AudioTrack audioTrack, int duration, int sampleRate) {
        int numSamples = duration * sampleRate / 1000;
        short[] buffer = new short[numSamples];
        if (audioTrack != null) {
            audioTrack.write(buffer, 0, numSamples);
        }
    }

    private void saveMorseCodeAudio(String morseCode) {
        final int SAMPLE_RATE = 44100;
        final int DOT_DURATION = 200;
        final int DASH_DURATION = DOT_DURATION * 3;
        final int SILENCE_DURATION = DOT_DURATION;

        int totalSamples = 0;
        for (char c : morseCode.toCharArray()) {
            if (c == '.') totalSamples += DOT_DURATION * SAMPLE_RATE / 1000 + SILENCE_DURATION * SAMPLE_RATE / 1000;
            else if (c == '-') totalSamples += DASH_DURATION * SAMPLE_RATE / 1000 + SILENCE_DURATION * SAMPLE_RATE / 1000;
            else if (c == ' ') totalSamples += SILENCE_DURATION * 2 * SAMPLE_RATE / 1000;
        }

        short[] audioData = new short[totalSamples];
        int offset = 0;

        for (char c : morseCode.toCharArray()) {
            if (c == '.') {
                offset = writeTone(audioData, offset, selectedFrequency, DOT_DURATION, SAMPLE_RATE);
                offset = writeSilence(audioData, offset, SILENCE_DURATION, SAMPLE_RATE);
            } else if (c == '-') {
                offset = writeTone(audioData, offset, selectedFrequency, DASH_DURATION, SAMPLE_RATE);
                offset = writeSilence(audioData, offset, SILENCE_DURATION, SAMPLE_RATE);
            } else if (c == ' ') {
                offset = writeSilence(audioData, offset, SILENCE_DURATION * 2, SAMPLE_RATE);
            }
        }

        File file = new File(Environment.getExternalStorageDirectory(), "morse_audio_" + System.currentTimeMillis() + ".pcm");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            for (short sample : audioData) {
                fos.write(sample & 0xff);
                fos.write((sample >> 8) & 0xff);
            }
            Toast.makeText(this, "Ses dosyası kaydedildi: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Dosya kaydedilemedi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private int writeTone(short[] audioData, int offset, double frequency, int duration, int sampleRate) {
        int numSamples = duration * sampleRate / 1000;
        for (int i = 0; i < numSamples; i++) {
            audioData[offset + i] = (short) (Math.sin(2 * Math.PI * i * frequency / sampleRate) * Short.MAX_VALUE);
        }
        return offset + numSamples;
    }

    private int writeSilence(short[] audioData, int offset, int duration, int sampleRate) {
        int numSamples = duration * sampleRate / 1000;
        for (int i = 0; i < numSamples; i++) {
            audioData[offset + i] = 0;
        }
        return offset + numSamples;
    }

    // AudioTrack'ı güvenli bir şekilde durdur ve serbest bırak
    private void stopAudioTrack() {
        if (audioTrack != null) {
            try {
                if (audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
                    if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                        audioTrack.stop();
                    }
                    audioTrack.release();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace(); // Hata loglanıyor ama çökme önleniyor
            } finally {
                audioTrack = null;
            }
        }
        if (audioThread != null && audioThread.isAlive()) {
            audioThread.interrupt(); // Thread'i güvenli bir şekilde kes
            audioThread = null;
        }
    }

    // Aktivite durduğunda sesi durdur
    @Override
    protected void onPause() {
        super.onPause();
        stopAudioTrack();
    }

    // Aktivite kapandığında sesi temizle
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudioTrack();
    }
}