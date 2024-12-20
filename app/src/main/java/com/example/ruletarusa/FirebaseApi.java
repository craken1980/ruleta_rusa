package com.example.ruletarusa;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface FirebaseApi {
    @PATCH("scores/{userId}.json")
    Call<Void> saveUserScore(@Path("userId") String userId, @Body Puntuacion puntuacion);

    @GET("scores/{userId}.json")
    Call<Puntuacion> getUserScore(@Path("userId") String userId);

    @GET("scores.json")  // Obtiene todos los scores
    Call<Map<String, Puntuacion>> getAllScores();
}
