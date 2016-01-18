package com.cyphercove.dayinspace;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.*;

/**
 * Ensures pixels are multipled by a whole number when rendered to screen, as long as the screen is large enough.
 */
public class PixelMultipleViewport extends Viewport {

    float minWorldWidth;
    float minWorldHeight;

    public PixelMultipleViewport(float minWorldWidth, float minWorldHeight) {
        this(minWorldWidth, minWorldHeight, new OrthographicCamera());
    }

    public PixelMultipleViewport(float minWorldWidth, float minWorldHeight, Camera camera) {
        this.minWorldWidth = minWorldWidth;
        this.minWorldHeight = minWorldHeight;
        setCamera(camera);
    }

    @Override
    public void update (int screenWidth, int screenHeight, boolean centerCamera) {
        Vector2 scaled = Scaling.fit.apply(minWorldWidth, minWorldHeight, screenWidth, screenHeight);

        boolean heightDriven = (Math.round(scaled.x) < screenWidth);
        float approximateScale = heightDriven ? scaled.y / minWorldHeight : scaled.x / minWorldWidth;
        float scale = approximateScale < 1 ? approximateScale : (int)approximateScale;

        setWorldSize(screenWidth / scale, screenHeight / scale);
        setScreenBounds(0, 0, screenWidth, screenHeight);
        apply(centerCamera);

        Util.log("Scale " + scale + " from approximate scale " + approximateScale);
    }
}
