package com.example.ruletarusa;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class FirebaseAuthInterceptor implements Interceptor {
    private String authToken;

    public FirebaseAuthInterceptor(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // Añadir el token al header de la solicitud
        Request request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + authToken)  // Se agrega el token en el encabezado
                .build();
        return chain.proceed(request);
    }
}
