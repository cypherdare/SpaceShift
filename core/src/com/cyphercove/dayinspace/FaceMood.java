package com.cyphercove.dayinspace;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class FaceMood {
    public Sprite speakingSprite, idleSprite;
    private Sprite.Parameters speaking;
    private Sprite.Parameters idle;

    public void init (TextureAtlas atlas){
        speakingSprite = new Sprite(speaking, atlas);
        idleSprite = new Sprite(idle, atlas);
    }
}
