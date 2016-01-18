package com.cyphercove.dayinspace;

public abstract class GameObject {

    final Coordinate coord = new Coordinate();

    public GameObject (int x, int y){
        coord.set(x, y);
    }

    /**whether an enemy can spawn on this location.*/
    public boolean allowEnemySpawn (){
        return false;
    }
}
