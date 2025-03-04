package com.consul.suika;

public interface DatabaseService {
    long insertPlayer(String name); // Insert a player and return their ID
    void insertScorePakBulaklak(int score, long playerId); // Insert score for Pak Bulaklak
    void insertScoreBloomtastic(int score, long playerId); // Insert score for Bloomtastic
}
