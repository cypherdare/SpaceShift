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

package com.cyphercove.dayinspace.shared;

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
