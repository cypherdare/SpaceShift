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

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.cyphercove.dayinspace.Constants;
import com.cyphercove.dayinspace.gameplayscene.rendering.GlowSpot;
import com.cyphercove.dayinspace.shared.Sprite;

public class Tile {
    public Sprite sprite;
    public Sprite spriteGravityOn;
    public GlowSpot glowSpot;
    public Sprite light;
    public boolean drawFlipped;
    public boolean isLadder;

    public static class Parameters {
        Sprite.Parameters sprite;
        Sprite.Parameters light;
        GlowSpot glowSpot;
        public boolean includeFlipped; //not used internally by Tile. Convenient place to tell assets to create flipped duplicate
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
