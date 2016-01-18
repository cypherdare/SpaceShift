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
