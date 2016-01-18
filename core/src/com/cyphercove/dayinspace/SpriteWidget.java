package com.cyphercove.dayinspace;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class SpriteWidget extends Widget {

    Sprite sprite;
    float elapsed;

    public SpriteWidget(Sprite sprite){
        this.sprite = sprite;
    }

    public void set (Sprite sprite){
        this.sprite = sprite;
        elapsed = 0;
    }

    @Override
    public void act (float delta){
        elapsed += delta;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        batch.setColor(getColor());
        sprite.draw(batch, getX(), getY(), elapsed, false);
    }

    public float getPrefWidth () {
        return sprite.getNaturalWidth();
    }

    public float getPrefHeight () {
        return sprite.getNaturalHeight();
    }
//
//    public float getMaxWidth () {
//        return 0;
//    }
//
//    public float getMaxHeight () {
//        return 0;
//    }
}
