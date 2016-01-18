package com.cyphercove.dayinspace;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.*;

import static com.cyphercove.dayinspace.Constants.*;

/**
 * Not intended for being drawn by stage because it glows. Manages animation actions for the trap.
 *
 * Quick hack: uses color to encode which sprite should be drawn.
 */
public class TrapRepresentation extends Actor {

    public static final Color NO_DRAW = new Color (1, 0, 0, 1);
    public static final Color SPARKS = new Color (0, 1, 0, 1);
    public static final Color ZAP = new Color (0, 0, 1, 1);

    private static final Color LIGHT_COLOR = new Color (0xc6b9ffff);
    private static final float ZAP_LIGHT_SIZE = 56;
    private static final Color GLOW_COLOR = new Color (0x4e35baff);
    private static final float ZAP_GLOW_SIZE = 36;

    private final Color lastColor = new Color();

    float animationTime = 0;

    public void act (float delta) {
        super.act(delta);
        animationTime += delta;
    }

    public void drawAdditiveGlow(Batch batch, Assets assets){
        Color color = getColor();
        if (!lastColor.equals(color)) {
            animationTime = MathUtils.random(); //start looping animation at random time
            lastColor.set(color);
        }
        if (color.r > 0.5f)
            return;
        Sprite sprite = color.g > 0.5f ? assets.sprites.get("trapSparks") : assets.sprites.get("trapZap");
        sprite.draw(batch, getX() + SCALE / 2, getY() + SCALE / 2, animationTime, false);
    }

    public void drawLinearGlow (Batch batch, Assets assets){
        Color color = getColor();
        if (color.b > 0){
            batch.setColor(GLOW_COLOR);
            batch.draw(assets.lightPoint, getX() + SCALE / 2 - ZAP_GLOW_SIZE / 2, getY() + SCALE / 2 - ZAP_GLOW_SIZE / 2, ZAP_GLOW_SIZE, ZAP_GLOW_SIZE);
        }
    }

    public void drawLight (Batch batch, Assets assets){
        Color color = getColor();
        if (color.b > 0){
            batch.setColor(LIGHT_COLOR);
            batch.draw(assets.lightPoint, getX() + SCALE / 2 - ZAP_LIGHT_SIZE / 2, getY() + SCALE / 2 - ZAP_LIGHT_SIZE / 2, ZAP_LIGHT_SIZE, ZAP_LIGHT_SIZE);
        }
    }
}
