package com.example.ruletarusa;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RankingActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RankingAdapter adapter;
    private List<Puntuacion> rankingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        recyclerView = findViewById(R.id.rankingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Obtener el token de autenticación de Firebase (si es necesario)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String idToken = task.getResult().getToken();
                    fetchRanking(idToken);
                } else {
                    Toast.makeText(RankingActivity.this, "Error al obtener el token", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchRanking(String idToken) {
        FirebaseApi api = RetrofitClient.getFirebaseApi();

        api.getAllScores().enqueue(new Callback<Map<String, Puntuacion>>() {
            @Override
            public void onResponse(Call<Map<String, Puntuacion>> call, Response<Map<String, Puntuacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Puntuacion> scores = response.body();

                    // Ordenar los puntajes de mayor a menor
                    List<Puntuacion> sortedScores = new ArrayList<>(scores.values());
                    sortedScores.sort((score1, score2) -> Integer.compare(score2.getPoints(), score1.getPoints()));

                    // Mostrar solo los 10 primeros
                    rankingList.clear();
                    for (int i = 0; i < Math.min(sortedScores.size(), 10); i++) {
                        rankingList.add(sortedScores.get(i));
                    }

                    // Actualizar el RecyclerView
                    adapter = new RankingAdapter(rankingList);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(RankingActivity.this, "Error al obtener los datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Puntuacion>> call, Throwable t) {
                Toast.makeText(RankingActivity.this, "Fallo en la conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
