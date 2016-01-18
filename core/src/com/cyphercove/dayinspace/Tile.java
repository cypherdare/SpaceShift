package com.cyphercove.dayinspace;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Tile {
    Sprite sprite;
    Sprite spriteGravityOn;
    GlowSpot glowSpot;
    Sprite light;
    boolean drawFlipped;
    boolean isLadder;

    public static class Parameters {
        Sprite.Parameters sprite;
        Sprite.Parameters light;
        GlowSpot glowSpot;
        boolean includeFlipped; //not used internally by Tile. Convenient place to tell assets to create flipped duplicate
        boolean isLadder;
    }

    public Tile (Parameters parameters, TextureAtlas atlas){
        if (parameters.sprite != null) {
            sprite = new Sprite(parameters.sprite, atlas);
            spriteGravityOn = new Sprite(parameters.sprite, atlas, true);
        }
        if (parameters.light != null)
            light = new Sprite(parameters.light, atlas);
        glowSpot = parameters.glowSpot;
        isLadder = parameters.isLadder;
    }

    public Tile (Parameters parameters, TextureAtlas atlas, boolean drawFlipped){
        this(parameters, atlas);
        this.drawFlipped = drawFlipped;
        if (drawFlipped && glowSpot != null)
            glowSpot.x = Constants.SCALE - glowSpot.x;
    }
}
