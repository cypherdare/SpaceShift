package com.cyphercove.dayinspace;

import com.badlogic.gdx.utils.*;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class PlayerRepresentation extends EntityActor {

    boolean airlockWasOpen = false;

    public PlayerRepresentation(ObjectMap<String, Sprite> library) {
        super(library);
    }

    @Override
    protected String getSpriteName(Movement movement, boolean gravityOn, boolean attacking, boolean onLadder) {
        if (airlockWasOpen){
            if (movement != Movement.AirlockOpen){
                airlockWasOpen = false;
                return "playerAirlockEnd";
            } else {
                return "playerAirlockMiddle";
            }
        }

        if (movement == Movement.AirlockOpen){
            airlockWasOpen = true;
            flipHorizontally = false; //ensure facing correct way for scene.
            addAction(sequence(
                    delay(0.6f),
                    run(new Runnable() {
                        @Override
                        public void run() { //set the same movement again as a trigger to move to the middle sprite
                            setAppearance(Movement.AirlockOpen, true, true, true);
                        }
                    })
            ));
            return "playerAirlockBegin";
        }

        if (gravityOn) {
            switch (movement){
                case Idle:
                    return onLadder ? "playerLadderIdle" : "playerIdle";
                case Right:
                case Left:
                    return attacking ? "playerAttack" : "playerWalk";
                case Up:
                case Down:
                    return "playerLadderClimb";
                case Hurt:
                    return "playerHurt";
                case Dead:
                    return "playerDead";
            }
        }

        switch (movement){
            case Idle:
                return "playerFloatIdle";
            case Right:
            case Left:
                return attacking ? "playerFloatAttackSideways" : "playerFloatSideways";
            case Up:
                return attacking ? "playerFloatAttackUp" : "playerFloatUp";
            case Down:
                return attacking ? "playerFloatAttackDown" : "playerFloatDown";
            case Hurt:
                return "playerHurt";
            case Dead:
                return "playerFloatDead";
        }
        return "playerIdle"; //default should never happen
    }

}
