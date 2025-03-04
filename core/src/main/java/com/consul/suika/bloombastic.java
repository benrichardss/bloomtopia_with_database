package com.consul.suika;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import java.util.HashMap;
import java.util.Map;

public class bloombastic implements Screen {

    private Game game;

    public bloombastic(Game game) {
        this.game = game;
    }


    @Override
    public void hide() {

    }

    // Textures and Sprites (unchanged)
    Texture road, player, crack1, crack2, crack3, trafficIsland, people1, people2,  pauseButton, peopleCounterButton, pauseBackground, resumeButton, restartButton, exitButton, playButton;
    SpriteBatch batch;
    FitViewport viewport;
    OrthographicCamera camera;
    Sprite playerSprite, pauseButtonSprite, peopleCounterButtonSprite, pauseBackgroundSprite, resumeButtonSprite, restartButtonSprite, exitButtonSprite, playButtonSprite;
    Vector2 touchPos;
    private Vector2 touchStartPos = new Vector2(); // Stores the starting position of the touch
    private Vector2 touchEndPos = new Vector2(); // Stores the ending position of the touch
    private boolean isSwiping = false; // Whether a swipe is being processed
    private boolean hasSwiped = false; // Whether a swipe has been processed for the current touch
    private final float initialSwipeThreshold = 15f; // Threshold for the first swipe
    private final float doubleSwipeThreshold = 6.25f; // Lower threshold for the second swipe
    private boolean isFirstSwipe = true; // Whether the current swipe is the first in a potential double swipe
    private final float doubleSwipeTimeThreshold = 5f; // Time window for double swipe (in seconds)
    private int currentLane = 1; // 0 = left, 1 = middle, 2 = right
    private int targetLane = 1; // Target lane for transition
    private boolean isTransitioning = false; // Whether the player is transitioning between lanes
    private float transitionProgress = 0f; // Progress of the transition (0 to 1)
    private float transitionSpeed;
    private final float normalTransitionSpeed = 12.5f; // Normal transition speed
    private final float doubleSwipeTransitionSpeed = 30f; // Double swipe transition speed
    private float lastSwipeTime = 0f; // Time of the last swipe
    private int lastSwipeDirection = 0; // Direction of the last swipe (-1 = left, 1 = right)

    float scrollY = 0;
    boolean isPaused = false;
    boolean ignoreNextTouch = false;
    private boolean gameStarted = false;
    private final float[] lanes = {26f, 110f, 194f};
    private enum GameState { MENU, PLAYING, PAUSED, GAME_OVER}
    private GameState gameState = GameState.MENU;
    private int score = 0;
    private int peopleCounter = 0;
    private BitmapFont font;
    GlyphLayout glyphLayout;

    private float obstacleBaseScrollSpeed = 2f; // Starting speed
    private float currentObstacleScrollSpeed; // Current obstacle speed
    private float roadBaseScrollSpeed = 125f; // Separate speed for road animation
    private float currentRoadScrollSpeed;
    private float obstacleMaxSpeed = 9f; // Maximum speed cap
    private float roadMaxSpeed = 562.5f;
    private float obstacleSpeedIncreaseAmount = 0.5f; // Amount to increase speed every 3 seconds
    private float roadSpeedIncreaseAmount = 31.25f;
    private float timeSinceLastSpeedIncrease = 0f; // Timer for speed increasess
    private float scoreTimer = 0f;

    Array<Sprite> crackSprites;
    Array<Sprite> trafficIslandSprites;
    Array<Sprite> peopleSprites;
    Map<Sprite, Boolean> animatedPeopleMap = new HashMap<>();
    Array<Sprite> obstacles;

    // Obstacle spawn logic
    private int obstacleSpawnDelay = 100; // Delay between obstacle spawns (in updates)
    private int obstacleSpawnCounter = 0; // Counter to track spawn delay
    private float desiredDistance = 700f; // Desired distance between obstacles
    private Animation<TextureRegion> playerAnimation;
    private TextureRegion[] playerFrames;
    private float stateTime;
    private float currentFrameDuration = 0.4f; // Starting frame duration
    private final float minFrameDuration = 0.1f; // Minimum frame duration
    private final float frameDurationDecreaseRate = 0.01f; // Rate at which frame duration decreases
    private Animation<TextureRegion> npcAnimation1;
    private Animation<TextureRegion> npcAnimation2;
    private TextureRegion[] npcFrames1;
    private TextureRegion[] npcFrames2;
    private float npcStateTime;
    private float lastTouchTime = 0;
    private final float touchCooldown = 0.5f;

    private boolean ignoreFirstTouch = true;

    public void show() {
        currentObstacleScrollSpeed = obstacleBaseScrollSpeed;
        currentRoadScrollSpeed = roadBaseScrollSpeed;
        font = new BitmapFont();
        font.getData().setScale(1f);
        glyphLayout = new GlyphLayout();
        Gdx.app.log("Debug", "Bloombastic screen loaded");
        ignoreFirstTouch = true;
        Gdx.input.setInputProcessor(null); // Clear any previous input processor
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Gdx.app.log("Bloombastic", "Screen touched");
                return true;
            }
        });

        // Load textures (unchanged)
        player = new Texture("g2_mcSpritesheet.png");
        TextureRegion[][] tmp = TextureRegion.split(player, player.getWidth(), player.getHeight() / 4);
        playerFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            playerFrames[i] = tmp[i][0];
        }

        // Create the animation
        playerAnimation = new Animation<>(currentFrameDuration, playerFrames); // 0.1f is the frame duration
        stateTime = 0f;

        road = new Texture("g2_road.png");
        crack1 = new Texture("g2_crack1.png");
        crack2 = new Texture("g2_crack2.png");
        crack3 = new Texture("g2_crack3.png");
        trafficIsland = new Texture("g2_trafficIsland.png");

        people1 = new Texture("g2_npcSpritesheet.png");
        TextureRegion[][] npcTmp1 = TextureRegion.split(people1, people1.getWidth(), people1.getHeight() / 4); // Assuming 4 frames in a row
        npcFrames1 = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            npcFrames1[i] = npcTmp1[i][0]; // Assuming frames are in a single row
        }

        npcAnimation1 = new Animation<>(0.2f, npcFrames1);

        people2 = new Texture("g2_mcSpritesheet.png");
        TextureRegion[][] npcTmp2 = TextureRegion.split(people2, people2.getWidth(), people2.getHeight() / 4); // Assuming 4 frames in a row
        npcFrames2 = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            npcFrames2[i] = npcTmp2[i][0]; // Assuming frames are in a single row
        }

        npcAnimation2 = new Animation<>(0.2f, npcFrames2); // 0.2f is the frame duration

        npcStateTime = 0f;

        pauseButton = new Texture("g2_pauseButton.png");
        peopleCounterButton = new Texture("g2_peopleCounterButton.png");
        pauseBackground = new Texture("g2_pauseBackground.png");
        resumeButton = new Texture("g2_resumeButton.png");
        restartButton = new Texture("g2_restartButton.png");
        exitButton = new Texture("g2_exitButton.png");
        playButton = new Texture("g2_playButton.png");

        road.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        viewport = new FitViewport(300, 560);
        camera = (OrthographicCamera) viewport.getCamera();
        touchPos = new Vector2();

        batch = new SpriteBatch();

        // Initialize sprites (unchanged)
        playerSprite = new Sprite(player);
        playerSprite.setSize(80, 80);
        playerSprite.setPosition(viewport.getWorldWidth()/2 - playerSprite.getWidth()/2, 75);

        pauseButtonSprite = new Sprite(pauseButton);
        pauseButtonSprite.setSize(40, 40);
        pauseButtonSprite.setPosition(40, viewport.getWorldHeight() - pauseButtonSprite.getHeight() - 10);

        peopleCounterButtonSprite = new Sprite(peopleCounterButton);
        peopleCounterButtonSprite.setSize(100, 40);
        peopleCounterButtonSprite.setPosition(
            (viewport.getWorldWidth() - peopleCounterButtonSprite.getWidth()) / 2,
            viewport.getWorldHeight() - peopleCounterButtonSprite.getHeight() - 10);

        pauseBackgroundSprite = new Sprite(pauseBackground);
        pauseBackgroundSprite.setSize(230, 330);
        pauseBackgroundSprite.setPosition(viewport.getWorldWidth() / 2 - 115, viewport.getWorldHeight() / 2 - 180);

        resumeButtonSprite = new Sprite(resumeButton);
        resumeButtonSprite.setSize(150, 40);
        resumeButtonSprite.setPosition(pauseBackgroundSprite.getX() + (pauseBackgroundSprite.getWidth() - resumeButtonSprite.getWidth()) / 2,
            pauseBackgroundSprite.getY() + (pauseBackgroundSprite.getHeight() - resumeButtonSprite.getHeight()) - 60);

        restartButtonSprite = new Sprite(restartButton);
        restartButtonSprite.setSize(150, 40);
        restartButtonSprite.setPosition(
            pauseBackgroundSprite.getX() + (pauseBackgroundSprite.getWidth() - restartButtonSprite.getWidth()) / 2,
            resumeButtonSprite.getY() - restartButtonSprite.getHeight() - 10);

        exitButtonSprite = new Sprite(exitButton);
        exitButtonSprite.setSize(150, 40);
        exitButtonSprite.setPosition(
            pauseBackgroundSprite.getX() + (pauseBackgroundSprite.getWidth() - exitButtonSprite.getWidth()) / 2,
            restartButtonSprite.getY() - exitButtonSprite.getHeight() - 10);

        playButtonSprite = new Sprite(playButton);
        playButtonSprite.setSize(150, 60);
        playButtonSprite.setPosition(viewport.getWorldWidth() / 2 - 75, viewport.getWorldHeight() / 2 - 30);

        obstacles = new Array<>();
        crackSprites = new Array<>();
        trafficIslandSprites = new Array<>();
        peopleSprites = new Array<>();

        updateObstacleSpawnDelay();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(viewport.getCamera().combined);
    }


    @Override
    public void render(float delta) {
        Gdx.app.log("Debug", "Touched at: " + touchPos.x + ", " + touchPos.y);
        if (ignoreFirstTouch) {
            if (!Gdx.input.isTouched()) {
                ignoreFirstTouch = false; // Once no touch is detected, allow input
            }
            return; // Skip processing while ignoring first touch
        }


        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (gameState != GameState.GAME_OVER && !isPaused) {
            stateTime += Gdx.graphics.getDeltaTime();
            npcStateTime += Gdx.graphics.getDeltaTime();
        }
        movementAndButtons();
        logic();
        draw();
    }


    private void movementAndButtons() {
        touchPos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPos);

        //Buttons
        if (Gdx.input.justTouched()) {
            //only allows the play button to work if the game has not started
            if (!gameStarted) {
                if (playButtonSprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                    gameStarted = true;
                    restartGame();
                }
                return;
            }

            if (isPaused || gameState == GameState.GAME_OVER) {
                if (resumeButtonSprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                    isPaused = false;
                    ignoreNextTouch = true;
                    return;
                }

                if (Gdx.input.justTouched()) {
                    float currentTime = Gdx.graphics.getDeltaTime();
                    if (currentTime - lastTouchTime > touchCooldown) {
                        lastTouchTime = currentTime;

                        if (restartButtonSprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                            restartGame();
                            isPaused = false;
                            ignoreNextTouch = true;
                        }
                    }
                }

                if (Gdx.input.justTouched()) {
                    if (exitButtonSprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                        game.setScreen(new FirstScreen(game));
                    }
                }

                return;
            }

            //if the pause button is clicked the game will be paused
            if (pauseButtonSprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                isPaused = true;
                return;
            }
        }

        if (ignoreNextTouch) {
            if (!Gdx.input.isTouched()) {
                ignoreNextTouch = false;
            }
            return;
        }

        //Player will not move if the game has not started or is paused
        if (!gameStarted || isPaused || gameState ==  GameState.GAME_OVER) return;

        if (Gdx.input.justTouched()) {
            touchStartPos.set(Gdx.input.getX(), Gdx.input.getY()); // Record the starting position of the touch
            viewport.unproject(touchStartPos); // Convert to world coordinates
            isSwiping = true; // Start tracking a potential swipe
            hasSwiped = false; // Reset swipe tracking for the new touch
        }

        if (Gdx.input.isTouched() && isSwiping) {
            touchEndPos.set(Gdx.input.getX(), Gdx.input.getY()); // Record the current position of the touch
            viewport.unproject(touchEndPos); // Convert to world coordinates

            // Determine the swipe threshold based on whether it's the first or second swipe
            float currentSwipeThreshold = isFirstSwipe ? initialSwipeThreshold : doubleSwipeThreshold;

            // Check if the swipe distance exceeds the threshold and no swipe has been processed yet
            float deltaX = touchEndPos.x - touchStartPos.x; // Horizontal distance of the swipe
            if (Math.abs(deltaX) >= currentSwipeThreshold && !hasSwiped) {
                int swipeDirection = (deltaX > 0) ? 1 : -1; // 1 = right, -1 = left

                // Check for double swipe
                if (swipeDirection == lastSwipeDirection && (Gdx.graphics.getDeltaTime() - lastSwipeTime) <= doubleSwipeTimeThreshold) {
                    // Double swipe detected
                    if (swipeDirection == 1) {
                        movePlayerRight(true); // Move two lanes to the right
                    } else if (swipeDirection == -1) {
                        movePlayerLeft(true); // Move two lanes to the left
                    }
                } else {
                    // Single swipe detected
                    if (swipeDirection == 1) {
                        movePlayerRight(false); // Move one lane to the right
                    } else if (swipeDirection == -1) {
                        movePlayerLeft(false); // Move one lane to the left
                    }
                }

                // Update last swipe time and direction
                lastSwipeTime = Gdx.graphics.getDeltaTime();
                lastSwipeDirection = swipeDirection;
                hasSwiped = true; // Mark the swipe as processed
                isFirstSwipe = !isFirstSwipe; // Toggle between first and second swipe
            }
        }

        if (!Gdx.input.isTouched()) {
            isSwiping = false; // Reset swipe tracking when the touch is released
            isFirstSwipe = true; // Reset to first swipe for the next touch
        }
    }

    private void movePlayerLeft(boolean isDoubleSwipe) {
        if (isDoubleSwipe && currentLane > 1) {
            // Move two lanes to the left
            targetLane = currentLane - 2;
            transitionSpeed = doubleSwipeTransitionSpeed; // Double the transition speed
        } else if (currentLane > 0) {
            // Move one lane to the left
            targetLane = currentLane - 1;
            transitionSpeed = normalTransitionSpeed; // Normal transition speed
        }

        if (targetLane != currentLane && !isTransitioning) {
            isTransitioning = true; // Start transition
            transitionProgress = 0f; // Reset transition progress
        }
    }

    private void movePlayerRight(boolean isDoubleSwipe) {
        if (isDoubleSwipe && currentLane < 1) {
            // Move two lanes to the right
            targetLane = currentLane + 2;
            transitionSpeed = doubleSwipeTransitionSpeed; // Double the transition speed
        } else if (currentLane < 2) {
            // Move one lane to the right
            targetLane = currentLane + 1;
            transitionSpeed = normalTransitionSpeed; // Normal transition speed
        }

        if (targetLane != currentLane && !isTransitioning) {
            isTransitioning = true; // Start transition
            transitionProgress = 0f; // Reset transition progress
        }
    }

    private void restartGame() {
        Gdx.app.log("Debug", "Restarting game...");

        gameStarted = true; // Ensure the game starts
        gameState = GameState.PLAYING; // Reset game state

        score = 0;
        peopleCounter = 0;
        isPaused = false;
        ignoreNextTouch = false;

        currentLane = 1; // Start in the middle lane
        targetLane = 1;
        isTransitioning = false;
        transitionProgress = 0f;
        playerSprite.setPosition(lanes[1], 75);

        scrollY = 0f;

        obstacles.clear();
        crackSprites.clear();
        trafficIslandSprites.clear();
        peopleSprites.clear();
        animatedPeopleMap.clear();

        currentObstacleScrollSpeed = obstacleBaseScrollSpeed;
        currentRoadScrollSpeed = roadBaseScrollSpeed;
        timeSinceLastSpeedIncrease = 0f;
        currentFrameDuration = 0.4f; // Reset frame duration
        playerAnimation.setFrameDuration(currentFrameDuration); // Update animation frame duration

        updateObstacleSpawnDelay();

        Gdx.app.log("Debug", "Game restarted successfully.");

    }

    private void updateObstacleSpawnDelay() {
        // Adjust spawn delay to maintain desired distance between obstacles
        obstacleSpawnDelay = (int) (desiredDistance / currentObstacleScrollSpeed);
    }

    private void spawnObstacle() {

        int numberOfLanesToSpawn = MathUtils.random(1, 2); // Number of lanes to spawn obstacles in
        // Randomly select lanes to spawn obstacles
        Array<Integer> lanesToSpawn = new Array<>();
        while (lanesToSpawn.size < numberOfLanesToSpawn) {
            int lane = MathUtils.random(0, lanes.length - 1);
            if (!lanesToSpawn.contains(lane, false)) {
                lanesToSpawn.add(lane);
            }
        }

        // Spawn obstacles in the selected lanes
        for (int lane : lanesToSpawn) {
            float x = lanes[lane];
            float y = viewport.getWorldHeight();

            // Randomly choose between cracks, traffic islands, and people
            int obstacleType = MathUtils.random(0, 2);
            switch (obstacleType) {
                case 0: // Crack
                    Texture selectedCrackTexture = MathUtils.randomBoolean() ? crack1 : (MathUtils.randomBoolean() ? crack2 : crack3);
                    Sprite crackSprite = new Sprite(selectedCrackTexture);
                    crackSprite.setSize(80, 80);
                    crackSprite.setPosition(x, y);
                    crackSprites.add(crackSprite);
                    break;
                case 1: // Traffic Island
                    Sprite trafficIslandSprite = new Sprite(trafficIsland);
                    trafficIslandSprite.setSize(80, 210);
                    trafficIslandSprite.setPosition(x, y);
                    trafficIslandSprites.add(trafficIslandSprite);
                    break;
                case 2: // People
                    boolean isAnimated = MathUtils.randomBoolean();
                    Texture selectedPeopleTexture = isAnimated ? people1 : people2;
                    Sprite peopleSprite = new Sprite(selectedPeopleTexture);
                    peopleSprite.setSize(80, 100);
                    peopleSprite.setPosition(x, y);
                    peopleSprites.add(peopleSprite);
                    animatedPeopleMap.put(peopleSprite, isAnimated);
                    break;
            }
        }
    }

    private void logic() {
        if (!gameStarted || isPaused || gameState == GameState.GAME_OVER) return;

        float deltaTime = Gdx.graphics.getDeltaTime(); // Get time since last frame

        if (isTransitioning) {
            transitionProgress += deltaTime * transitionSpeed; // Increase transition progress
            if (transitionProgress >= 1f) { // Transition complete
                transitionProgress = 1f;
                isTransitioning = false;
                currentLane = targetLane; // Update current lane
            }

            // Interpolate player position between current and target lane
            float startX = lanes[currentLane];
            float endX = lanes[targetLane];
            float playerX = startX + (endX - startX) * transitionProgress;
            playerSprite.setX(playerX);
        }

        // Increment score every 1 second
        scoreTimer += deltaTime;
        if (scoreTimer >= 0.1f) { // 1 second
            score++;
            scoreTimer -= 0.1f; // Reset timer but keep any overflow
        }

        // Increase speed every 3 seconds (both obstacle speed and animation speed)
        timeSinceLastSpeedIncrease += deltaTime;
        if (timeSinceLastSpeedIncrease >= 3f) { // Every 3 seconds
            // Increase obstacle speed
            if (currentObstacleScrollSpeed < obstacleMaxSpeed) {
                currentObstacleScrollSpeed += obstacleSpeedIncreaseAmount;
                updateObstacleSpawnDelay();
            }

            if (currentRoadScrollSpeed < roadMaxSpeed) {
                currentRoadScrollSpeed += roadSpeedIncreaseAmount;
            }

            // Decrease frame duration (speed up animation)
            if (currentFrameDuration > minFrameDuration) {
                currentFrameDuration -= frameDurationDecreaseRate;
                currentFrameDuration = Math.max(currentFrameDuration, minFrameDuration); // Ensure it doesn't go below min
                playerAnimation.setFrameDuration(currentFrameDuration); // Update animation frame duration
            }

            timeSinceLastSpeedIncrease = 0f; // Reset the timer
        }

        scrollY -= currentRoadScrollSpeed * deltaTime;
        if (scrollY <= -viewport.getWorldHeight()) {
            scrollY = 0;
        }

        // Spawn obstacles
        obstacleSpawnCounter++;
        if (obstacleSpawnCounter >= obstacleSpawnDelay) {
            spawnObstacle();
            obstacleSpawnCounter = 0;
        }

        // Update obstacle positions
        for (Sprite crackSprite : crackSprites) {
            crackSprite.translateY(-currentObstacleScrollSpeed);
            if (crackSprite.getY() + crackSprite.getHeight() < 0) {
                crackSprites.removeValue(crackSprite, true);
            }
        }

        for (Sprite trafficIslandSprite : trafficIslandSprites) {
            trafficIslandSprite.translateY(-currentObstacleScrollSpeed);
            if (trafficIslandSprite.getY() + trafficIslandSprite.getHeight() < 0) {
                trafficIslandSprites.removeValue(trafficIslandSprite, true);
            }
        }

        for (Sprite peopleSprite : peopleSprites) {
            peopleSprite.translateY(-currentObstacleScrollSpeed);
            if (peopleSprite.getY() + peopleSprite.getHeight() < 0) {
                peopleSprites.removeValue(peopleSprite, true);
            }
        }

        // Check for collision with obstacles using oval hitboxes
        for (Sprite crackSprite : crackSprites) {
            if (checkOvalCollision(playerSprite, crackSprite)) {
                gameState = GameState.GAME_OVER; // Trigger game over
                return;
            }
        }

        for (Sprite trafficIslandSprite : trafficIslandSprites) {
            if (checkOvalCollision(playerSprite, trafficIslandSprite)) {
                gameState = GameState.GAME_OVER; // Trigger game over
                return;
            }
        }

        // Check for collision with people using oval hitboxes
        for (int i = peopleSprites.size - 1; i >= 0; i--) {
            Sprite person = peopleSprites.get(i);
            if (checkOvalCollision(playerSprite, person)) {
                score += 50; // Additional points for collecting people
                peopleSprites.removeIndex(i);
                peopleCounter++;
            }
        }
    }

    private boolean checkOvalCollision(Sprite player, Sprite obstacle) {
        // Get the bounding rectangle of the player and obstacle
        float playerCenterX = player.getX() + player.getWidth() / 2;
        float playerCenterY = player.getY() + player.getHeight() / 2;
        float obstacleCenterX = obstacle.getX() + obstacle.getWidth() / 2;
        float obstacleCenterY = obstacle.getY() + obstacle.getHeight() / 2;

        // Calculate the distance between the centers of the player and obstacle
        float dx = playerCenterX - obstacleCenterX;
        float dy = playerCenterY - obstacleCenterY;

        // Calculate the radii of the ovals
        float playerRadiusX = player.getWidth() / 2;
        float playerRadiusY = player.getHeight() / 2;
        float obstacleRadiusX = obstacle.getWidth() / 2;
        float obstacleRadiusY = obstacle.getHeight() / 2;

        // Check if the distance is within the combined radii of the ovals
        return (dx * dx) / ((playerRadiusX + obstacleRadiusX) * (playerRadiusX + obstacleRadiusX)) +
            (dy * dy) / ((playerRadiusY + obstacleRadiusY) * (playerRadiusY + obstacleRadiusY)) <= 1;
    }

    private void draw() {
        batch.setProjectionMatrix(viewport.getCamera().combined);
        viewport.apply();

        batch.begin();

        float roadWidth = viewport.getWorldWidth();
        float roadHeight = viewport.getWorldHeight();

        batch.draw(road, 0, scrollY, roadWidth, roadHeight);
        batch.draw(road, 0, scrollY + roadHeight, roadWidth, roadHeight);

        if (!gameStarted) {
            playButtonSprite.draw(batch);
            batch.end();
            return;
        }

        for (Sprite crackSprite : crackSprites) {
            crackSprite.draw(batch);

        }

        for (Sprite trafficIslandSprite : trafficIslandSprites) {
            trafficIslandSprite.draw(batch);
        }

        for (Sprite peopleSprite : peopleSprites) {
            boolean isAnimated = animatedPeopleMap.get(peopleSprite); // Retrieve the flag
            TextureRegion currentNpcFrame;
            if (isAnimated) {
                // Use the second NPC animation
                currentNpcFrame = npcAnimation1.getKeyFrame(npcStateTime, true);
            } else {
                // Use the first NPC animation
                currentNpcFrame = npcAnimation2.getKeyFrame(npcStateTime, true);
            }
            batch.draw(currentNpcFrame, peopleSprite.getX(), peopleSprite.getY(), peopleSprite.getWidth(), peopleSprite.getHeight());
        }

        TextureRegion currentFrame = playerAnimation.getKeyFrame(stateTime, true);
        batch.draw(currentFrame, playerSprite.getX(), playerSprite.getY(), playerSprite.getWidth(), playerSprite.getHeight());

        pauseButtonSprite.draw(batch);
        peopleCounterButtonSprite.draw(batch);

        if (gameState == GameState.GAME_OVER) {
            pauseBackgroundSprite.draw(batch); // Reuse the pause background
            restartButtonSprite.draw(batch);
            exitButtonSprite.draw(batch);

            String gameOverText = "Game Over!";
            glyphLayout.setText(font, gameOverText);
            float gameOverTextWidth = glyphLayout.width;
            font.draw(batch, gameOverText, pauseBackgroundSprite.getX() + (pauseBackgroundSprite.getWidth() / 2 - gameOverTextWidth / 2), pauseBackgroundSprite.getY() + 270);

            String finalScoreText = "Score: " + score;
            glyphLayout.setText(font, finalScoreText);
            float scoreTextWidth = glyphLayout.width;
            font.draw(batch, finalScoreText, pauseBackgroundSprite.getX() + (pauseBackgroundSprite.getWidth() / 2 - scoreTextWidth / 2), pauseBackgroundSprite.getY() + 255);
        }

        if (isPaused) {
            pauseBackgroundSprite.draw(batch);
            resumeButtonSprite.draw(batch);
            restartButtonSprite.draw(batch);
            exitButtonSprite.draw(batch);
        }

        if (gameStarted) {
            String scoreText = "Score: " + score;
            glyphLayout.setText(font, scoreText);
            float scoreTextHeight = glyphLayout.height;
            font.draw(batch, scoreText, 210, viewport.getWorldHeight() - scoreTextHeight - 12);

            String peopleCountText = String.valueOf(peopleCounter);
            glyphLayout.setText(font, peopleCountText);
            float peopleCountTextWidth = glyphLayout.width;
            float peopleCountTextHeight = glyphLayout.height;
            font.draw(batch, peopleCountText, peopleCounterButtonSprite.getX() + (peopleCounterButtonSprite.getWidth() / 2 - peopleCountTextWidth / 2) + 15, peopleCounterButtonSprite.getY() + (peopleCounterButtonSprite.getHeight() / 2 + peopleCountTextWidth / 2));
        }

        batch.end();

    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        font.dispose();
        road.dispose();
        player.dispose();
        people1.dispose();
        people2.dispose();
        crack1.dispose();
        crack2.dispose();
        crack3.dispose();
        trafficIsland.dispose();
    }
}
