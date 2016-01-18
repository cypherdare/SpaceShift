package com.cyphercove.dayinspace;

import com.badlogic.gdx.utils.*;

public enum EnemyType {
    Chupoof(1, 1, true, true, c(0, 1), c(1, 0)),
    Toughie (2, 2, false, false, c(0, 1), c(1, 0)),
    Boss (100, 5, false, true, c(0, 1), c(1, 0));

    public final int hp, beginningStepsPerTurn;
    public final boolean affectedByGravity;
    public final boolean mergeable;
    ObjectSet<Coordinate> validMoves;
    EnemyType (int hp, int beginningStepsPerTurn, boolean mergeable, boolean affectedByGravity,
               Coordinate... validMoves){
        this.hp = hp;
        this.beginningStepsPerTurn = beginningStepsPerTurn;
        this.mergeable = mergeable;
        this.affectedByGravity = affectedByGravity;
        this.validMoves = new ObjectSet<>(validMoves.length);
        for (Coordinate c : validMoves){
            this.validMoves.add(c);
            this.validMoves.add(c(-c.x, c.y));
            this.validMoves.add(c(-c.x, -c.y));
            this.validMoves.add(c(c.x, -c.y));
        }
        this.validMoves.addAll(validMoves);
    }

    private static Coordinate c (int x, int y){
        return new Coordinate(x, y);
    }
}
