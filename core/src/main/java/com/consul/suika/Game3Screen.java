package com.consul.suika;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class Game3Screen implements Screen {
    private final SpriteBatch batch;
    private final OrthographicCamera camera;

    private final Texture characterTexture;
    private final Texture platformTexture;
    private final Texture backgroundTexture;
    private final Texture flowerTexture;

    private final Rectangle character;
    private final Array<Rectangle> platforms;
    private final Array<Rectangle> flowers;

    private float gravity = 2.4f;
    private float characterVelocity = 0;
    private boolean isOnPlatform = true;

    private int score = 0;

    // Voice input strength
    private float voiceStrength = 0;

    public Game3Screen() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        characterTexture = new Texture(Gdx.files.internal("char_land.png"));
        platformTexture = new Texture(Gdx.files.internal("platform.jpg"));
        backgroundTexture = new Texture(Gdx.files.internal("bg.jpg"));
        flowerTexture = new Texture(Gdx.files.internal("jasmin.png"));

        character = new Rectangle();
        character.x = 150;
        character.y = 150;
        character.width = 64;
        character.height = 64;

        platforms = new Array<>();
        flowers = new Array<>();

        spawnInitialPlatforms();
    }

    private void spawnInitialPlatforms() {
        Rectangle platform = new Rectangle();
        platform.x = 0;
        platform.y = 0;
        platform.width = 400;
        platform.height = 32;
        platforms.add(platform);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(backgroundTexture, 0, 0);
        for (Rectangle platform : platforms) {
            batch.draw(platformTexture, platform.x, platform.y, platform.width, platform.height);
        }
        for (Rectangle flower : flowers) {
            batch.draw(flowerTexture, flower.x, flower.y, flower.width, flower.height);
        }
        batch.draw(characterTexture, character.x, character.y, character.width, character.height);
        batch.end();

        update(delta);
    }

    private void update(float delta) {
        if (isOnPlatform) {
            characterVelocity = 0;
        } else {
            characterVelocity += gravity;
            character.y -= characterVelocity;
        }

        if (character.y < 0) {
            gameOver();
        }

        for (Rectangle platform : platforms) {
            if (character.overlaps(platform)) {
                isOnPlatform = true;
                character.y = platform.y + platform.height;
                break;
            } else {
                isOnPlatform = false;
            }
        }

        // Jump based on voice strength
        if (voiceStrength > 9.99 && isOnPlatform) {
            characterVelocity = -Math.min(voiceStrength * 5.5f, 50);
            isOnPlatform = false;
        }

        // Move platforms and flowers
        for (Rectangle platform : platforms) {
            platform.x -= 2;
        }
        for (Rectangle flower : flowers) {
            flower.x -= 2;
        }

        // Spawn new platforms and flowers
        if (platforms.get(platforms.size - 1).x < 800) {
            spawnPlatform();
        }
        if (Math.random() < 0.04) {
            spawnFlower();
        }
    }

    private void spawnPlatform() {
        Rectangle platform = new Rectangle();
        platform.x = 800;
        platform.y = 0;
        platform.width = 200;
        platform.height = 32;
        platforms.add(platform);
    }

    private void spawnFlower() {
        Rectangle flower = new Rectangle();
        flower.x = 800;
        flower.y = 100;
        flower.width = 32;
        flower.height = 32;
        flowers.add(flower);
    }

    private void gameOver() {
        // Reset game state
        character.x = 150;
        character.y = 150;
        characterVelocity = 0;
        isOnPlatform = true;
        platforms.clear();
        flowers.clear();
        spawnInitialPlatforms();
    }

    // Method to update voice strength
    public void setVoiceStrength(float strength) {
        this.voiceStrength = strength;
    }

    @Override
    public void show() {
        // Called when this screen becomes the current screen
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {
        // Called when the game is paused
    }

    @Override
    public void resume() {
        // Called when the game is resumed
    }

    @Override
    public void hide() {
        // Called when this screen is no longer the current screen
    }

    @Override
    public void dispose() {
        batch.dispose();
        characterTexture.dispose();
        platformTexture.dispose();
        backgroundTexture.dispose();
        flowerTexture.dispose();
    }
}
