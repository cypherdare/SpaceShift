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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.cyphercove.dayinspace.Assets;
import com.cyphercove.dayinspace.Util;

public class GlowSpot {
    public float x;
    public float y;
    public float width;
    public float height; //for spotlights, this is beam length
    public float angle; //angle in degrees if spotlight
    public boolean spotlight;
    public int glowColor; //pre-multiplied with standard shader, so alpha is opaqueness
    public int lightColor;
    public float glowSizeFraction;

    public void drawLight (Batch batch, Assets assets, float xOffset, float yOffset){
        TextureRegion region = spotlight ? assets.lightBeam : assets.lightPoint;
        batch.setColor(Util.rgba8888ToFloatBits(lightColor));
        float height = spotlight ? this.height * 2 : this.height;
        batch.draw(region, x + xOffset - width / 2, y + yOffset - height / 2, width / 2, height / 2, width, height, 1, 1, angle - 90);
        //the minus 90 is so 0 degrees is pointing right (based on assets orientation)
    }

    public void drawGlow (Batch batch, Assets assets, float xOffset, float yOffset){
        TextureRegion region = spotlight ? assets.lightBeam : assets.lightPoint;
        batch.setColor(Util.rgba8888ToFloatBits(glowColor));
        float width = this.width * glowSizeFraction;
        float height = spotlight ? this.height * 2 * glowSizeFraction : this.height * glowSizeFraction;
        batch.draw(region, x + xOffset - width / 2, y + yOffset - height / 2, width / 2, height / 2, width, height, 1, 1, angle - 90);
        //the minus 90 is so 0 degrees is pointing right (based on assets orientation)
    }
}
