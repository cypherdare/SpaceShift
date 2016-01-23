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

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.*;
import com.cyphercove.dayinspace.Movement;
import com.cyphercove.dayinspace.Util;

public class EntityAppearanceAction extends Action {

    public static EntityAppearanceAction entityAppearance (Movement movement, boolean gravityOn, boolean attacking, boolean onLadder){
        EntityAppearanceAction action = Actions.action(EntityAppearanceAction.class);
        action.setAppearanceParams(movement, gravityOn, attacking, onLadder);
        return action;
    }

    public static EntityAppearanceAction entityAppearance (EntityActor target, Movement movement,
                                                           boolean gravityOn, boolean attacking, boolean onLadder){
        EntityAppearanceAction action = Actions.action(EntityAppearanceAction.class);
        action.setTarget(target);
        action.setAppearanceParams(movement, gravityOn, attacking, onLadder);
        return action;
    }

    Movement movement;
    boolean gravityOn;
    boolean attacking;
    boolean onLadder;

    public void setAppearanceParams(Movement movement, boolean gravityOn, boolean attacking, boolean onLadder) {
        this.movement = movement;
        this.gravityOn = gravityOn;
        this.attacking = attacking;
        this.onLadder = onLadder;
    }

    @Override
    public boolean act(float delta) {
        if (!(target instanceof EntityActor)){
            Util.logError(toString() + "Target must be instance of EntityActor.");
            return true;
        }
        ((EntityActor)target).setAppearance(movement, gravityOn, attacking, onLadder);
        return true;
    }
}