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

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.actions.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

/**Target must be a table. */
public class PaddingToAction extends TemporalAction {

    public static PaddingToAction padTop (Table target, float duration, float topPadding){
        PaddingToAction targetAction = Actions.action(PaddingToAction.class);
        targetAction.setTarget(target);
        targetAction.setDuration(duration);
        targetAction.setPadding(
                target.getPadLeft(), target.getPadRight(), topPadding, target.getPadBottom()
        );
        return targetAction;
    }

    public static PaddingToAction padBottom (Table target, float duration, float bottomPadding){
        PaddingToAction targetAction = Actions.action(PaddingToAction.class);
        targetAction.setTarget(target);
        targetAction.setDuration(duration);
        targetAction.setPadding(
                target.getPadLeft(), target.getPadRight(), target.getPadTop(), bottomPadding
        );
        return targetAction;
    }

    private float startLeft, startRight, startTop, startBottom;
    private float endLeft, endRight, endTop, endBottom;

    public PaddingToAction(){
        super();
    }

    public PaddingToAction (float duration){
        super(duration);
    }

    public PaddingToAction (float duration, Interpolation interpolation){
        super(duration, interpolation);
    }

    @Override
    protected void begin(){
        if (!(target instanceof Table))
            return;
        Table table = (Table)target;

        startLeft = table.getPadLeft();
        startRight = table.getPadRight();
        startTop = table.getPadTop();
        startBottom = table.getPadBottom();
    }

    @Override
    protected void update(float percent) {
        if (!(target instanceof Table))
            return;

        int left = Math.round(startLeft + (endLeft - startLeft) * percent);
        int right = Math.round(startRight + (endRight - startRight) * percent);
        int top = Math.round(startTop + (endTop - startTop) * percent);
        int bottom = Math.round(startBottom + (endBottom - startBottom) * percent);

        Table table = (Table)target;
        table.padLeft(left).padRight(right).padTop(top).padBottom(bottom);
        table.invalidate();
    }

    public void setPadding(float padding){
        endLeft = padding;
        endRight = padding;
        endTop = padding;
        endBottom = padding;
    }

    public void setPadding(float horizontal, float vertical){
        endLeft = horizontal;
        endRight = horizontal;
        endTop = vertical;
        endBottom = vertical;
    }

    public void setPadding(float left, float right, float top, float bottom){
        endLeft = left;
        endRight = right;
        endTop = top;
        endBottom = bottom;
    }

    public float getLeft(){
        return endLeft;
    }

    public float getRight(){
        return endRight;
    }

    public float getTop(){
        return endTop;
    }

    public float getBottom(){
        return endBottom;
    }
}
