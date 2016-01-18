package com.cyphercove.dayinspace;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GlowSpot {
    float x;
    float y;
    float width;
    float height; //for spotlights, this is beam length
    float angle; //angle in degrees if spotlight
    boolean spotlight;
    int glowColor; //pre-multiplied with standard shader, so alpha is opaqueness
    int lightColor;
    float glowSizeFraction;

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
