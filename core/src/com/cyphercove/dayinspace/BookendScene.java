/*******************************************************************************
 * Copyright 2016 Cypher Cove, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.cyphercove.dayinspace;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.*;
import static com.cyphercove.dayinspace.Constants.*;

public class BookendScene implements Disposable{

    private ObjectSet<Disposable> disposables = new ObjectSet<>();

    private GameMain main;
    private SpriteBatch batch;
    private Assets assets;

    private Stage stage;
    private Viewport viewport;

    private Table table;
    private ScrollPane scrollPane;
    Button skipButton;

    private int nextPageIndex;
    private boolean prologue;
    private String[] currentSpeech;
    private FaceMood[] currentMoods;
    private FaceMood currentMood;
    private SpriteWidget faceWidget;
    private SpeakingLabel latestLabel;
    private InputMultiplexer inputMultiplexer;

    public BookendScene(GameMain main, final Assets assets, SpriteBatch batch){
        this.main = main;
        this.assets = assets;
        this.batch = batch;
        viewport = new ExtendViewport(520, 390);
        stage = new Stage(viewport, batch);

        table = new Table(assets.skin);

        scrollPane = new ScrollPane(table);
        scrollPane.setTouchable(Touchable.disabled);

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.pad(10);
        rootTable.padTop(20);
        rootTable.padBottom(20);

        Button dummyButton = new TextButton("skip", assets.skin);
        dummyButton.setVisible(false);
        skipButton = new TextButton("skip", assets.skin);
        skipButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                BookendScene.this.main.onIntroEnded();
                assets.sfx.get("menu").play();
            }
        });
        rootTable.add(dummyButton);

        faceWidget = new SpriteWidget(assets.faceMoods.get("default").idleSprite);
        faceWidget.setColor(Color.WHITE);
        rootTable.add(faceWidget).center().expand();

        rootTable.add(skipButton).top().right().row();
        rootTable.add(scrollPane).center().height(250).expandX().fill().colspan(100);

        stage.addActor(rootTable);

        inputMultiplexer = new InputMultiplexer(stage, inputAdapter);

    }

    public void resize (int width, int height){
        viewport.update(width, height, true);
    }

    /* @param prologue Whether to display the prologue scene rather than the epilogue.
     * @param story The current story, used only in the epilogue. */
    public void reset (boolean prologue, Story story){
        this.prologue = prologue;
        skipButton.setVisible(prologue);
        currentMood = assets.faceMoods.get("default");
        if (prologue){
            currentSpeech = assets.prologueDialogue;
            currentMoods = assets.prologueDialogueMoods;
        } else {
            currentSpeech = new String[assets.epilogueDialoguePart1.length + assets.epilogueDialoguePart2.length + 1];
            int idx = 0;
            for (String string : assets.epilogueDialoguePart1)
                currentSpeech[idx++] = string;
            currentSpeech[idx++] = "\n\n\n" + story.getStatisticsDialogue() + "\n\n\n\n\n";
            for (String string : assets.epilogueDialoguePart2)
                currentSpeech[idx++] = string;
            currentMoods = assets.epilogueMoods;
        }
        table.clear();
        nextPageIndex = 0;
        latestLabel = null;
        next();
    }

    private void next (){
        if (nextPageIndex >= currentSpeech.length) {
            if (prologue)
                main.onIntroEnded();
            else {
                //TODO animate falling asleep.
                main.onGameOver();
            }
            return;
        }
        table.row();
        latestLabel = new SpeakingLabel(assets, currentSpeech[nextPageIndex], assets.skin, "default", Constants.NON_ROUND_DIALOGUE_SPEED);
        currentMood = currentMoods[nextPageIndex];
        faceWidget.set(currentMood.speakingSprite);
        latestLabel.setWrap(true);
        table.add(latestLabel).width(300);
        nextPageIndex++;
    }

    public InputProcessor getInputProcessor (){
        return inputMultiplexer;
    }

    private InputAdapter inputAdapter = new InputAdapter(){
        public boolean keyDown (int keycode) {
            handleTap();
            return true;
        }

        public boolean touchDown (int screenX, int screenY, int pointer, int button) {
            handleTap();
            return true;
        }
    };

    private void handleTap (){
        if (!latestLabel.isSpeaking()){
            next();
        }
    }

    public void render (float delta, boolean fadeInComplete){
        Gdx.gl.glClearColor(BOOKEND_BG_COLOR.r, BOOKEND_BG_COLOR.g, BOOKEND_BG_COLOR.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!latestLabel.isSpeaking())
            faceWidget.set(currentMood.idleSprite);
        scrollPane.setScrollY(scrollPane.getMaxY());

        stage.getViewport().apply();
        Camera camera = stage.getCamera();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.enableBlending();
        batch.begin();
        TextureRegion bgRegion = assets.mainAtlas.findRegion("bookend-background");
        batch.setColor(Color.WHITE);
        batch.draw(bgRegion, camera.position.x - bgRegion.getRegionWidth() / 2, camera.position.y - bgRegion.getRegionHeight() / 2,
                bgRegion.getRegionWidth(), bgRegion.getRegionHeight());
        batch.end();

        stage.act(fadeInComplete ? delta : 0);
        stage.draw();

    }

    public void dispose (){
        for (Disposable disposable : disposables) disposable.dispose();
    }
}
