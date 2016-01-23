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

package com.cyphercove.dayinspace.shared;

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
