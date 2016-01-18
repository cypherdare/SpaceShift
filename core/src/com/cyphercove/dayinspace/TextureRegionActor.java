package com.cyphercove.dayinspace;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Used for final boss. Draws a centered texture region.
 */
public class TextureRegionActor extends Actor {
    TextureRegion region;

    public void setRegion (TextureRegion region){
        this.region = region;
    }

    public void draw (Batch batch, float parentAlpha) {
        if (region == null)
            return;

        batch.setColor(Color.WHITE);

        int width = (int)(region.getRegionWidth() * getScaleX());
        int height = (int)(region.getRegionHeight() * getScaleY());

        batch.draw(region, getX() - width / 2, getY() - height / 2, width, height);
    }
}
