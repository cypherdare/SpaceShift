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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

//I didn't have time to implement save and continue, but this class is prepared for saving a game's progress to Json easily.
/**
 * Serializable game instance. Contains everything that gets saved.
 */
public class Story {

    Array<Board> boards = new Array<Board>();

    int livesLeft;
    int heartsLeft;
    int movesPerTurn;
    int stagesComplete;
    int stepsTaken;
    int enemiesDestroyed;

    public Story (Assets assets){
        Json json = new Json();
        Board.Parameters[] params = json.fromJson(Board.Parameters[].class, Gdx.files.internal("boardParams.json"));
        for (int i = 0; i < params.length; i++) {
            Board.Parameters stageParams = params[i];
            Board board = new Board(assets, stageParams);
            boards.add(board);
        }

        movesPerTurn = 3;
        livesLeft = 3;
        heartsLeft = 3;
        stagesComplete = 0;
        stepsTaken = 0;
        enemiesDestroyed = 0;
    }

    private void syncRoundData (GamePlayScene gamePlayScene){
        stepsTaken += gamePlayScene.stepsTaken;
        enemiesDestroyed += gamePlayScene.enemiesDestroyed;
        heartsLeft = gamePlayScene.heartsLeft;
        movesPerTurn = gamePlayScene.movesPerTurn;
    }

    public void onRoundCompleted (GamePlayScene gamePlayScene){
        stagesComplete++;
        syncRoundData(gamePlayScene);
    }

    public void onGameOver (GamePlayScene gamePlayScene){
        syncRoundData(gamePlayScene);
    }

    public String getStatisticsDialogue (){
        return "Steps taken: " + stepsTaken +
                "\nHostiles decommissioned: " + enemiesDestroyed +
                "\nChore list completed: " + (stagesComplete == boards.size ? "YES!!" : "NO!!");
    }

    public void generateNextRound (GamePlayScene gamePlayScene){
        gamePlayScene.initialize(this, boards.get(stagesComplete), heartsLeft, movesPerTurn);
    }

    public boolean hasNextRound (){
        return stagesComplete < boards.size;
    }
}
