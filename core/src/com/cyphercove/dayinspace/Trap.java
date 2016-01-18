package com.cyphercove.dayinspace;

public class Trap extends GameObject {

    int stepsToFire = 3; //just a constant for now.
    int startingFireSteps;

    int fireSteps;

    public Trap (int x, int y, int initialPhase){
        super(x, y);
        startingFireSteps = initialPhase % stepsToFire;
    }

    public void reset (){
        fireSteps = startingFireSteps;
    }

    public void step (){
        fireSteps = (fireSteps + 1) % stepsToFire;
    }

    public boolean shouldFire (){
        return fireSteps == 0;
    }

    public boolean shouldWarn (){
        return fireSteps == stepsToFire - 1;
    }
}
