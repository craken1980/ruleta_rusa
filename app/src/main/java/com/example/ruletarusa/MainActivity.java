package com.example.ruletarusa;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ruletarusa.ui.login.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.reactivex.android.schedulers.AndroidSchedulers;
import retrofit2.Call;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageView ruleta;
    private ImageView imageSound;
    private ImageView imageRanking;
    private ImageButton btnLogout;
    private TextView txtMonedas;
    private TextView txtUser;
    private boolean girando = false;
    private int agujeroBala = 1;
    private int monedas = 0;
    private MonedasDatabaseHelper dbHelper;
    private MediaPlayer mediaPlayer;
    private MediaPlayer hilo_musical;
    private MediaPlayer gun;
    private MediaPlayer coin;
    private FirebaseUser currentUser;

    private static final int PICK_AUDIO_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        getUserScore(currentUser.getUid());

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
        txtUser = findViewById(R.id.txt_user);
        imageSound = findViewById(R.id.imageSound);
        imageRanking = findViewById(R.id.img_ranking);
        btnLogout = findViewById(R.id.btn_logout);

        if (currentUser != null){
            txtUser.setText(currentUser.getDisplayName());
        }

        monedas = dbHelper.obtenerMonedas();
        txtMonedas.setText(String.valueOf(monedas));

       /* dbHelper.getMonedasObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cantidad -> {
                    monedas = cantidad.intValue();
                    txtMonedas.setText(String.valueOf(monedas));
                }, throwable -> {
                });*/
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
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
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
        imageRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RankingActivity.class);
                startActivity(intent);
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

    private void logout() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Ahora que la sesión de Google está cerrada, también cierras la sesión de Firebase
                FirebaseAuth.getInstance().signOut();

                // Redirigir a la pantalla de login (puedes mostrar el login de nuevo)
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Si tienes un MediaPlayer, libera los recursos
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Libera los recursos del MediaPlayer
            mediaPlayer = null;
        }

        // Si tienes un MediaPlayer, libera los recursos
        if (hilo_musical != null) {
            hilo_musical.release(); // Libera los recursos del MediaPlayer
            hilo_musical = null;
        }

        // Si tienes un MediaPlayer, libera los recursos
        if (gun != null) {
            gun.release(); // Libera los recursos del MediaPlayer
            gun = null;
        }

        // Si tienes un MediaPlayer, libera los recursos
        if (coin != null) {
            coin.release(); // Libera los recursos del MediaPlayer
            coin = null;
        }

        // Si tienes otros recursos (por ejemplo, conexiones de red o bases de datos), ciérralos aquí
        // Ejemplo: db.close(); o conexion.close(); dependiendo de lo que estés usando
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
                Puntuacion p = new Puntuacion(currentUser.getDisplayName(),monedas);
                saveUserScore(currentUser.getUid(),p);
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
    public void saveUserScore(String userId, Puntuacion userScore) {
        FirebaseApi api = RetrofitClient.getFirebaseApi();
        api.saveUserScore(userId, userScore).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    System.out.println("Puntuación guardada correctamente.");
                } else {
                    try {
                        System.out.println("Error al guardar puntuación: " + response.errorBody().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                System.out.println("Fallo en la solicitud: " + t.getMessage());
            }
        });
    }

    public void getUserScore(String userId) {
        FirebaseApi api = RetrofitClient.getFirebaseApi();
        api.getUserScore(userId).enqueue(new retrofit2.Callback<Puntuacion>() {
            @Override
            public void onResponse(Call<Puntuacion> call, retrofit2.Response<Puntuacion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Puntuacion userScore = response.body();
                    monedas = userScore.getPoints();
                    txtMonedas.setText(String.valueOf(monedas));
                    System.out.println("Usuario: " + userScore.getUsername() + ", Puntos: " + userScore.getPoints());
                } else {
                    System.out.println("Error al obtener puntuación: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<Puntuacion> call, Throwable t) {
                System.out.println("Fallo en la solicitud: " + t.getMessage());
            }
        });
    }



}