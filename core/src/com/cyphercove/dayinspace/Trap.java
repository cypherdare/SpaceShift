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

public class Trap extends GameObject {

    private static final int STEPS_TO_FIRE = 3;
    int startingFireSteps;

    int fireSteps;

    public Trap (int x, int y, int initialPhase){
        super(x, y);
        startingFireSteps = initialPhase % STEPS_TO_FIRE;
    }

    public void reset (){
        fireSteps = startingFireSteps;
    }

    public void step (){
        fireSteps = (fireSteps + 1) % STEPS_TO_FIRE;
    }

    public boolean shouldFire (){
        return fireSteps == 0;
    }

    public boolean shouldWarn (){
        return fireSteps == STEPS_TO_FIRE - 1;
    }
}
