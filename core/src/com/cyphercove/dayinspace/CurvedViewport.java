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

package com.cyphercove.dayinspace;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;

public class CurvedViewport extends Viewport {

    private float worldAndSourceTop, worldBottom, cornerAngle, worldLeft, worldRight, sourceBottom;

    public CurvedViewport () {
        setCamera(new OrthographicCamera());
    }

    public void update (int screenWidth, int screenHeight, boolean centerCamera) {
        setScreenBounds(0, 0, screenWidth, screenHeight);
        apply(centerCamera);
    }

    public void setCurvatureParameters (float worldAndSourceTop, float worldBottom, float worldLeft, float worldRight,
                                        float cornerAngle, float sourceBottom){
        this.worldAndSourceTop = worldAndSourceTop;
        this.worldBottom = worldBottom;
        this.worldLeft = worldLeft;
        this.worldRight = worldRight;
        this.cornerAngle = cornerAngle;
        this.sourceBottom = sourceBottom;
    }

    public void apply (boolean centerCamera) {
        super.apply(centerCamera);
        Gdx.gl.glViewport(0, 0, (int)getWorldWidth(), (int)getWorldHeight());
    }

    public Vector2 unproject (Vector2 screenCoords) {
        //map to curved world coordinates (the input uv's of the shader)
        screenCoords.x = (screenCoords.x - (float)getScreenX()) / (float)getScreenWidth() *
                (worldRight - worldLeft) + worldLeft;
        screenCoords.y = (screenCoords.y - (float)getScreenY()) / (float)getScreenHeight() *
                (worldBottom - worldAndSourceTop) + worldAndSourceTop;

        //map to output UVs following same formula as fragment shader
        float u = (float)Math.atan2(-screenCoords.x, -screenCoords.y) / cornerAngle * 0.5f + 0.5f;
        float v = (-screenCoords.len() - sourceBottom) / (worldAndSourceTop - sourceBottom);

        //map to the viewport's screen coordinates
        screenCoords.x = u * getScreenWidth() + getScreenX();
        screenCoords.y = (1f - v) * getScreenHeight() + getScreenY();

        return super.unproject(screenCoords);
    }

}
