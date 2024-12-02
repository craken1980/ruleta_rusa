package com.example.ruletarusa;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;
import android.widget.ImageView;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageView ruleta;
    private ImageView imageSound;
    private TextView txtMonedas;
    private boolean girando = false;
    private int agujeroBala = 1;
    private int monedas = 0;
    private MonedasDatabaseHelper dbHelper;
    private MediaPlayer mediaPlayer;
    private MediaPlayer hilo_musical;
    private MediaPlayer gun;
    private MediaPlayer coin;

    private static final int PICK_AUDIO_REQUEST = 1;

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
        mediaPlayer = MediaPlayer.create(this,R.raw.revolver_click);
        hilo_musical = MediaPlayer.create(this,R.raw.hilo_musical);
        gun = MediaPlayer.create(this,R.raw.gun);
        coin = MediaPlayer.create(this,R.raw.coin);


        hilo_musical.setLooping(true);
        hilo_musical.setVolume(0.4f,0.4f);
        coin.setVolume(0.3f,0.3f);
        hilo_musical.start();
        dbHelper = new MonedasDatabaseHelper(this);
        ruleta = findViewById(R.id.imageView);
        txtMonedas = findViewById(R.id.txt_monedas);
        imageSound = findViewById(R.id.imageSound);

        monedas = dbHelper.obtenerMonedas();
        txtMonedas.setText(String.valueOf(monedas));

        dbHelper.getMonedasObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cantidad -> {
                    monedas = cantidad.intValue();
                    txtMonedas.setText(String.valueOf(monedas));
                }, throwable -> {
                });
        // Configura el click en la imagen para comenzar la animación
        ruleta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!girando) {
                    iniciarRotacion();
                }
            }
        });
        // Configura el click en la imagen para comenzar la animación
        imageSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hilo_musical.isPlaying()){
                    imageSound.setImageResource(R.drawable.bocina_silencio);
                    hilo_musical.pause();
                }else{
                    imageSound.setImageResource(R.drawable.bocina);
                    hilo_musical.start();
                }
            }
        });
        // Configura el click en la imagen para comenzar la animación
        imageSound.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                seleccionarArchivoAudio();
                return false;
            }
        });
    }

    private void iniciarRotacion() {
        girando = true;

        // Ángulo aleatorio para detenerse en un agujero (360 grados divididos por número de agujeros)
        int numAgujeros = 6; // Por ejemplo, si tienes 6 agujeros en la ruleta
        int agujeroFinal = new Random().nextInt(numAgujeros);
        int gradosFinal = 360 * 5 + (agujeroFinal * (360 / numAgujeros)); // Ajusta el giro

        // Configura la animación de rotación
        ObjectAnimator rotacion = ObjectAnimator.ofFloat(ruleta, "rotation", 0, gradosFinal);
        rotacion.setDuration(3000); // 3 segundos para dar realismo
        rotacion.setInterpolator(new android.view.animation.DecelerateInterpolator()); // Para simular desaceleración

        // Sonido sincronizado con la rotación
        long[] intervalos = calcularIntervalosSonido(gradosFinal, rotacion.getDuration());

        Handler handler = new Handler();
        for (int i = 0; i < intervalos.length; i++) {
            long delay = intervalos[i];
            handler.postDelayed(() -> {
                if (girando) {
                    mediaPlayer.seekTo(0); // Reinicia el sonido
                    mediaPlayer.start();
                }
            }, delay);
        }

        // Configura la animación
        AnimatorSet animacion = new AnimatorSet();
        animacion.play(rotacion);
        animacion.start();

        // Al final de la animación, habilita de nuevo el botón de giro
        rotacion.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                girando = false;
                EstablecerPuntuacion(agujeroFinal);
                dbHelper.guardarMonedas(monedas);
            }
        });
    }

    // Calcula los intervalos para el sonido
    private long[] calcularIntervalosSonido(int gradosTotales, long duracionTotal) {
        int pasos = 50; // Número de sonidos a reproducir (puedes ajustar esto)
        long[] intervalos = new long[pasos];

        float desaceleracion = 1.5f; // Ajusta la desaceleración para sincronizar con la animación
        for (int i = 0; i < pasos; i++) {
            float progreso = (float) i / pasos;
            float velocidad = 1 - (progreso * progreso * desaceleracion); // Simula la desaceleración
            intervalos[i] = (long) (duracionTotal * progreso / velocidad);
        }
        return intervalos;
    }


    private void EstablecerPuntuacion(int agujeroFinal) {
        if (agujeroFinal == agujeroBala){
            gun.start();
            monedas-=3;
        }else{
            coin.start();
            monedas++;
        }
        txtMonedas.setText(String.valueOf(monedas));
    }

    // Método para abrir el selector de archivos
    private void seleccionarArchivoAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");  // Solo selecciona archivos de audio
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    // Manejar la selección del archivo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_AUDIO_REQUEST) {
            Uri selectedAudioUri = data.getData();
            reproducirHiloMusicalExterno(selectedAudioUri);
        }
    }

    private void reproducirHiloMusicalExterno(Uri audioUri) {
        /*if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }*/

        try {
            hilo_musical.reset();  // Asegúrate de reiniciar el MediaPlayer antes de configurarlo
            ContentResolver contentResolver = getContentResolver();
            AssetFileDescriptor afd = contentResolver.openAssetFileDescriptor(audioUri, "r");

            if (afd != null) {
                hilo_musical.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();  // Cierra el descriptor después de usarlo
            }
            imageSound.setImageResource(R.drawable.bocina);
            hilo_musical.setLooping(true);  // Configura el bucle
            hilo_musical.prepare();  // Prepara el MediaPlayer
            hilo_musical.start();  // Inicia la reproducción
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al reproducir el archivo", Toast.LENGTH_SHORT).show();
        }
    }

}