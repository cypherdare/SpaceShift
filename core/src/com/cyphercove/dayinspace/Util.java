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
import com.badlogic.gdx.utils.NumberUtils;

import java.util.Random;

public final class Util {

    public static final boolean DEBUG = false;

    private static final Random random = new Random();

    private static void logInternal (Object obj){
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[3];
        String className = caller.getClassName();
        Gdx.app.log(className.substring(className.lastIndexOf('.') + 1, className.length())+ "." + caller.getMethodName() + "()", obj.toString());
    }

    public static void log (Object obj){
        logInternal(obj);
    }

    public static void log (int integer){
        logInternal(""+integer);
    }

    public static void log (float floating){
        logInternal(""+floating);
    }

    public static void logError (Object obj){
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[2];
        String className = caller.getClassName();
        Gdx.app.error(className.substring(className.lastIndexOf('.') + 1, className.length()) + "." + caller.getMethodName() + "()", obj.toString());
    }

    public static float rand (){
        return random.nextFloat();
    }

    public static int rand (int maxExc){
        return random.nextInt(maxExc);
    }

    public static boolean coin () {
        return random.nextBoolean();
    }

    public static void resetViewport (){
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public static void printYUpRectangular2DBooleanArray (boolean[][] array){
        for (int y = array[0].length - 1; y >= 0; y--) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < array.length; x++) {
                sb.append(array[x][y] ? "X " : "O ");
            }
            System.out.println(sb.toString());
        }
        System.out.println("");
    }

    public static float rgba8888ToFloatBits (int rgba){
        int abgr = ((rgba & 0xff) << 24) | (((rgba >> 8) & 0xff) << 16) | (((rgba >> 16) & 0xff) << 8) | ((rgba >> 24) & 0xff);
        return NumberUtils.intToFloatColor(abgr);
    }
}
