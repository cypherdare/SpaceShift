package com.cyphercove.dayinspace;

public class Coordinate {

    public int x, y;

    public Coordinate (){}

    public Coordinate (int x, int y){
        this.x = x;
        this.y = y;
    }

    public Coordinate (Coordinate coordinate){
        this.x = coordinate.x;
        this.y = coordinate.y;
    }

    public Coordinate set (int x, int y){
        this.x = x;
        this.y = y;
        return this;
    }

    public Coordinate set (Coordinate other){
        this.x = other.x;
        this.y = other.y;
        return this;
    }

    public Coordinate add (int x, int y){
        this.x += x;
        this.y += y;
        return this;
    }

    public Coordinate add (Coordinate other){
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public boolean equals (int x, int y){
        return this.x == x && this.y == y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coordinate that = (Coordinate) o;

        if (x != that.x) return false;
        return y == that.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    public String toString (){
        return "(" + x + ", " + y + ")";
    }
}
