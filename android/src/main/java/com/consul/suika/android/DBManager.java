package com.consul.suika.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bloombastic.db";
    private static final int DATABASE_VERSION = 1;
    // SQL statement to create the scores table
    private static final String CREATE_TABLE_PLAYERS =
        "CREATE TABLE players ("
            + "player_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "name TEXT NOT NULL);";
    private static final String CREATE_TABLE_PAK_BULAKLAK =
        "CREATE TABLE pak_bulaklak ("
            + "pak_bulaklak_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "player_id INTEGER NOT NULL,"
            + "score INTEGER NOT NULL,"
            + "FOREIGN KEY(player_id) REFERENCES players(player_id) ON DELETE CASCADE);";

    private static final String CREATE_TABLE_BLOOMTASTIC =
        "CREATE TABLE pak_bulaklak ("
            + "pak_bulaklak_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "player_id INTEGER NOT NULL,"
            + "score INTEGER NOT NULL,"
            + "FOREIGN KEY(player_id) REFERENCES players(player_id) ON DELETE CASCADE);";

    /*private static final String CREATE_TABLE_BLOOMTARATTARAT =
        "CREATE TABLE pak_bulaklak ("
            + "pak_bulaklak_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "player_id INTEGER NOT NULL,"
            + "score INTEGER NOT NULL,"
            + "FOREIGN KEY(player_id) REFERENCES players(player_id) ON DELETE CASCADE);";*/

    public DBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the scores table
        db.execSQL(CREATE_TABLE_PLAYERS);
        db.execSQL(CREATE_TABLE_PAK_BULAKLAK);
        db.execSQL(CREATE_TABLE_BLOOMTASTIC);
        //db.execSQL(CREATE_TABLE_BLOOMTARATTARAT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the table if it exists and recreate it
        db.execSQL("DROP TABLE IF EXISTS players");
        db.execSQL("DROP TABLE IF EXISTS pak_bulaklak");
        db.execSQL("DROP TABLE IF EXISTS bloomtastic");
        //db.execSQL("DROP TABLE IF EXISTS bloomtarattarat");
        onCreate(db);
    }

    public long insertPlayer(String name) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);

        long playerId = db.insert("players", null, values);
        db.close();
        return playerId;
    }

    public void insertScorePakBulaklak(int score, long playerId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("score", score);
        values.put("player_id", playerId); // Foreign key

        db.insert("pak_bulaklak", null, values);
        db.close();
    }

    public void insertScoreBloomtastic(int score, long playerId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("score", score);
        values.put("player_id", playerId); // Foreign key

        db.insert("bloomtastic", null, values);
        db.close();
    }
}
