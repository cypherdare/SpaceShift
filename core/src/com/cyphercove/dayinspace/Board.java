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

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.*;
import com.cyphercove.dayinspace.gameplayscene.rendering.GlowSpot;
import com.cyphercove.dayinspace.gameplayscene.rendering.Tile;
import com.cyphercove.dayinspace.gameplayscene.simpledata.MusicTrack;
import com.cyphercove.dayinspace.gameplayscene.simpledata.PickupType;
import com.cyphercove.dayinspace.gameplayscene.simpledata.SwitchAction;
import com.cyphercove.dayinspace.shared.Coordinate;
import com.cyphercove.dayinspace.shared.Sprite;

import static com.cyphercove.dayinspace.Constants.SCALE;
import static com.cyphercove.dayinspace.Util.*;

public class Board {

    public static final int HEIGHT = 6;
    public static final int A_WIDTH = 3;
    public static final int B_WIDTH = 5;
    public static final int WIDTH = 2 * A_WIDTH + B_WIDTH;

    public static final int NONE = 0x000000ff; //Black
    public static final int DOOR = 0x00ff00ff; //Green
    public static final int TRAP_PHASE_1 = 0xff0000ff; //Red
    public static final int TRAP_PHASE_2 = 0xbf0000ff; //Deep Red
    public static final int TRAP_PHASE_3 = 0x800000ff; //Half Red
    public static final int OBSTACLE = 0x0000ffff; //Blue
    public static final int SWITCH = 0xffff00ff; //Yellow
    public static final int SECONDARY_SWITCH = 0x808000ff; //Dark yellow.
    public static final int ENFORCED_ENEMY_SPAWN = 0xff00ffff; //Magenta
    public static final int LADDER_ALPHA = 0x00000080;
    public static final int PICKUP = 0x00ffffff; //Cyan
    public static final int BLANK = 0xffffffff; //White

    final boolean firstRoom;
    final boolean[][] obstacles = new boolean[WIDTH][HEIGHT];
    final boolean[][] ladders = new boolean[WIDTH][HEIGHT];
    final boolean[][] trapLocations = new boolean[WIDTH][HEIGHT];
    final Tile[][] tiles = new Tile[WIDTH][HEIGHT]; //TODO if save function added, cannot store these here;
    Tile airlockTile;
    final ObjectMap<Tile, Coordinate> decorationTiles = new ObjectMap<>();
    final Array<Trap> traps = new Array<>();
    final Array<Enemy> enemies = new Array<Enemy>();
    int entryDoorY;
    int exitDoorY;
    boolean exitStartsDoorLocked;
    boolean gravityStartsOn;
    boolean lightsStartOn;
    Coordinate zwitch;
    Coordinate zwitch2;
    boolean drawSwitch;
    SwitchAction switchAction;
    String[] entryDialogue;
    String[] switchDialogue;
    String[] secondarySwitchDialogue;
    public FaceMood[] entryDialogueMoods;
    public FaceMood[] switchDialogueMoods;
    public FaceMood[] secondarySwitchDialogueMoods;
    PickupType pickupType;
    Coordinate pickupCoordinate;
    Coordinate airlockCoordinate;
    Sprite pickupSprite;
    String entrySound;
    float entrySoundDelay;
    MusicTrack musicTrack;

    public static class Parameters {
        public int numEnemies;
        public Array<EnemyType> enemyTypes; //repeat enemy type to make it more common than the others.
        public boolean exactEnemyDistribution;
        public SwitchAction switchAction;
        public boolean hasSwitch;
        public String enforcedMap;
        public boolean firstRoom;
        public boolean gravityStartsOn;
        public boolean lightsStartOn;
        public String entryDialogue;
        public String switchDialogue;
        public String secondarySwitchDialogue;
        public String[] entryDialogueMoods;
        public String[] switchDialogueMoods;
        public String[] secondarySwitchDialogueMoods;
        public boolean pickupOnSwitch;
        public PickupType pickupType;
        public String entrySound;
        public float entrySoundDelay;
        public MusicTrack mxLevel;
        public boolean hasAirlock;
        public String[] decorationTiles;
        public Coordinate[] decorationTileCoords;
    }

    public Board(Assets assets, Parameters params){
        this.firstRoom = params.firstRoom;
        this.pickupType = params.pickupType;
        this.entrySound = params.entrySound;
        this.entrySoundDelay = params.entrySoundDelay;
        this.musicTrack = params.mxLevel;
        drawSwitch = params.switchAction == SwitchAction.UnlockExit || params.switchAction == SwitchAction.TurnGravityOn;
        if (params.entryDialogue != null)
            this.entryDialogue = params.entryDialogue.split("\n");
        if (params.switchDialogue != null)
            this.switchDialogue = params.switchDialogue.split("\n");
        if (params.secondarySwitchDialogue != null){
            this.secondarySwitchDialogue = params.secondarySwitchDialogue.split("\n");
        }

        if (params.decorationTiles != null) {
            for (int i = 0; i < params.decorationTiles.length; i++) {
                String tileArrayName = params.decorationTiles[i];
                Coordinate coord = params.decorationTileCoords[i];
                decorationTiles.put(assets.tiles.get(tileArrayName).first(), coord);
            }
        }

        if (entryDialogue != null){
            entryDialogueMoods = new FaceMood[entryDialogue.length];
            assets.populateMoods(entryDialogueMoods, params.entryDialogueMoods);
        }
        if (switchDialogue != null){
            switchDialogueMoods = new FaceMood[switchDialogue.length];
            assets.populateMoods(switchDialogueMoods, params.switchDialogueMoods);
        }
        if (secondarySwitchDialogue != null){
            secondarySwitchDialogueMoods = new FaceMood[secondarySwitchDialogue.length];
            assets.populateMoods(secondarySwitchDialogueMoods, params.secondarySwitchDialogueMoods);
        }

        boolean[][] enemySpawnBlockedSpaces = new boolean[WIDTH][HEIGHT];
        Array<Coordinate> switchLocations = new Array<>();
        Array<Coordinate> enforcedEnemyLocations = new Array<>();
        boolean[][] noTileLocations = new boolean[WIDTH][HEIGHT];

        if (params.enforcedMap != null){
            Pixmap map = assets.completeMaps.get(params.enforcedMap);
            for (int i = 0; i < map.getWidth(); i++) {
                for (int j = 0; j < map.getHeight(); j++) {
                    int pixel = map.getPixel(i, map.getHeight() - j - 1); //pixmaps are flipped vertically
                    handlePixel(pixel, i, j, i < WIDTH / 2, switchLocations, enforcedEnemyLocations, noTileLocations, enemySpawnBlockedSpaces);
                }
            }
        } else {

            Pixmap leftMap = assets.typeAMaps.get(rand(assets.typeAMaps.size));
            Pixmap middleMap = assets.typeBMaps.get(rand(assets.typeBMaps.size));
            Pixmap rightMap = assets.typeAMaps.get(rand(assets.typeAMaps.size));

            boolean flipLeft = coin();
            for (int i = 0; i < leftMap.getWidth(); i++) {
                for (int j = 0; j < leftMap.getHeight(); j++) {
                    int pixel = leftMap.getPixel(i, flipLeft ? leftMap.getHeight() - j - 1 : j);
                    handlePixel(pixel, i, j, true, switchLocations, enforcedEnemyLocations, noTileLocations, enemySpawnBlockedSpaces);
                }
            }

            boolean flipMidV = coin();
            boolean flipMidH = coin();
            for (int i = 0; i < middleMap.getWidth(); i++) {
                for (int j = 0; j < middleMap.getHeight(); j++) {
                    int pixel = middleMap.getPixel(flipMidH ? middleMap.getWidth() - 1 - i : i,
                            flipMidV ? middleMap.getHeight() - 1 - j : j);
                    handlePixel(pixel, i + A_WIDTH, j, false, switchLocations, enforcedEnemyLocations, noTileLocations, enemySpawnBlockedSpaces);
                }
            }

            boolean flipRight = coin();
            for (int i = 0; i < rightMap.getWidth(); i++) {
                for (int j = 0; j < rightMap.getHeight(); j++) {
                    int pixel = rightMap.getPixel(rightMap.getWidth() - 1 - i, flipRight ? rightMap.getHeight() - 1 - j : j);
                    handlePixel(pixel, i + A_WIDTH + B_WIDTH, j, false, switchLocations, enforcedEnemyLocations, noTileLocations, enemySpawnBlockedSpaces);
                }
            }
        }

        zwitch = (switchLocations.size > 0 && params.hasSwitch) ? switchLocations.get(rand(switchLocations.size)) : null;
        switchLocations.removeValue(zwitch, true);
        exitStartsDoorLocked = (params.hasSwitch && params.switchAction == SwitchAction.UnlockExit) || zwitch2 != null;
        gravityStartsOn = params.gravityStartsOn;
        lightsStartOn = params.lightsStartOn;
        this.switchAction = params.switchAction;

        if (params.pickupType != null) { //has pickup
            if (zwitch != null && params.pickupOnSwitch)
                pickupCoordinate = new Coordinate(zwitch);
            else if (params.pickupType != null && pickupCoordinate == null) {
                //if there wasn't a pickup coordinate on the maps, use a switch location.
                pickupCoordinate = new Coordinate(switchLocations.get(rand(switchLocations.size)));
            }

            String pickupSpriteArrayName = params.pickupType == PickupType.Health ? "healthPickup" : "stepsPickup";
            pickupSprite = assets.tiles.get(pickupSpriteArrayName).first().sprite;
        }

        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (noTileLocations[i][j]) {
                    if (airlockTile == null && params.hasAirlock){ //first encountered empty tile is lower left of where airlock goes.
                        airlockTile = assets.tiles.get("airlockTile").first();
                        tiles[i][j] = airlockTile;
                        airlockCoordinate = new Coordinate(i, j);
                    }
                    continue;
                }
                String tilesArrayName;
                if (pickupCoordinate != null && pickupCoordinate.x == i && pickupCoordinate.y == j) {
                    tilesArrayName = params.pickupType == PickupType.Health ? "healthPickupTiles" : "stepsPickupTiles";
                } else if (ladders[i][j]){
                    if ( j == 0 || !ladders[i][j-1]) tilesArrayName = "ladderBottomTiles";
                    else if (j == HEIGHT - 1 || !ladders[i][j+1]) tilesArrayName = "ladderTopTiles";
                    else tilesArrayName = "ladderTiles";
                } else if (trapLocations[i][j]){
                    tilesArrayName = "trapTiles";
                } else if (obstacles[i][j]){
                    tilesArrayName = "obstacleTiles";
                } else {
                    tilesArrayName = "standardTiles";
                }
                Array<Tile> tilesArray = assets.tiles.get(tilesArrayName);
                tiles[i][j] = tilesArray.get(rand(tilesArray.size));
            }
        }

        if (params.exactEnemyDistribution)
            params.numEnemies = params.enemyTypes.size;

        outer:
        for (int i = 0; i < params.numEnemies; i++) {
            EnemyType type = params.exactEnemyDistribution ?
                    params.enemyTypes.get(i) : params.enemyTypes.get(rand(params.enemyTypes.size));

            int minSpawnDistance = type.beginningStepsPerTurn + 2;
            int x, y;

            if (enforcedEnemyLocations.size > 0){
                Coordinate coord = enforcedEnemyLocations.get(enforcedEnemyLocations.size - 1);
                x = coord.x;
                y = coord.y;
                enforcedEnemyLocations.removeIndex(enforcedEnemyLocations.size - 1);
            } else {
                int xStart = rand(WIDTH), yStart = rand(HEIGHT);
                int xAdd = 0, yAdd = 0;
                x = xStart;
                y = yStart;
                while (enemySpawnBlockedSpaces[x][y] ||
                        (x > WIDTH - minSpawnDistance && Math.abs(y - entryDoorY) < minSpawnDistance)) {
                    if (xAdd < WIDTH) {
                        x = (xStart + ++xAdd) % WIDTH;
                    } else if (yAdd < HEIGHT) {
                        y = (yStart + ++yAdd) % HEIGHT;
                    } else {
                        Util.logError("No spaces left to add enemy.");
                        break outer;
                    }
                }
            }
            Enemy enemy = new Enemy(type, x, y);
            enemies.add(enemy);
            enemySpawnBlockedSpaces[x][y] = true;
        }
    }

    private void handlePixel (int pixel, int x, int y, boolean left, Array<Coordinate> switchLocations,
                              Array<Coordinate> enforcedEnemyLocations, boolean[][] noTileSpaces, boolean[][] enemySpawnBlockedSpaces){
        if ( (pixel & 0x000000ff) == LADDER_ALPHA) {
            ladders[x][y] = true;
        }

        pixel |= 0x000000ff; //whiteout alpha
        switch (pixel){
            case NONE:
                return;
            case DOOR:
                if (left) exitDoorY = y;
                else entryDoorY = y;
                return;
            case TRAP_PHASE_1:
                handleTrap(1, x, y, enemySpawnBlockedSpaces);
                return;
            case TRAP_PHASE_2:
                handleTrap(2, x, y, enemySpawnBlockedSpaces);
                return;
            case TRAP_PHASE_3:
                handleTrap(3, x, y, enemySpawnBlockedSpaces);
                return;
            case OBSTACLE:
                obstacles[x][y] = true;
                enemySpawnBlockedSpaces[x][y] = true;
                return;
            case SWITCH:
                switchLocations.add(new Coordinate(x, y));
                return;
            case SECONDARY_SWITCH:
                zwitch2 = new Coordinate(x, y);
                return;
            case ENFORCED_ENEMY_SPAWN:
                enforcedEnemyLocations.add(new Coordinate(x, y));
                return;
            case PICKUP:
                pickupCoordinate = new Coordinate(x, y);
                return;
            case BLANK:
                noTileSpaces[x][y] = true;
                return;
        }
        logError("unknown pixel color: " + Integer.toHexString(pixel));
    }

    private void handleTrap (int phase, int x, int y, boolean[][] enemySpawnBlockedSpaces){
        traps.add(new Trap(x, y, phase));
        trapLocations[x][y] = true;
        enemySpawnBlockedSpaces[x][y] = true; //TODO only if trap is in warn state
    }

    public void reset(){
        for (Trap trap : traps) trap.reset();
        for (Enemy enemy : enemies) enemy.reset();
    }

    // RENDERING --------------------------------------------------------------------------

    public void drawRearLight(Assets assets, Batch batch, float gravityLadderAnimationTime, float elapsedTime, boolean switchPressed, boolean secondarySwitchPressed){
        for (int i = 0; i < Board.WIDTH; i++) {
            for (int j = 0; j < Board.HEIGHT; j++) {
                Tile tile = tiles[i][j];
                if (tile == null) continue;
                if (tile.light != null) {
                    tile.light.draw(batch, i * SCALE, j * SCALE, gravityLadderAnimationTime, tile.drawFlipped);
                }
            }
        }

        if (drawSwitch && zwitch != null){
            Tile switchTile = switchPressed ? assets.switchPressedTile : assets.switchUnpressedTile;
            if (switchTile != null && switchTile.light != null)
                switchTile.light.draw(batch, zwitch.x * SCALE, zwitch.y * SCALE, elapsedTime, switchTile.drawFlipped);
        }
        if (zwitch2 != null){
            Tile secondarySwitchTile = secondarySwitchPressed ? assets.secondarySwitchPressedTile : assets.secondarySwitchUnpressedTile;
            if (secondarySwitchTile != null && secondarySwitchTile.light != null)
                secondarySwitchTile.light.draw(batch, zwitch2.x * SCALE, zwitch2.y * SCALE, elapsedTime, secondarySwitchTile.drawFlipped);
        }

        for (ObjectMap.Entry<Tile, Coordinate> entry : decorationTiles){
            Tile tile = entry.key;
            Coordinate coord = entry.value;
            if (tile.light != null)
                tile.light.draw(batch, coord.x * SCALE, coord.y * SCALE, elapsedTime, tile.drawFlipped);
        }
    }

    public void drawFrontLight(Assets assets, Batch batch, float gravityLadderAnimationTime, boolean switchPressed, boolean secondarySwitchPressed){
        for (int i = 0; i < Board.WIDTH; i++) {
            for (int j = 0; j < Board.HEIGHT; j++) {
                Tile tile = tiles[i][j];
                if (tile == null) continue;
                GlowSpot glowSpot = tile.glowSpot;
                if (glowSpot != null)
                    glowSpot.drawLight(batch, assets, i * SCALE, j * SCALE);
            }
        }
        if (drawSwitch && zwitch != null){
            Tile switchTile = switchPressed ? assets.switchPressedTile : assets.switchUnpressedTile;
            if (switchTile != null && switchTile.glowSpot != null)
                switchTile.glowSpot.drawLight(batch, assets, zwitch.x * SCALE, zwitch.y * SCALE);
        }
        if (zwitch2 != null){
            Tile secondarySwitchTile = secondarySwitchPressed ? assets.secondarySwitchPressedTile : assets.secondarySwitchUnpressedTile;
            if (secondarySwitchTile != null && secondarySwitchTile.glowSpot != null)
                secondarySwitchTile.glowSpot.drawLight(batch, assets, zwitch2.x * SCALE, zwitch2.y * SCALE);
        }

        for (ObjectMap.Entry<Tile, Coordinate> entry : decorationTiles){
            Tile tile = entry.key;
            Coordinate coord = entry.value;
            if (tile.glowSpot != null)
                tile.glowSpot.drawLight(batch, assets, coord.x * SCALE, coord.y * SCALE);
        }
    }

    public void draw(Assets assets, Batch batch, float elapsedTime, float gravityLadderAnimationTime,
                     float airlockSpriteTime, boolean gravityOn, boolean switchPressed,
                     boolean secondarySwitchPressed, GamePlayScene.AirlockState airlockState, boolean hasPickup){
        for (int i = 0; i < Board.WIDTH; i++) {
            for (int j = 0; j < Board.HEIGHT; j++) {
                Tile tile = tiles[i][j];
                if (tile == null) continue;
                Sprite sprite = tile.sprite;
                if (tile.isLadder && gravityOn)
                    sprite = tile.spriteGravityOn;
                if (sprite == null) continue;
                sprite.draw(batch, i * SCALE, j * SCALE, tile.isLadder ? gravityLadderAnimationTime : elapsedTime, tile.drawFlipped);
            }
        }

        if (drawSwitch && zwitch != null){
            Tile switchTile = switchPressed ? assets.switchPressedTile : assets.switchUnpressedTile;
            if (switchTile != null && switchTile.sprite != null)
                switchTile.sprite.draw(batch, zwitch.x * SCALE, zwitch.y * SCALE, 0, switchTile.drawFlipped);
        }
        if (zwitch2 != null){
            Tile secondarySwitchTile = secondarySwitchPressed ? assets.secondarySwitchPressedTile : assets.secondarySwitchUnpressedTile;
            if (secondarySwitchTile != null && secondarySwitchTile.sprite != null)
                secondarySwitchTile.sprite.draw(batch, zwitch2.x * SCALE, zwitch2.y * SCALE, 0, secondarySwitchTile.drawFlipped);
        }

        for (ObjectMap.Entry<Tile, Coordinate> entry : decorationTiles){
            Tile tile = entry.key;
            Coordinate coord = entry.value;
            if (tile.sprite != null)
                tile.sprite.draw(batch, coord.x * SCALE, coord.y * SCALE, elapsedTime, tile.drawFlipped);
        }

        if (airlockCoordinate != null){
            String airlockSpriteName;
            switch (airlockState){
                case Open:
                    airlockSpriteName = "airlockOpen";
                    break;
                case Closed:
                    airlockSpriteName = "airlockClose";
                    break;
                case Static:
                default:
                    airlockSpriteName = "airlockStatic";
            }
            Sprite airlockSprite = assets.sprites.get(airlockSpriteName);
            airlockSprite.draw(batch, airlockCoordinate.x * SCALE, airlockCoordinate.y * SCALE, airlockSpriteTime, false);
        }

        if (pickupType != null && hasPickup){
            pickupSprite.draw(batch, pickupCoordinate.x * SCALE, pickupCoordinate.y * SCALE, elapsedTime, false);
        }
    }

    public void drawGlow(Assets assets, Batch batch, float gravityLadderAnimationTime, boolean switchPressed, boolean secondarySwitchPressed){
        for (int i = 0; i < Board.WIDTH; i++) {
            for (int j = 0; j < Board.HEIGHT; j++) {
                Tile tile = tiles[i][j];
                if (tile == null) continue;
                GlowSpot glowSpot = tile.glowSpot;
                if (glowSpot != null)
                    glowSpot.drawGlow(batch, assets, i * SCALE, j * SCALE);
            }
        }
        if (drawSwitch && zwitch != null){
            Tile switchTile = switchPressed ? assets.switchPressedTile : assets.switchUnpressedTile;
            if (switchTile != null && switchTile.glowSpot != null)
                switchTile.glowSpot.drawGlow(batch, assets, zwitch.x * SCALE, zwitch.y * SCALE);
        }
        if (zwitch2 != null){
            Tile secondarySwitchTile = secondarySwitchPressed ? assets.secondarySwitchPressedTile : assets.secondarySwitchUnpressedTile;
            if (secondarySwitchTile != null && secondarySwitchTile.glowSpot != null)
                secondarySwitchTile.glowSpot.drawGlow(batch, assets, zwitch2.x * SCALE, zwitch2.y * SCALE);
        }

        for (ObjectMap.Entry<Tile, Coordinate> entry : decorationTiles){
            Tile tile = entry.key;
            Coordinate coord = entry.value;
            if (tile.glowSpot != null)
                tile.glowSpot.drawGlow(batch, assets, coord.x * SCALE, coord.y * SCALE);
        }
    }

}
