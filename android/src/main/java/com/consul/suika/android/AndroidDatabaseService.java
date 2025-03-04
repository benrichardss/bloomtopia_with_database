package com.consul.suika.android;

import android.content.Context;
import com.consul.suika.DatabaseService;

public class AndroidDatabaseService implements DatabaseService {
    private DBManager dbManager;

    public AndroidDatabaseService(Context context) {
        this.dbManager = new DBManager(context); // Initialize DBManager
    }

    @Override
    public long insertPlayer(String name) {
        return dbManager.insertPlayer(name);
    }

    @Override
    public void insertScorePakBulaklak(int score, long playerId) {
        dbManager.insertScorePakBulaklak(score, playerId);
    }

    @Override
    public void insertScoreBloomtastic(int score, long playerId) {
        dbManager.insertScoreBloomtastic(score, playerId);
    }
}
