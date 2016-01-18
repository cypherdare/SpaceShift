package com.cyphercove.dayinspace;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.*;

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
