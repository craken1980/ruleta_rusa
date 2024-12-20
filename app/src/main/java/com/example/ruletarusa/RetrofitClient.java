package com.example.ruletarusa;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://producto3-ruleta-default-rtdb.europe-west1.firebasedatabase.app/";

    public static FirebaseApi getFirebaseApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(FirebaseApi.class);
    }
}
