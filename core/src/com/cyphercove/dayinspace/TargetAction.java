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

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.*;

/**
 * Wrapper that passes its target down to the child's children. Also sets grandchildren through SequenceAction and
 * ParallelAction to have the same target if they haven't had theirs set yet.
 */
public class TargetAction extends DelegateAction {

    public static TargetAction target (Actor target, Action action){
        TargetAction targetAction = Actions.action(TargetAction.class);
        targetAction.setTarget(target);
        targetAction.setAction(action);
        return targetAction;
    }

    protected boolean delegate(float delta) {
        return action.act(delta);
    }

    public void setActor (Actor actor) {
        if (action != null) {
            action.setActor(getTarget()); //use this to get Parallel and SequenceActions to apply target to children.
            action.setTarget(getTarget());
        }
        super.setActor(actor);
    }

}
