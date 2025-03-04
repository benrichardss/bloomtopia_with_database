package com.consul.suika;

import com.badlogic.gdx.Game;

public class MyGame extends Game {
    private DatabaseService databaseService; // Add DatabaseService

    public MyGame(DatabaseService databaseService) { // Accept DatabaseService
        this.databaseService = databaseService;
    }
    @Override
    public void create() {
        this.setScreen(new FirstScreen(this, databaseService));
    }
}
