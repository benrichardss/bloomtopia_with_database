package com.consul.suika;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class UsernameScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private TextField usernameField;
    private SpriteBatch batch;
    private Texture background;
    private FitViewport viewport;
    private Game game; // Use Game instead of FirstScreen

    // Texture for the next button and transparent background
    private Texture nextButtonTexture;
    private Texture transparentTexture;

    public UsernameScreen(Game game) { // Accept Game instance
        this.game = game;
    }

    @Override
    public void show() {
        viewport = new FitViewport(110, 192);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json")); // Ensure you have a skin file

        batch = new SpriteBatch();

        background = new Texture("winsertbg.png");

        // Setup next button
        nextButtonTexture = new Texture("nextbutton.png");
        TextureRegionDrawable nextButtonDrawable = new TextureRegionDrawable(new TextureRegion(nextButtonTexture));
        ImageButton nextButton = new ImageButton(nextButtonDrawable);
        nextButton.setPosition(-50, -4);
        nextButton.setSize(200, 200);

        nextButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String username = usernameField.getText().trim();
                if (username.isEmpty()) {
                    // Show a notice dialog if the username field is empty
                    Dialog dialog = new Dialog("Notice", skin) {
                        @Override
                        public void pack() {
                            super.pack();
                            setSize(100, 100);
                        }
                    };
                    dialog.text("Username required. Please enter your username.");
                    dialog.button("OK");
                    dialog.show(stage);
                } else {
                    // Transition directly to GameScreen and pass the username
                    GameScreen gameScreen = new GameScreen(game);
                    //gameScreen.setUsername(username);  // Assumes GameScreen has setUsername()
                    game.setScreen(gameScreen);          // Switch to GameScreen
                }
            }
        });

        // Create a transparent texture for the text field background.
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 0); // Fully transparent
        pixmap.fill();
        transparentTexture = new Texture(pixmap);
        pixmap.dispose();

        // Use the default TextFieldStyle from the skin and override the background to be transparent.
        TextFieldStyle tfStyle = skin.get("default", TextFieldStyle.class);
        tfStyle.background = new TextureRegionDrawable(new TextureRegion(transparentTexture));
        tfStyle.fontColor = Color.WHITE;

        usernameField = new TextField("", tfStyle);
        usernameField.setMessageText("...");
        usernameField.setSize(80, 30);
        // Center the text field within the viewport
        usernameField.setPosition(
            (viewport.getWorldWidth() - usernameField.getWidth()) / 2,
            (viewport.getWorldHeight() - usernameField.getHeight()) / 2
        );

        stage.addActor(usernameField);
        stage.addActor(nextButton);
        stage.setKeyboardFocus(usernameField);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        batch.begin();
        batch.draw(background, 0, 0, 500, 900);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null); // Reset input processor
    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        background.dispose();
        skin.dispose();
        nextButtonTexture.dispose();
        transparentTexture.dispose();
    }
}
