package com.cyphercove.dayinspace;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.viewport.Viewport;

public class TitleScreenScene implements Disposable {

    private GameMain main;
    private SpriteBatch batch;
    private Assets assets;

    private ObjectSet<Disposable> disposables = new ObjectSet<>();
    private Stage stage;
    private Viewport viewport;


    public TitleScreenScene(final GameMain main, final Assets assets, SpriteBatch batch) {
        this.main = main;
        this.assets = assets;
        this.batch = batch;
        viewport = new PixelMultipleViewport(600, 400);
        stage = new Stage(viewport, batch);

        Table table = new Table(assets.skin);
        table.setFillParent(true);
        table.add(new SpriteWidget(assets.sprites.get("titleLogo"))).padTop(50).padBottom(20).row();

        Table menu = new Table(assets.skin);
        table.add(menu).row();

        Button beginButton = new TextButton("Begin", assets.skin, "clear");
        beginButton.pad(4);
        beginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                main.beginNewGame();
                assets.sfx.get("menu").play();
            }
        });
        menu.add(beginButton).left().padBottom(4).row();

        if (main.isFullScreenAvailable()) {
            final Button fullScreen = new TextButton("Toggle fullscreen", assets.skin, "clear");
            fullScreen.pad(4);
            //no longer a check box fullScreen.getLabelCell().padLeft(4);
            fullScreen.setProgrammaticChangeEvents(false);
            fullScreen.setChecked(main.isFullScreen());
            fullScreen.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    main.setFullScreen(fullScreen.isChecked());
                }
            });
            menu.add(fullScreen).left().padBottom(4).row();
        }

        Button exitButton = new TextButton("Exit", assets.skin, "clear");
        exitButton.pad(4);
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        menu.add(exitButton).left().row();

        table.add("(c)2016 Cypher Cove").center().bottom().pad(10).expandY();

        stage.addActor(table);
    }

    public void resize (int width, int height){
        viewport.update(width, height, true);
    }

    public void render (float delta, boolean fadeInComplete){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(fadeInComplete ? delta : 0);
        stage.draw();
    }

    public InputProcessor getInputProcessor (){
        return stage;
    }

    public void dispose (){
        for (Disposable disposable : disposables) disposable.dispose();
    }
}
