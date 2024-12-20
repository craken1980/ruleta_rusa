package com.example.ruletarusa;

public class Puntuacion {
    private String username;
    private int points;

    public Puntuacion(String username, int points) {
        this.username = username;
        this.points = points;
    }

    // Getters y setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}