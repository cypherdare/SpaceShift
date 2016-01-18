package com.cyphercove.dayinspace;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import static com.cyphercove.dayinspace.Constants.*;
import static com.cyphercove.dayinspace.EntityAppearanceAction.*;
import static com.cyphercove.dayinspace.TargetAction.*;
import static com.cyphercove.dayinspace.GamePlaySceneDamageAction.*;

/**
 * Details pertinent to a round of the game on one board. Not saved.
 */
public class GamePlayScene implements Disposable{

    private enum State {
        Dialogue,
        PlayerInput,
        Animating
    }

    enum AirlockState {
        Static, Open, Closed
    }

    ObjectSet<Disposable> disposables = new ObjectSet<>();

    GameMain main;
    Assets assets;
    SpriteBatch batch;
    MusicManager musicManager;

    int inputLock;
    Stage stage;
    Stage uiStage;
    InputMultiplexer inputMultiplexer;
    State state;
    CurvedViewport viewport;
    Camera camera;
    FrameBuffer buffer;
    FrameBuffer lightBuffer;
    FrameBuffer gameSceneBuffer;
    FrameBuffer postProcessBuffer;
    TextureRegion bufferRegion;
    ScrollPane dialogueScrollPane;
    String[] currentDialogue;
    FaceMood[] currentDialogueMoods;
    FaceMood currentDialogueMood;
    int currentDialogueNextIndex;
    Table speechBoxTable;
    SpriteWidget faceWidget;
    float speechBoxX;
    float speechBoxYShown;
    float speechBoxYHidden;
    boolean speechBoxActive;
    SpeakingLabel dialogueLabel;
    Button doneButton;
    Table healthTable;
    Image[] heartImages;
    Image[] stepImages;
    float damageAnimationIntensity;

    boolean fxaa = true;

    private static final Matrix4 IDT = new Matrix4();

    Story story;
    Board board;
    Border border;
    int heartsLeft;
    int movesLeft;
    int movesPerTurn;
    boolean switchPressed;
    boolean secondarySwitchPressed;
    boolean switchDialogueReady;
    boolean secondarySwitchDialogueReady;
    boolean gameOver;
    boolean exitDoorLocked;
    boolean roundOver;
    float exitDoorOpenTime;
    AirlockState airlockState;
    float airlockSpriteTime;
    boolean gravityOn;
    boolean lightsOn;
    int stepsTaken;
    int enemiesDestroyed;
    boolean hasPickup;
    float elapsed;
    float gravityLadderAnimationTime;
    float gravityStateTime;
    float lightStateTime;
    float backgroundOffset;
    boolean showBrokenGlass;
    final Coordinate playerPosition = new Coordinate();
    private final Coordinate tmpC = new Coordinate();
    PlayerRepresentation playerRep;
    final ObjectMap<Trap, TrapRepresentation> trapRepresentations = new ObjectMap<>();
    final ObjectMap<Enemy, EnemyRepresentation> enemyRepresentations = new ObjectMap<>();
    Group enemyActorGroup; //used to move player in front of and behind enemies.
    Button buttonRight, buttonLeft, buttonUp, buttonDown;
    Button[] movementButtons;
    TextureRegion backgroundRegion;
    private final Color ambientColor = new Color();
    Array<TextureRegionActor> monchuPieceActors = new Array<>();

    public GamePlayScene(GameMain main, Assets assets, MusicManager musicManager, SpriteBatch batch){
        this.main = main;
        this.assets = assets;
        this.musicManager = musicManager;
        this.batch = batch;

        border = new Border(assets);

        viewport = new CurvedViewport();
        camera = viewport.getCamera();
        stage = new Stage(viewport, batch);
        disposables.add(stage);

        uiStage = new Stage(new PixelMultipleViewport(200, 350), batch);
        disposables.add(uiStage);
        setUpUI();

        backgroundRegion = new TextureRegion(assets.backgroundTexture);

        playerRep = new PlayerRepresentation(assets.sprites);

        movementButtons = new Button[5];
        movementButtons[0] = buttonUp = newButton();
        movementButtons[1] = buttonDown = newButton();
        movementButtons[2] = buttonLeft = newButton();
        movementButtons[3] = buttonRight = newButton();
        movementButtons[4] = doneButton = new Button(assets.skin, "done");
        doneButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                finishTurn();
            }
        });

        inputMultiplexer = new InputMultiplexer(inputAdapter, uiStage, stage);
    }

    private Button newButton (){
        Button button = new Button(assets.skin, "default");
        button.setSize(SCALE, SCALE);
        button.addListener(buttonListener);
        button.getClickListener().setTapSquareSize(0.5f);
        return button;
    }

    private void setUpUI (){
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.pad(UI_SCREEN_PADDING);

        Table topRowTable = new Table();

        healthTable = new Table(assets.skin);
        healthTable.setBackground("healthUI");
        heartImages = new Image[MAX_HEARTS];
        for (int i = 0; i < heartImages.length; i++) {
            Image image = new Image(assets.skin, "heart");
            heartImages[i] = image;
            healthTable.add(image).pad(2);
        }
        healthTable.row();
        stepImages = new Image[MAX_STEPS_PER_TURN];
        for (int i = 0; i < stepImages.length; i++) {
            Image image = new Image(assets.skin, "step-pip");
            stepImages[i] = image;
            healthTable.add(image).pad(2);
        }

        topRowTable.add(healthTable).center().top().expandX();

        rootTable.add(topRowTable).expand().top().fillX().colspan(3).row();

        uiStage.addActor(rootTable);

        speechBoxTable = new Table(assets.skin);
        speechBoxTable.setSize(SPEECH_BOX_WIDTH, DIALOGUE_LABEL_HEIGHT);
        speechBoxTable.setBackground("speechboxTrans");

        faceWidget = new SpriteWidget(assets.faceMoods.get("default").idleSprite);
        speechBoxTable.add(faceWidget).center();

        Table dialogueTable = new Table(assets.skin);
        dialogueLabel = new SpeakingLabel(assets, "", assets.skin, "default", ROUND_DIALOGUE_SPEED);
        dialogueLabel.setWrap(true);
        dialogueTable.add(dialogueLabel).width(DIALOGUE_LABEL_WIDTH);
        dialogueScrollPane = new ScrollPane(dialogueLabel);
        speechBoxTable.add(dialogueScrollPane).width(DIALOGUE_LABEL_WIDTH).height(DIALOGUE_LABEL_HEIGHT).fill();

        speechBoxTable.invalidate();
        uiStage.addActor(speechBoxTable);
    }

    private void onBackPressed (){
        Util.log("escape or back pressed");
    }

    public void resize (int width, int height){
        uiStage.getViewport().update(width, height, true);
        Util.log("ui viewport width: " + uiStage.getViewport().getWorldWidth() + " , height: " + uiStage.getViewport().getWorldHeight());
        speechBoxX = uiStage.getCamera().position.x - speechBoxTable.getWidth() / 2;
        speechBoxYShown = UI_SCREEN_PADDING;
        speechBoxYHidden = uiStage.getCamera().position.y - uiStage.getCamera().viewportHeight / 2 - speechBoxTable.getHeight();
        speechBoxTable.setPosition(speechBoxX, speechBoxActive ? speechBoxYShown : speechBoxYHidden);

        //The source width and height are the initially rendered game world.
        //The world variables define the coordinates as seen after curvature manipulation.
        //The source top and source bottom are where the top and bottom of the rendered game world map to at the extremes,
        //so source top maps to world top, while source bottom maps to the bottom corners of the world, to ensure
        //complete coverage of the world view.
        int sourceWidth = (int)((12 * (float)width / (float)height) * SCALE); //Experimentally determined ratio to ensure game board viewable in both 4:3 and 16:9
        int sourceHeight = (int)((Board.HEIGHT + 3.6f) * SCALE);
        if (sourceWidth % 2 != 0) sourceWidth++; //ensure even dimensions
        if (sourceHeight % 2 != 0) sourceHeight++; //ensure even dimensions
        float viewableCornerAngle = 15f * MathUtils.degRad; //angle at top corners of screen (world rectangle).
        float worldAndSourceTop = -400;
        float worldLeft = -worldAndSourceTop * (float)Math.tan(viewableCornerAngle);
        float worldRight = -worldLeft;
        float sourceBottom = worldAndSourceTop - sourceHeight;
        float worldBottom = -(float)Math.sqrt(sourceBottom * sourceBottom - worldLeft * worldLeft);

        assets.curvatureShader.setUniformFloat("u_sourceTop", worldAndSourceTop);
        assets.curvatureShader.setUniformFloat("u_sourceBottom", sourceBottom);
        assets.curvatureShader.setUniformFloat("u_cornerAngle", viewableCornerAngle);
        assets.curvatureDamageShader.setUniformFloat("u_sourceTop", worldAndSourceTop);
        assets.curvatureDamageShader.setUniformFloat("u_sourceBottom", sourceBottom);
        assets.curvatureDamageShader.setUniformFloat("u_cornerAngle", viewableCornerAngle);

        if (buffer != null) {
            buffer.dispose();
            disposables.remove(buffer);
        }
        buffer = makeFrameBuffer(sourceWidth, sourceHeight);
        disposables.add(buffer);
        bufferRegion = new TextureRegion(buffer.getColorBufferTexture());
        bufferRegion.setRegion(worldLeft, worldBottom, worldRight, worldAndSourceTop);

        if (lightBuffer != null){
            lightBuffer.dispose();
            disposables.remove(lightBuffer);
        }
        lightBuffer = makeFrameBuffer(sourceWidth, sourceHeight);
        disposables.add(lightBuffer);

        viewport.setCurvatureParameters(worldAndSourceTop, worldBottom, worldLeft, worldRight, viewableCornerAngle, sourceBottom);
        viewport.setWorldSize(sourceWidth, sourceHeight);
        viewport.update(width, height, false);
        Util.resetViewport();

        camera.position.set(Board.WIDTH / 2f * SCALE, Board.HEIGHT / 2f * SCALE, 0);
        camera.update();

        if (gameSceneBuffer != null){
            gameSceneBuffer.dispose();
            disposables.remove(gameSceneBuffer);
        }
        gameSceneBuffer = makeFrameBuffer(width, height);
        disposables.add(gameSceneBuffer);

        if (postProcessBuffer != null){
            postProcessBuffer.dispose();
            disposables.remove(postProcessBuffer);
        }
        postProcessBuffer = makeFrameBuffer(width, height);
        disposables.add(postProcessBuffer);

        assets.glassShader.setUniformFloat("u_resolution", width, height);

        assets.fxaaShader.setUniformFloat("u_invResolution", 1f / (float)width, 1f / (float)height);
    }

    private FrameBuffer makeFrameBuffer (int width, int height){
        FrameBuffer frameBuffer;
        try {
            frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        } catch (IllegalStateException e){
            frameBuffer = new FrameBuffer(Pixmap.Format.RGB565, width, height, false);
        }
        frameBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return frameBuffer;
    }

    public void initialize (Story story, Board board, int heartsLeft, int movesPerTurn){
        this.story = story;
        this.board = board;
        this.heartsLeft = heartsLeft;
        this.movesPerTurn = movesPerTurn;
        movesLeft = movesPerTurn;
        exitDoorLocked = board.exitStartsDoorLocked;
        roundOver = false;
        lightsOn = board.lightsStartOn;
        gravityOn = board.gravityStartsOn;
        gravityLadderAnimationTime = 1000; //don't want to see animation at start of level.
        gravityStateTime = 1000;//don't want to see animation at start of level.
        lightStateTime = 1000;//don't want to see animation at start of level.
        exitDoorOpenTime = exitDoorLocked ? 0 : 1000; //don't want to see animation at start of level.
        stepsTaken = 0;
        enemiesDestroyed = 0;
        switchPressed = false;
        secondarySwitchPressed = false;
        switchDialogueReady = false;
        secondarySwitchDialogueReady = false;
        speechBoxActive = false;
        speechBoxTable.setY(speechBoxYHidden);
        hasPickup = board.pickupType != null;
        inputLock = 0;
        gameOver = false;
        showBrokenGlass = false;
        airlockSpriteTime = 0;
        airlockState = AirlockState.Static;
        syncHealthUI();
        stage.clear();
        damageAnimationIntensity = 0;
        board.reset();
        elapsed = 0;

        enemyActorGroup = new Group();

        for (TextureRegionActor actor : monchuPieceActors) Pools.free(actor);
        monchuPieceActors.clear();
        if (board.airlockCoordinate != null) { //boss level
            for (TextureRegion region : assets.monchuPieces){
                TextureRegionActor actor = Pools.obtain(TextureRegionActor.class);
                actor.clear();
                actor.setRegion(region);
                actor.setScale(1);
                actor.setColor(Color.WHITE);
                actor.setVisible(false);
                enemyActorGroup.addActor(actor);
                monchuPieceActors.add(actor);
            }
        }

        enemyRepresentations.clear();
        for (Enemy enemy : board.enemies){
            EnemyRepresentation enemyRepresentation = new EnemyRepresentation(assets.sprites);
            enemyRepresentation.setPosition(enemy.coord.x * SCALE, enemy.coord.y * SCALE);
            enemyRepresentation.setColor(enemy.type == EnemyType.Chupoof ? Color.YELLOW : enemy.type == EnemyType.Toughie ? Color.MAGENTA : Color.LIME);
            enemyRepresentations.put(enemy, enemyRepresentation);
            enemyRepresentation.setEnemy(enemy);
            enemyRepresentation.setAppearance(Movement.Idle, gravityOn, false, false);
            enemyRepresentation.setFaceLeft(false);
            enemyActorGroup.addActor(enemyRepresentation);
        }
        stage.addActor(enemyActorGroup);

        if (board.musicTrack != null){
            musicManager.setNextTrack(board.musicTrack, NEW_STAGE_MUSIC_CROSS_FADE_TIME);
        }

        trapRepresentations.clear();
        for (Trap trap : board.traps){
            TrapRepresentation trapRepresentation = new TrapRepresentation();
            trapRepresentation.setPosition(trap.coord.x * SCALE, trap.coord.y * SCALE);
            trapRepresentation.setColor(trap.shouldWarn() ? TrapRepresentation.SPARKS : TrapRepresentation.NO_DRAW);
            trapRepresentations.put(trap, trapRepresentation);
            stage.addActor(trapRepresentation);
        }

        if (board.firstRoom)
            playerPosition.set(Board.WIDTH / 2, 0);
        else
            playerPosition.set(Board.WIDTH - 1, board.entryDoorY);
        playerRep.setPosition(playerPosition.x * SCALE, playerPosition.y * SCALE);
        playerRep.setAppearance(Movement.Idle, gravityOn, false, false);
        playerRep.setFaceLeft(true);
        stage.addActor(playerRep);

        for (Button button : movementButtons){
            stage.addActor(button);
        }

        if (board.entryDialogue != null) {
            beginDialogue(board.entryDialogue, board.entryDialogueMoods);
            if (board.entrySound != null)
                stage.addAction(sound(board.entrySound, board.entrySoundDelay));
        }
        else
            preparePlayerMove();
    }

    private void enemiesToFront (){
        enemyActorGroup.setZIndex(1000);
    }

    private void playerToFront (){
        if (board.airlockCoordinate != null) return; //boss level exception: player never in front.
        playerRep.setZIndex(1000);
    }

    public InputProcessor getInputProcessor(){
        return inputMultiplexer;
    }

    public void render (float delta, boolean fadeInComplete){

        // Updates -------------------------
        elapsed += delta;
        gravityLadderAnimationTime += delta;
        gravityStateTime += delta;
        lightStateTime += delta;
        airlockSpriteTime += delta;
        stage.act(delta);
        if (state == State.Dialogue)
            dialogueScrollPane.setScrollY(dialogueScrollPane.getMaxY());
        if (!dialogueLabel.isSpeaking()){
            faceWidget.set(currentDialogueMood == null ? assets.faceMoods.get("default").idleSprite : currentDialogueMood.idleSprite);
        }
        uiStage.act(fadeInComplete ? delta : 0);
        float backgroundVelocity = BACKGROUND_FULL_VELOCITY * (gravityOn ? Interpolation.fade.apply(0, 1, gravityStateTime / GRAVITY_CHANGE_TIME) :
                Interpolation.fade.apply(1, 0, gravityStateTime / GRAVITY_CHANGE_TIME));
        backgroundOffset += backgroundVelocity * delta;
        backgroundRegion.setRegion((int)backgroundOffset, 0, (int)viewport.getWorldWidth(), backgroundRegion.getTexture().getHeight());
        exitDoorOpenTime = exitDoorLocked ? 0 : exitDoorOpenTime + delta;

        //Only need to draw border in front when player's walking through door. Otherwise don't want it blocking buttons or entities
        boolean drawBorderForegroundBehindStage = !roundOver;

        //LIGHT BUFFER ------------------------------

        lightBuffer.begin();
        if (lightsOn) {
            ambientColor.set(DIM_AMBIENT_COLOR).lerp(Color.WHITE, Interpolation.fade.apply(lightStateTime / LIGHT_CHANGE_TIME));
        } else {
            ambientColor.set(Color.WHITE).lerp(DIM_AMBIENT_COLOR, Interpolation.fade.apply(lightStateTime / LIGHT_CHANGE_TIME));
        }

        Gdx.gl.glClearColor(ambientColor.r, ambientColor.g, ambientColor.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 1) Window light sources -----------------
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(Color.WHITE);
        board.drawRearLight(assets, batch, gravityLadderAnimationTime, elapsed, switchPressed, secondarySwitchPressed);
        batch.end();

        // 2) Mask silhouettes against windows --------
        assets.solidColorShader.setUniformFloat("u_color", ambientColor.r, ambientColor.g, ambientColor.b);
        batch.setShader(assets.solidColorShader.getProgram());
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        stage.draw();

        // 3) Front light sources ---------------------
        batch.begin();
        batch.setShader(null);
        batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
        board.drawFrontLight(assets, batch, gravityLadderAnimationTime, switchPressed, secondarySwitchPressed);
        border.drawLight(batch, board.exitDoorY * SCALE, exitDoorOpenTime, board.entryDoorY * SCALE);
        for (TrapRepresentation trapRepresentation : trapRepresentations.values()){
            trapRepresentation.drawLight(batch, assets);
        }
        for (EnemyRepresentation enemyRepresentation : enemyRepresentations.values()){
            enemyRepresentation.drawLight(batch, assets);
        }
        batch.end();

        lightBuffer.end();

        //MAIN BUFFER ------------------------------

        buffer.begin();
        Gdx.gl.glClearColor(0.3f, 0.075f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.begin();

        // 1) Background - opaque (no blend)
        batch.setProjectionMatrix(camera.combined);
        batch.disableBlending();
        batch.setColor(Color.WHITE);
        batch.draw(backgroundRegion, camera.position.x - camera.viewportWidth / 2, camera.position.y - backgroundRegion.getRegionHeight() / 2,
                camera.viewportWidth, backgroundRegion.getRegionHeight());

        // 2) Tiles and scenery - standard projection and blending.
        batch.setProjectionMatrix(camera.combined);
        batch.enableBlending();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        border.drawBackground(batch, board.exitDoorY * SCALE, exitDoorOpenTime, board.entryDoorY * SCALE);
        batch.setColor(Color.WHITE);
        board.draw(assets, batch, elapsed, gravityLadderAnimationTime, airlockSpriteTime, gravityOn, switchPressed,
                secondarySwitchPressed, airlockState, hasPickup);
        if (drawBorderForegroundBehindStage){
            border.drawForeground(batch, board.exitDoorY * SCALE, exitDoorOpenTime);
        }

        // 3) The stage draws the moveable game objects (player and enemies).
        batch.end();
        stage.draw();
        batch.begin();

        // 4) Scenery in front of moveable game objects
        if (!drawBorderForegroundBehindStage) {
            border.drawForeground(batch, board.exitDoorY * SCALE, exitDoorOpenTime);
        }

        // 5) Light layer - Mutliplitive blending, identity matrix for simplicity
        batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_COLOR);
        batch.setProjectionMatrix(IDT);
        batch.setColor(Color.WHITE);
        batch.draw(lightBuffer.getColorBufferTexture(), -1, 1, 2, -2);

        // 6) Glow layer - pre-multiplied alpha, linear, standard projection
        batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setProjectionMatrix(camera.combined);
        border.drawGlow(batch, board.exitDoorY * SCALE, exitDoorOpenTime, board.entryDoorY * SCALE);
        board.drawGlow(assets, batch, gravityLadderAnimationTime, switchPressed, secondarySwitchPressed);
        batch.setColor(Color.WHITE);
        for (TrapRepresentation trapRepresentation : trapRepresentations.values()){
            trapRepresentation.drawLinearGlow(batch, assets);
        }
        for (EnemyRepresentation enemyRepresentation : enemyRepresentations.values()){
            enemyRepresentation.drawGlow(batch, assets);
        }

        // 7) Glow layer - additive blending, nearest, standard projection
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(Color.WHITE);
        for (TrapRepresentation trapRepresentation : trapRepresentations.values()){
            trapRepresentation.drawAdditiveGlow(batch, assets);
        }
        batch.end();

        buffer.end();

        Util.resetViewport();

        boolean fxaa = this.fxaa;
        boolean showBrokenGlass = this.showBrokenGlass;
        if (fxaa || showBrokenGlass){
            gameSceneBuffer.begin();
            Gdx.gl.glClearColor(0.075f, 0.075f, 0.075f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }

        batch.setProjectionMatrix(IDT);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        if (damageAnimationIntensity > 0){
            assets.curvatureDamageShader.setUniformFloat("u_damageIntensity", damageAnimationIntensity);
            batch.setShader(assets.curvatureDamageShader.getProgram());
        } else {
            batch.setShader(assets.curvatureShader.getProgram());

        }
        batch.begin();
        batch.draw(bufferRegion, -1, 1, 2, -2);
        batch.end();

        if (fxaa || showBrokenGlass){
            gameSceneBuffer.end();
        }

        if (fxaa && showBrokenGlass){
            postProcessBuffer.begin();
            Gdx.gl.glClearColor(0.075f, 0.075f, 0.075f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }

        if (fxaa) {
            batch.setShader(assets.fxaaShader.getProgram());
            batch.begin();
            batch.draw(gameSceneBuffer.getColorBufferTexture(), -1, 1, 2, -2);
            batch.end();
        }

        if (fxaa && showBrokenGlass){
            postProcessBuffer.end();
        }

        if (showBrokenGlass){
            assets.brokenGlassTexture.bind(1);
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
            uiStage.getViewport().apply();
            Camera uiStageCam = uiStage.getCamera();
            batch.setProjectionMatrix(uiStageCam.combined);
            batch.begin();
            batch.setShader(assets.glassShader.getProgram());
            {
                Texture tex = assets.brokenGlassTexture; //since texture 0 varies, bind with it, but we're using the UV's for the broken glass
                FrameBuffer sourceBuffer = fxaa ? postProcessBuffer : gameSceneBuffer;
                batch.draw(sourceBuffer.getColorBufferTexture(), uiStageCam.position.x - tex.getWidth() / 2, uiStageCam.position.y - tex.getHeight() / 2, tex.getWidth(), tex.getHeight());
            }
            batch.end();
        }

        batch.setShader(null);

        uiStage.draw();
    }

    private void beginDialogue(String[] currentDialogue, FaceMood[] currentDialogueMoods){
        state = State.Dialogue;
        this.currentDialogue = currentDialogue;
        this.currentDialogueMoods = currentDialogueMoods;
        currentDialogueNextIndex = 0;
        dialogueLabel.setColor(gameOver ? Color.RED : Color.GREEN);
        faceWidget.setColor(gameOver ? Color.RED : Color.GREEN);
        hideMovementButtons();
        continueDialogue();
    }

    private void continueDialogue(){
        if (currentDialogue != null && currentDialogueNextIndex < currentDialogue.length){
            speechBoxActive = true;
            boolean needPresent = speechBoxTable.getY() != speechBoxYShown;
            dialogueLabel.setText(currentDialogue[currentDialogueNextIndex], needPresent ? DIALOGUE_PRESENT_DURATION : 0);
            currentDialogueMood = currentDialogueMoods == null ? assets.faceMoods.get("default") : currentDialogueMoods[currentDialogueNextIndex];
            currentDialogueNextIndex++;
            if (needPresent) {
                faceWidget.set(currentDialogueMood.idleSprite);
                speechBoxTable.addAction(
                        sequence(
                                moveTo(speechBoxX, speechBoxYShown, DIALOGUE_PRESENT_DURATION),
                                run(new Runnable(){public void run(){faceWidget.set(currentDialogueMood.speakingSprite);}})
                ));
            } else {
                faceWidget.set(currentDialogueMood.speakingSprite);
            }
            return;
        }

        currentDialogue = null;
        if (gameOver){
            main.onGameOver();
        } else {
            speechBoxActive = false;
            uiStage.addAction(sequence(
                    target(speechBoxTable, moveTo(speechBoxX, speechBoxYHidden, DIALOGUE_HIDE_DURATION)),
                    run(new Runnable(){ public void run (){
                        preparePlayerMove();
                    } })
            ));
        }

    }

    private void preparePlayerMove (){
        playerToFront();
        if (switchDialogueReady && board.switchDialogue != null){
            switchDialogueReady = false;
            beginDialogue(board.switchDialogue, board.switchDialogueMoods);
            return;
        }
        if (secondarySwitchDialogueReady && board.secondarySwitchDialogue != null){
            secondarySwitchDialogueReady = false;
            beginDialogue(board.secondarySwitchDialogue, board.secondarySwitchDialogueMoods);
            return;
        }
        state = State.PlayerInput;
        updateButton(buttonDown, playerPosition.x, playerPosition.y - 1);
        updateButton(buttonUp, playerPosition.x, playerPosition.y + 1);
        updateButton(buttonLeft, playerPosition.x - 1, playerPosition.y);
        updateButton(buttonRight, playerPosition.x + 1, playerPosition.y);
        doneButton.setVisible(true);
        int doneButtonX = (playerPosition.x > Board.WIDTH - 2) ?
                playerPosition.x * SCALE - 2 - (int)doneButton.getWidth() :
                (playerPosition.x + 1) * SCALE + 2;
        doneButton.setPosition(doneButtonX, playerPosition.y * SCALE - 2 - doneButton.getHeight());
        syncHealthUI();
    }

    private void hideMovementButtons(){
        doneButton.setVisible(false);
        for (Button button : movementButtons){
            button.setVisible(false);
        }
    }

    private void executePlayerMove(Button button){
        stepsTaken++;
        movesLeft--;
        Movement movement;
        tmpC.set(playerPosition);
        if (button == buttonUp) {
            tmpC.y++;
            movement = Movement.Up;
        } else if (button == buttonDown) {
            tmpC.y--;
            movement = Movement.Down;
        } else if (button == buttonLeft) {
            tmpC.x--;
            movement = Movement.Left;
        } else {
            tmpC.x++;
            movement = Movement.Right;
        }

        hideMovementButtons();

        boolean bounce = false;
        boolean attacking = false;
        ParallelAction firstParallel = parallel();
        SequenceAction fullSequence = sequence(firstParallel);
        firstParallel.addAction(sound(gravityOn ? "walk" : "float"));

        for (Enemy enemy : board.enemies){
            if (enemy.isDead())
                continue;
            if (enemy.coord.equals(tmpC)){
                attacking = true;
                enemy.hurt();
                if (enemy.shouldDie()){
                    enemiesDestroyed++;
                    enemy.die();
                    firstParallel.addAction(generateEnemyDieAction(enemy, board.ladders[tmpC.x][tmpC.y]));
                } else {
                    bounce = true;
                    firstParallel.addAction(generateEnemyHurtAction(enemy, board.ladders[tmpC.x][tmpC.y]));
                }
                break; //only one enemy can occupy the space
            }
        }

        state = State.Animating;

        final boolean gravityOnAtMoveStart = gravityOn;
        SequenceAction playerRepSequence =
                sequence(parallel(
                        moveTo(tmpC.x * SCALE, tmpC.y * SCALE, PLAYER_STEP_TIME, Interpolation.fade),
                        entityAppearance(movement, gravityOnAtMoveStart, attacking && !gravityOnAtMoveStart, board.ladders[tmpC.x][tmpC.y])
                ));
        if (attacking && gravityOnAtMoveStart){ //attack animation when on ground comes after the move
            playerRepSequence.addAction(
                    entityAppearance(movement, gravityOnAtMoveStart, attacking, board.ladders[tmpC.x][tmpC.y])
                    );
            playerRepSequence.addAction(delay(PLAYER_ATTACK_ANIMATION_TIME));
        }
        if (bounce){
            playerRepSequence.addAction(
                    entityAppearance(movement, gravityOnAtMoveStart, false, board.ladders[tmpC.x][tmpC.y])
            );
            playerRepSequence.addAction(
                    moveTo(playerPosition.x * SCALE, playerPosition.y * SCALE, PLAYER_STEP_TIME, Interpolation.fade));
        } else {
            playerPosition.set(tmpC);

            if (!switchPressed && board.zwitch != null && playerPosition.equals(board.zwitch)){
                switchDialogueReady = true;
                fullSequence.addAction(generateSwitchPressAction());
            } else if (!secondarySwitchPressed && board.zwitch2 != null && playerPosition.equals(board.zwitch2)){
                secondarySwitchDialogueReady = true;
                fullSequence.addAction(generateSecondarySwitchPressAction());
            }
        }
        boolean endsOnLadder = gravityOn && playerPosition.y != 0 && board.ladders[playerPosition.x][playerPosition.y];
        playerRepSequence.addAction(entityAppearance(Movement.Idle, gravityOnAtMoveStart, false, endsOnLadder));
        firstParallel.addAction(target(playerRep, playerRepSequence));

        if (hasPickup && board.pickupCoordinate.equals(tmpC)){
            fullSequence.addAction(generatePickupAction());
        }

        if (!exitDoorLocked && playerPosition.equals(0, board.exitDoorY)) {
            //end round
            roundOver = true;
            fullSequence.addAction(generateEndRoundAction());
        } else {
            //continue round
            fullSequence.addAction(run(new Runnable() {public void run() { finishMove(); } }));
        }

        stage.addAction(fullSequence);
    }

    private Action generateSwitchPressAction (){
        SequenceAction action = sequence(run(new Runnable() {
            @Override
            public void run() {
                switchPressed = true;
                Gdx.app.log("Round", "Switch pressed: " + board.switchAction.toString());
            }
        }));
        switch (board.switchAction){
            case UnlockExit:
                exitDoorLocked = false;
                action.addAction(sound("switch"));
                break;
            case DimLights:
                action.addAction(addAction(sequence(
                        delay(LIGHT_DIM_DELAY_TIME),
                        parallel(
                                sound("comp"),
                                run(new Runnable() {
                                    public void run() {
                                        lightStateTime = 0;
                                        lightsOn = false;
                                    }
                                }))
                        )
                ));
                break;
            case TurnGravityOff:
                gravityLadderAnimationTime = -1000; //prevent ladder image change until ready
                gravityStateTime = 0;
                gravityOn = false;
                ParallelAction parallelAction = parallel(
                        run(new Runnable() {
                            public void run() {
                                gravityLadderAnimationTime = 0;
                            }}),
                        sound("grav_off")
                );
                parallelAction.addAction(entityAppearance(playerRep, Movement.Idle, false, false, false));
                for (EnemyRepresentation enemyRepresentation : enemyRepresentations.values()){
                    parallelAction.addAction(entityAppearance(enemyRepresentation, Movement.Idle, false, false, false));
                }
                action.addAction(parallelAction);
                break;
            case TurnGravityOn:
                gravityOn = true;
                gravityLadderAnimationTime = -1000; //prevent ladder image change until ready
                gravityStateTime = 0;
                Enemy boss = board.enemies.first();
                while (boss.coord.y > 0){
                    if (board.obstacles[boss.coord.x][boss.coord.y - 1])
                        break;
                    boss.coord.y--;
                }
                EnemyRepresentation bossRep = enemyRepresentations.get(boss);
                action.addAction(sequence(
                        parallel(
                                sound("switch"),
                                sound("grav_on")
                        ),
                        delay(0.5f),
                        parallel(
                                run(new Runnable() {
                                    public void run() {
                                        gravityLadderAnimationTime = 0;
                                    }
                                }),
                                sequence(delay(0.3f), entityAppearance(playerRep, Movement.Idle, true, false, true)),
                                entityAppearance(bossRep, Movement.Hurt, false, false, false),
                                target(bossRep, moveTo(boss.coord.x * SCALE, boss.coord.y * SCALE, 1.2f, Interpolation.pow4In))
                            ),
                        parallel(
                                entityAppearance(bossRep, Movement.Idle, true, false, false),
                                sound("mon_move")
                        )
                ));
                break;
            case EndGame:
                action.addAction(generateEndGameAction());
                break;
        }

        return action;
    }

    //only for the boss.
    private Action generateSecondarySwitchPressAction (){
        Enemy boss = board.enemies.first();
        EnemyRepresentation bossRep = enemyRepresentations.get(boss);
        boss.die();
        bossRep.setVisible(false);
        airlockState = AirlockState.Open;
        airlockSpriteTime = 0;

        musicManager.setNextTrack(MusicTrack.Ambient, 5);

        int airlockTargetX = (board.airlockCoordinate.x + 1) * SCALE;
        int airlockTargetY = (board.airlockCoordinate.y + 1) * SCALE;

        float delay = 2.3f;
        float delayAdder = 0.5f;
        for (TextureRegionActor actor : monchuPieceActors) {
            actor.setVisible(true);
            actor.setPosition(bossRep.getX() + SCALE / 2, bossRep.getY() + SCALE / 2);
            actor.addAction(sequence(
                    delay((delay += (delayAdder *= 0.9f))),
                    parallel(
                            moveTo(airlockTargetX, airlockTargetY, 0.8f, Interpolation.pow2In),
                            sequence(delay(0.7f), scaleTo(0, 0, 0.35f, Interpolation.linear))
                    )
            ));
        }

        Action action = sequence(
                parallel(
                        sound("switch"),
                        sound("mon_kill"),
                        run(new Runnable() {
                            public void run() {
                                secondarySwitchPressed = true;
                            }
                        })
                ),
                delay(0.6f),
                entityAppearance(bossRep, Movement.AirlockOpen, false, true, false),
                delay(0.2f),
                parallel(
                        entityAppearance(playerRep, Movement.AirlockOpen, false, true, true),
                        addAction(
                                repeat(8, target(enemyActorGroup, sequence(moveBy(1, 0), delay(0.2f), moveBy(-1, 0), delay(0.2f))))
                        )
                ),
                delay(7), //time airlock is open
                run(new Runnable() {
                    public void run() {
                        airlockState = AirlockState.Closed;
                        airlockSpriteTime = 0;
                    }
                }),
                delay(1f),
                entityAppearance(playerRep, Movement.Idle, true, false, true),
                parallel(
                        sound("door"),
                        run(new Runnable() {
                            public void run() {
                                exitDoorLocked = false;
                            }
                        })
                )
        );
        return action;
    }

    private Action generateEndRoundAction (){
        return sequence(
                parallel(
                        entityAppearance(playerRep, Movement.Left, false, gravityOn, false),
                        target(playerRep, moveTo(-SCALE, playerPosition.y * SCALE, 0.3f)),
                        sound("door")
                ),
                run(new Runnable() { public void run() { main.onRoundEnded(); } })
        );
    }

    private Action generatePickupAction (){
        String sound = "menu";//def shouldn't happen
        switch (board.pickupType) {
            case Health:
                heartsLeft = Math.min(heartsLeft + 1, MAX_HEARTS);
                sound = "eat";
                break;
            case StepBoost:
                movesPerTurn = Math.min(movesPerTurn + 1, MAX_STEPS_PER_TURN);
                movesLeft = Math.min(movesLeft + 1, movesPerTurn);
                sound = "gain_move";
                break;
        }
        return parallel(
                run(new Runnable(){public void run(){
                    hasPickup = false;
                    Util.log("got pickup: " + board.pickupType);
                    Util.log("Player hearts: " + heartsLeft);
                    Util.log("Player moves per turn: " + movesPerTurn);
                }}),
                sound(sound)
        );
    }

    public void cheatGainStepBoostPickup (){
        movesPerTurn = Math.min(movesPerTurn + 1, MAX_STEPS_PER_TURN);
        movesLeft = Math.min(movesLeft + 1, movesPerTurn);
        syncHealthUI();
    }

    private Action generateEndGameAction (){
        return sequence(
                //TODO player to face container.
                run(new Runnable() { public void run() { main.onRoundEnded(); } })
        );
    }


    private Action generateEnemyDieAction (Enemy enemy, boolean onLadder){
        EnemyRepresentation enemyRep = enemyRepresentations.get(enemy);
        String dieSound;
        float delay = 0;
        switch (enemy.type){
            case Chupoof:
                dieSound = enemy.isMerged() ? "bchu_kill" : "chu_kill";
                delay = gravityOn ? 0.8f : 0.4f;
                break;
            case Toughie:
                dieSound = "robot_kill";
                delay = 0.3f;
                break;
            default: //boss, never happens
                dieSound = "chu_kill";
                break;
        }
        return sequence(
                parallel(
                        entityAppearance(enemyRep, Movement.Dead, gravityOn, false, onLadder),
                        sound(dieSound, delay)
                ),
                delay(ENEMY_DIE_TIME),
                target(enemyRep, addAction(fadeOut(ENEMY_DEAD_FADE_TIME))) //add action so we don't have to wait for fade before next turn
                );
    }

    private Action generateEnemyHurtAction (Enemy enemy, boolean onLadder){
        EnemyRepresentation enemyRep = enemyRepresentations.get(enemy);
        String hurtSound;
        float delay = 0;
        switch (enemy.type){
            case Chupoof:
                hurtSound = "bchu_damage";
                break;
            case Toughie:
                hurtSound = "robot_damage";
                break;
            default: //boss
                hurtSound = "mon_damage";
                break;
        }
        return parallel(
                entityAppearance(enemyRep, Movement.Hurt, gravityOn, false, onLadder),
                sound(hurtSound, delay)
        );
    }

    private Action generateEnemyAttackSoundAction (Enemy enemy){
        String moveSound;
        String attackSound;
        float moveDelay = 0;
        float attackDelay = 0;
        switch (enemy.type){
            case Chupoof:
                moveSound = enemy.isMerged() ? "bchu_move" : "chu_move";
                attackSound = enemy.isMerged() ? "bchu_hurt" : "chu_hurt";
                attackDelay = 0.2f;
                break;
            case Toughie:
                moveSound = "robot_move";
                attackSound = "robot_hurt";
                break;
            default: //boss
                moveSound = "mon_move";
                attackSound = "mon_hurt";
                break;
        }
        return sequence(sound(attackSound, attackDelay), sound(moveSound, moveDelay));
    }

    private Action generateEnemyMoveSoundAction (Enemy enemy){
        String sound;
        float delay = 0;
        switch (enemy.type){
            case Chupoof:
                sound = enemy.isMerged() ? "bchu_move" : "chu_move";
                break;
            case Toughie:
                sound = "robot_move";
                break;
            default: //boss
                sound = "mon_move";
                break;
        }
        return sound(sound, delay);
    }

    private Action generateEnemyAbsorbAction(final Enemy absorber, final Enemy absorbed){
        absorbed.die(); //Kill other immediately to prevent it from getting a turn
        Action action = target(enemyRepresentations.get(absorber), sequence(
                delay(ENEMY_FLOAT_TIME),
                run(new Runnable() {
                    public void run() {
                        absorber.absorb(absorbed);
                    }
                }),
                entityAppearance(Movement.Idle, false, false, false),
                target(enemyRepresentations.get(absorbed), hide())
        ));
        return action;
    }

    private void finishMove (){
        hideMovementButtons();
        syncHealthUI();
        if (movesLeft == 0){
            finishTurn();
        } else {
            state = State.PlayerInput;
            preparePlayerMove();
        }
    }

    private void finishTurn (){
        hideMovementButtons();
        movesLeft = 0;
        syncHealthUI();
        movesLeft = movesPerTurn;
        advanceTraps();
    }

    private void advanceTraps (){
        if (board.traps.size == 0){
            advanceEnemies();
            return;
        }
        ParallelAction trapAnimations = parallel();
        ParallelAction painAnimations = parallel();
        boolean anyPain = false;
        boolean anyFire = false;
        for (Trap trap : board.traps){
            TrapRepresentation representation = trapRepresentations.get(trap);
            trap.step();
            if (trap.shouldWarn()){
                trapAnimations.addAction(target(representation, color(TrapRepresentation.SPARKS)));
            }
            else if (trap.shouldFire()){
                anyFire = true;
                trapAnimations.addAction(
                        target(representation, sequence(
                                color(TrapRepresentation.ZAP),
                                delay(0.3f),
                                color(TrapRepresentation.NO_DRAW)
                        )));

                for (Enemy enemy : board.enemies){
                    if (!enemy.isDead() && enemy.coord.equals(trap.coord)){
                        anyPain = true;
                        enemy.die();
                        painAnimations.addAction(generateEnemyDieAction(enemy, false));
                        break;
                    }
                }
                if (playerPosition.equals(trap.coord)){
                    anyPain = true;
                    heartsLeft--;
                    if (heartsLeft == 0){
                        painAnimations.addAction(generateDeadPlayerAction(false, playerPosition.y > 0 && board.ladders[playerPosition.x][playerPosition.y]));
                    } else {
                        painAnimations.addAction(generateHurtPlayerAction(false, playerPosition.y > 0 && board.ladders[playerPosition.x][playerPosition.y]));
                    }
                }
            }
        }
        if (anyFire || anyPain){
            trapAnimations.addAction(sound(anyPain ? "shock" : "elec", 0));
        }
        final boolean playerDead = heartsLeft == 0;
        stage.addAction(sequence(trapAnimations, painAnimations,
                run(new Runnable() {public void run() { if (playerDead) onPlayerDead(); else advanceEnemies(); } })));
    }

    private void advanceEnemies (){
        enemiesToFront();
        SequenceAction sequenceAction = sequence();

        outer:
        for (int i = 0; i < board.enemies.size; i++) {
            Enemy enemy = board.enemies.get(i);
            if (enemy.isDead())
                continue;

            enemyMoves:
            for (int j = 0; j < enemy.getStepsPerTurn(this); j++) {
                Coordinate coordinate = enemy.findMove(this);
                if (coordinate == null)
                    break enemyMoves; //no move to make.

                Movement movement = Movement.Right;
                if (coordinate.x < 0) movement = Movement.Left;
                else if (coordinate.y > 0) movement = Movement.Up;
                else if (coordinate.y < 0) movement = movement.Down;

                EnemyRepresentation enemyRepresentation = enemyRepresentations.get(enemy);
                tmpC.set(coordinate).add(enemy.coord); //target space
                if (playerPosition.equals(tmpC)){ //land on player
                    heartsLeft--;
                    syncHealthUI();
                    Action attackAction = parallel(
                            target(enemyRepresentation, moveTo(tmpC.x * SCALE, tmpC.y * SCALE, gravityOn ? ENEMY_STEP_TIME : ENEMY_FLOAT_TIME)),
                            entityAppearance(enemyRepresentation, movement, gravityOn, true, board.ladders[tmpC.x][tmpC.y]),
                            generateEnemyAttackSoundAction(enemy)
                            );
                    if (heartsLeft == 0){
                        enemy.coord.add(coordinate);
                        sequenceAction.addAction(parallel(
                                generateDeadPlayerAction(true, playerPosition.y > 0 && board.ladders[playerPosition.x][playerPosition.y]),
                                attackAction
                        ));
                        break outer; //Player dying so no need to animate other steps or enemies.
                    } else { //hurts player, bounces back to where it was
                        Action retreatAction = parallel(
                                target(enemyRepresentation, moveTo(enemy.coord.x * SCALE, enemy.coord.y * SCALE, gravityOn ? ENEMY_STEP_TIME : ENEMY_FLOAT_TIME)),
                                entityAppearance(enemyRepresentation, movement, gravityOn, false, board.ladders[enemy.coord.x][enemy.coord.y]),
                                generateEnemyMoveSoundAction(enemy)
                        );

                        sequenceAction.addAction(parallel(
                                generateHurtPlayerAction(true, playerPosition.y > 0 && board.ladders[playerPosition.x][playerPosition.y]),
                                sequence(attackAction, retreatAction)
                        ));
                    }
                } else { //just move
                    enemy.coord.add(coordinate);
                    sequenceAction.addAction(target(enemyRepresentation,
                            parallel(
                                    moveTo(tmpC.x * SCALE, tmpC.y * SCALE, gravityOn ? ENEMY_STEP_TIME : ENEMY_FLOAT_TIME),
                                    entityAppearance(enemyRepresentation, movement, gravityOn, false, board.ladders[tmpC.x][tmpC.y]),
                                    generateEnemyMoveSoundAction(enemy)
                            )));
                    if (enemy.mergeable()){
                        for (int k = 0; k < board.enemies.size; k++) {
                            Enemy other = board.enemies.get(k);
                            if (enemy != other && !other.isDead() && enemy.coord.equals(other.coord)){
                                sequenceAction.addAction(generateEnemyAbsorbAction(enemy, other));
                                break enemyMoves; //stop moving or this could snow ball into long chain of merged enemies.
                            }
                        }
                    }
                }
                sequenceAction.addAction(entityAppearance(enemyRepresentation, Movement.Idle, gravityOn, false, board.ladders[enemy.coord.x][enemy.coord.y]));
            }


        }

        for (int i = 0; i < board.enemies.size; i++) {
            board.enemies.get(i).onEnemiesTurnDone();
        }

        final boolean playerDead = heartsLeft == 0;
        sequenceAction.addAction(
                run(new Runnable() {public void run() { if (playerDead) onPlayerDead(); else preparePlayerMove(); } }));
        stage.addAction(sequenceAction);
    }

    private void onPlayerDead(){
        Util.log("Game Over");
        gameOver = true;
        story.onGameOver(this);

        String[] dialogue = new String[assets.gameOverDialogue.length + 1];
        for (int i = 0; i < assets.gameOverDialogue.length; i++) {
            dialogue[i] = assets.gameOverDialogue[i];
        }
        dialogue[dialogue.length - 1] = story.getStatisticsDialogue();

        beginDialogue(dialogue, assets.gameOverDialogueMoods);
    }

    private Action generateHurtPlayerAction (boolean fromEnemy, boolean onLadder){
        return target(playerRep, sequence(
                delay(fromEnemy ? PLAYER_HURT_FROM_ENEMY_DELAY_TIME : 0),
                parallel(
                        entityAppearance(playerRep, Movement.Hurt, gravityOn, false, onLadder),
                        sequence(
                                damage(this, 1, PLAYER_HURT_SPRITE_TIME / 2f),
                                damage(this, 0, PLAYER_HURT_SPRITE_TIME / 2f)
                                )
                        ),
                entityAppearance(playerRep, Movement.Idle, gravityOn, false, onLadder)
        ));
    }

    private Action generateDeadPlayerAction (boolean fromEnemy, boolean onLadder){
        return target(playerRep, sequence(
                delay(fromEnemy ? PLAYER_HURT_FROM_ENEMY_DELAY_TIME : 0),
                parallel(
                        entityAppearance(playerRep, Movement.Hurt, gravityOn, false, onLadder),
                        sound("game_over", 0.4f),
                        run(new Runnable() {
                            public void run() {
                                musicManager.stop(true);
                            }
                        }),
                        sequence(
                                damage(this, 1, PLAYER_HURT_SPRITE_TIME / 2f),
                                damage(this, 0.3f, PLAYER_HURT_SPRITE_TIME / 2f)
                        ),
                        sequence(
                                delay(0.9f),
                                run(new Runnable() {public void run() { showBrokenGlass = true; }})
                        )
                ),
                entityAppearance(playerRep, Movement.Dead, gravityOn, false, false)
        ));
    }

    private void updateButton (Button button, int x, int y){
        if (isTileEnterable(x, y, true, false, false)){
            button.setVisible(true);
            button.setPosition(x * SCALE, y * SCALE);
        } else {
            button.setVisible(false);
        }
    }

    boolean isTileEnterable (int x, int y, boolean canClimbLadders, boolean isEnemy, boolean mergeable){
        if (x < 0 || x >= Board.WIDTH || y < 0 || y >= Board.HEIGHT || board.obstacles[x][y])
            return false;
        if (gravityOn &&
                (!canClimbLadders || !board.ladders[x][y]) && //not able to climb or not on ladder
                !(y == 0 || board.obstacles[x][y-1] || board.ladders[x][y-1])) //not on floor or over obstacle/ladder
            return false;
        for (int i = 0; i < board.enemies.size; i++) {
            Enemy enemy = board.enemies.get(i);
            if (!enemy.isDead() && enemy.coord.equals(x, y)){
                if (isEnemy){
                    if (!(mergeable && enemy.mergeable()))
                        return false; //not both mergeable enemies, so cannot occupy same space
                } else {
                    if (!enemy.stompable())
                        return false; //cannot occupy space with living enemy
                }
                break; //no two enemies can be in same space. no need to check other enemies
            }
        }
        return true;
    }

    private Action sound (final String name){
        return run(new Runnable() {
            @Override
            public void run() {
                assets.sfx.get(name).play();
            }
        });
    }

    private Action sound (final String name, final float delay){
        if (delay == 0)
            return sound(name);

        return sequence(delay(delay),run(new Runnable() {
            @Override
            public void run() {
                assets.sfx.get(name).play();
            }
        }));
    }

    private void soundInstant (String name){
        assets.sfx.get(name).play();
    }

    private void handleDialogueTap (){
        if (!dialogueLabel.isSpeaking()){
            continueDialogue();
        }
    }

    private void syncHealthUI (){
        for (int i = 0; i < heartImages.length; i++) {
            heartImages[i].setDrawable(assets.skin, i < heartsLeft ? "heart" : "heart-grey");
            heartImages[i].setVisible(i < MAX_HEARTS);
        }
        for (int i = 0; i < stepImages.length; i++) {
            stepImages[i].setDrawable(assets.skin, i < movesLeft ? "step-pip" : "step-pip-grey");
            stepImages[i].setVisible(i < movesPerTurn);
        }
    }

    private ChangeListener buttonListener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            executePlayerMove((Button)actor);
        }
    };

    private InputAdapter inputAdapter = new InputAdapter(){
        public boolean keyDown (int keycode) {
            switch (state){
                case PlayerInput:
                    switch (keycode){
                        case Input.Keys.LEFT:
                            arrowToButton(buttonLeft);
                            return true;
                        case Input.Keys.RIGHT:
                            arrowToButton(buttonRight);
                            return true;
                        case Input.Keys.UP:
                            arrowToButton(buttonUp);
                            return true;
                        case Input.Keys.DOWN:
                            arrowToButton(buttonDown);
                            return true;
                        case Input.Keys.SPACE:
                            arrowToButton(doneButton);
                            return true;
                        case Input.Keys.ESCAPE:
                            onBackPressed();
                            return true;
                    }
                    break;
                case Dialogue:
                    handleDialogueTap();
                    return true;
            }
            return false;
        }

        public boolean touchDown (int screenX, int screenY, int pointer, int button) {
            if (state == State.Dialogue) {
                handleDialogueTap();
                return true;
            }
            return false;
        }

        private void arrowToButton (Button button){
            if (button.isVisible()){
                button.setChecked(!button.isChecked()); //trigger change event.
            }
        }
    };

    public void dispose (){
        for (Disposable disposable : disposables) disposable.dispose();
    }
}
