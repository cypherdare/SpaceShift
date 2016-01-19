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

public class Enemy extends GameObject{

    final Coordinate startingCoord = new Coordinate();
    EnemyType type;
    private boolean dead;
    private final Vector2 tmp2 = new Vector2();
    private int hp;
    private int stepsPerTurn;
    private Coordinate previousMove; //used for maintaining "inertia"
    private int mergedSize;

    public Enemy (EnemyType type, int x, int y){
        super(x, y);
        this.type = type;
        startingCoord.set(x, y);
        reset();
    }

    public void reset (){
        coord.set(startingCoord);
        dead = false;
        hp = type.hp;
        stepsPerTurn = type.beginningStepsPerTurn;
        mergedSize = 1;
    }

    public void hurt (){
        hp--;
    }

    public boolean shouldDie (){
        return hp <= 0;
    }

    public int getHp (){
        return hp;
    }

    public int getStepsPerTurn(GamePlayScene gamePlayScene) {
        if (type == EnemyType.Boss && gamePlayScene.gravityOn)
            return stepsPerTurn - 1;
        return stepsPerTurn;
    }

    public boolean isDead() {
        return dead;
    }

    public void die() {
        dead = true;
    }

    public Coordinate findMove (GamePlayScene gamePlayScene){
        Coordinate targetCoordinate = gamePlayScene.playerPosition;

        if (type.mergeable){
            float bestDist2 = tmp2.set(coord.x - gamePlayScene.playerPosition.x, coord.y - gamePlayScene.playerPosition.y).len2();

            //if a mergeable enemy is closer than the player, try to move to that enemy instead of player.
            for (int i = 0; i < gamePlayScene.board.enemies.size; i++) {
                Enemy enemy = gamePlayScene.board.enemies.get(i);
                if (enemy == this || enemy.isDead() || !enemy.mergeable()) continue;
                float dist2 = tmp2.set(coord.x - enemy.coord.x, coord.y - enemy.coord.y).len2();
                if (bestDist2 < 0 || dist2 < bestDist2){
                    bestDist2 = dist2;
                    targetCoordinate = enemy.coord;
                }
            }
        }

        //Very special case. Lead boss down either ladder on the right if gravity on, and then over to left ladder.
        if (type == EnemyType.Boss && gamePlayScene.gravityOn && coord.x > 0){
            if (coord.y == 0){
                targetCoordinate = new Coordinate(coord.x - 1, coord.y);
            } else if (gamePlayScene.isTileEnterable(coord.x, coord.y - 1, canClimbLadders(), true, mergeable())){
                targetCoordinate = new Coordinate(coord.x, coord.y - 1);
            }
        }

        Coordinate bestCoord = null;
        float bestDist2 = -1;
        for (Coordinate c : type.validMoves){
            if (gamePlayScene.isTileEnterable(c.x + coord.x, c.y + coord.y, canClimbLadders(), true, mergeable())){
                float dist2 = tmp2.set(c.x + coord.x - targetCoordinate.x, c.y + coord.y - targetCoordinate.y).len2();
                if (bestDist2 < 0 || dist2 < bestDist2
                        || (epsilonEquals(dist2, bestDist2) && c.equals(previousMove))){ //prefer moving in same direction as previous turn
                    bestDist2 = dist2;
                    bestCoord = c;
                }
            }
        }

        previousMove = bestCoord;
        return bestCoord;
    }

    private boolean epsilonEquals (float a, float b){
        return Math.abs(a - b) < 0.001f;
    }

    public void onEnemiesTurnDone (){
        previousMove = null; //player likely in new position, no longer valid
    }

    public boolean canClimbLadders (){
        return false;
    }

    public boolean mergeable (){
        return type.mergeable;
    }

    public boolean isMerged() {
        return mergedSize > 1;
    }

    public int getMergedSize (){
        return mergedSize;
    }

    public void absorb (Enemy other){
        stepsPerTurn += other.stepsPerTurn;
        hp += other.hp;
        other.dead = true;
        mergedSize += other.mergedSize;
    }


    public boolean stompable (){
        return true; //TODO switch on enemy type
    }
}
