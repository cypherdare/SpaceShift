package com.cyphercove.dayinspace;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

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
