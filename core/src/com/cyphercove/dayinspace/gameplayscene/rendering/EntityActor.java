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

package com.cyphercove.dayinspace.gameplayscene.rendering;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.*;
import com.cyphercove.dayinspace.*;

import static com.cyphercove.dayinspace.Constants.*;

/**
 * Draws a game-board sprite. Give it a location based on bottom left corner of its tile.
 */
public abstract class EntityActor extends Actor{

    private float spriteAge;
    private com.cyphercove.dayinspace.shared.Sprite sprite;
    private String spriteName;
    protected boolean flipHorizontally;
    private boolean gravityOn;
    private final ObjectMap<String, com.cyphercove.dayinspace.shared.Sprite> library;
    private final Vector2 floatOffset = new Vector2();
    float floatTime;
    float floatPhase;

    public EntityActor (ObjectMap<String, com.cyphercove.dayinspace.shared.Sprite> library) {
        this.library = library;
        floatPhase = MathUtils.random() * MathUtils.PI2;
    }

    public void act (float delta){
        super.act(delta);
        spriteAge += delta;

        //float offset
        floatTime += delta;
        float angle = 90 + 20 * MathUtils.sin(MathUtils.PI2 * 0.125f * floatTime + 3 + floatPhase);
        float radius = 2f * MathUtils.sin(MathUtils.PI2 * 0.4f * floatTime + floatPhase);
        floatOffset.set(radius, 0).rotate(angle);
    }

    public void setFaceLeft (boolean faceLeft){
        flipHorizontally = faceLeft;
    }

    public void setAppearance (Movement movement, boolean gravityOn, boolean attacking, boolean onLadder){
        if (this.gravityOn && !gravityOn) {
            floatTime = 0; //reset so offset radius starts at 0
            floatOffset.set(0, 0);
        }
        this.gravityOn = gravityOn;
        if (movement == Movement.Left)
            flipHorizontally = true;
        else if (movement == Movement.Right)
            flipHorizontally = false;
        String spriteName = getSpriteName(movement, gravityOn, attacking, onLadder);
        if (this.spriteName != spriteName) {
            this.spriteName = spriteName;
            sprite = library.get(spriteName);
            spriteAge = 0;
        }
    }

    protected abstract String getSpriteName (Movement movement, boolean gravityOn, boolean attacking, boolean onLadder);

    public void draw (Batch batch, float parentAlpha){
        batch.setColor(1, 1, 1, getColor().a);
        if (sprite == null)
            return;
        if (gravityOn)
            sprite.draw(batch, getX() + SCALE / 2, getY() + SCALE / 2, spriteAge, flipHorizontally);
        else
            sprite.draw(batch, getX() + SCALE / 2 + (int)floatOffset.x, getY() + SCALE / 2 + (int)floatOffset.y, spriteAge, flipHorizontally);
    }
}
