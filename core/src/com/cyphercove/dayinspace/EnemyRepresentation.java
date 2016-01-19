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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.ObjectMap;

import static com.cyphercove.dayinspace.Constants.*;

public class EnemyRepresentation extends EntityActor {

    private Enemy enemy;
    private GlowSpot glowSpot;

    private static final int LIGHT_COLOR = 0xd182ffff;
    private static final int GLOW_COLOR = 0xdd82ff00;

    public EnemyRepresentation(ObjectMap<String, Sprite> library) {
        super(library);
    }

    public void setEnemy(Enemy enemy) {
        this.enemy = enemy;
        if (enemy.type == EnemyType.Toughie){
            glowSpot = new GlowSpot();
            glowSpot.x = SCALE / 2;
            glowSpot.y = SCALE / 2;
            glowSpot.spotlight = false;
            glowSpot.width = 70;
            glowSpot.height = 70;
            glowSpot.glowColor = GLOW_COLOR;
            glowSpot.lightColor = LIGHT_COLOR;
            glowSpot.glowSizeFraction = 0.4f;
        } else {
            glowSpot = null;
        }
    }

    @Override
    protected String getSpriteName(Movement movement, boolean gravityOn, boolean attacking, boolean onLadder) {
        switch (enemy.type){
            case Toughie:
                return movement == Movement.Dead ? "robotDead" : "robot";
            case Chupoof:
                return getChupoofSpriteName(movement, gravityOn, attacking, onLadder);
            case Boss:
                flipHorizontally = false; //boss always faces right
                return getBossSpriteName(movement, gravityOn, attacking, onLadder);

        }
        return "chupoofIdle"; //default should never happen
    }

    private String getBossSpriteName(Movement movement, boolean gravityOn, boolean attacking, boolean onLadder) {
        if (gravityOn) {
            switch (movement){
                case Idle:
                    return "monchuIdle";
                case Right:
                case Left:
                case Up:
                case Down:
                case Hurt:
                    return "monchuMove";
            }
        }

        switch (movement){
            case Idle:
                return "monchuFloatIdle";
            case Right:
            case Left:
            case Up:
            case Down:
            case Hurt:
                return "monchuFloatMove";
        }
        return "monchuFloatIdle"; //default should never happen
    }


    private String getChupoofSpriteName(Movement movement, boolean gravityOn, boolean attacking, boolean onLadder) {
        if (gravityOn) {
            switch (movement){
                case Idle:
                    return "chupoofIdle";
                case Right:
                case Left:
                case Up:
                case Down:
                    return "chupoofMove";
                case Hurt:
                    return "chupoofHurt";
                case Dead:
                    return "chupoofDead";
            }
        }

        switch (movement){
            case Idle:
                switch (enemy.getMergedSize()){
                    case 1:
                        return "chupoofFloatIdle";
                    case 2:
                        return "chupoof2FloatIdle";
                    default:
                        return "chupoof3FloatIdle";
                }
            case Right:
            case Left:
                switch (enemy.getMergedSize()){
                    case 1:
                        return "chupoof2FloatSideways";
                    case 2:
                        return "chupoof2FloatSideways";
                    default:
                        return "chupoof3FloatSideways";
                }
            case Up:
                switch (enemy.getMergedSize()){
                    case 1:
                        return "chupoofFloatUp";
                    case 2:
                        return "chupoof2FloatUp";
                    default:
                        return "chupoof3FloatUp";
                }
            case Down:
                switch (enemy.getMergedSize()){
                    case 1:
                        return "chupoofFloatDown";
                    case 2:
                        return "chupoof2FloatDown";
                    default:
                        return "chupoof3FloatDown";
                }
            case Hurt:
                switch (enemy.getMergedSize()){
                    case 1: //should never happen
                        return "chupoofFloatIdle";
                    case 2:
                        return "chupoof2FloatHurt";
                    default:
                        return "chupoof3FloatHurt";
                }
            case Dead:
                switch (enemy.getMergedSize()){
                    case 1:
                        return "chupoofFloatDead";
                    case 2:
                        return "chupoof2FloatDead";
                    default:
                        return "chupoof3FloatDead";
                }
        }
        return "chupoofIdle"; //default should never happen
    }

    private static int alphaScale (int color, float scale){
        float alpha = (color & 0xff) / 255f * scale;
        return (color & 0xffffff00) | (int)(alpha * 255);
    }

    public void drawLight (Batch batch, Assets assets){
        if (glowSpot != null && isVisible()) {
            glowSpot.lightColor = alphaScale(LIGHT_COLOR, getColor().a);
            glowSpot.drawLight(batch, assets, getX(), getY());
        }
    }

    public void drawGlow (Batch batch, Assets assets){
        if (glowSpot != null && isVisible()){
            glowSpot.glowColor = alphaScale(GLOW_COLOR, getColor().a);
            glowSpot.drawGlow(batch, assets, getX(), getY());
        }
    }

}
