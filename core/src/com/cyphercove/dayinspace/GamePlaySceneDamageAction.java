package com.cyphercove.dayinspace;

import com.badlogic.gdx.scenes.scene2d.actions.*;

public class GamePlaySceneDamageAction extends FloatAction {

    public static GamePlaySceneDamageAction damage (GamePlayScene gamePlayScene, float endValue, float duration){
        GamePlaySceneDamageAction action = Actions.action(GamePlaySceneDamageAction.class);
        action.setGamePlayScene(gamePlayScene);
        action.setEnd(endValue);
        action.setDuration(duration);
        return action;
    }

    GamePlayScene gamePlayScene;

    public void setGamePlayScene (GamePlayScene gamePlayScene){
        this.gamePlayScene = gamePlayScene;
    }

    protected void begin () {
        setStart(gamePlayScene.damageAnimationIntensity);
        super.begin();
    }
    public boolean act (float delta){
        boolean done = super.act(delta);
        gamePlayScene.damageAnimationIntensity = getValue();
        return done;
    }
}
