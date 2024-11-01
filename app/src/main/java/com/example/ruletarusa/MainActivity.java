package com.example.ruletarusa;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ImageView;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.TextView;

import io.reactivex.android.schedulers.AndroidSchedulers;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageView ruleta;
    private TextView txtMonedas;
    private boolean girando = false;
    private int agujeroBala = 1;
    private int monedas = 0;
    private MonedasDatabaseHelper dbHelper;

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
        dbHelper = new MonedasDatabaseHelper(this);
        ruleta = findViewById(R.id.imageView);
        txtMonedas = findViewById(R.id.txt_monedas);

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

    private void EstablecerPuntuacion(int agujeroFinal) {
        if (agujeroFinal == agujeroBala){
            monedas-=3;
        }else{
            monedas++;
        }
        txtMonedas.setText(String.valueOf(monedas));
    }
}