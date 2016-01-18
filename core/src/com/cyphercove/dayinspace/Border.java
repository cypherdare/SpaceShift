package com.cyphercove.dayinspace;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import static com.cyphercove.dayinspace.Constants.*;

public class Border {

    private Assets assets;

    private TextureRegion left, right, top, bottom, exitDoorBehindTile, entryDoor, white;
    private Animation exitDoor;
    private static final int EXIT_DOOR_Y_OFFSET = -2;
    private static final float DOOR_ANIMATION_GREEN_TIME = 0.05f;

    private Array<GlowSpot> beams;
    GlowSpot exitDoorGlow;
    GlowSpot entryDoorGlow;

    private static final int DOOR_LIGHT_ENTRY = 0x82c9ffff;
    private static final int DOOR_LIGHT_CLOSED = 0xff8282ff;
    private static final int DOOR_LIGHT_OPEN = 0x82ff85ff;
    private static final int BEAM_LIGHT = 0xffffffff;

    private static final int DOOR_GLOW_ENTRY = 0x4f7c9f00;
    private static final int DOOR_GLOW_CLOSED = 0x9f4f4f00;
    private static final int DOOR_GLOW_OPEN = 0x4f9f5100;
    private static final int BEAM_GLOW = 0x62828200;

    public Border (Assets assets){
        this.assets = assets;
        left = assets.mainAtlas.findRegion("borderLeft");
        right = assets.mainAtlas.findRegion("borderRight");
        top = assets.mainAtlas.findRegion("borderTop");
        bottom = assets.mainAtlas.findRegion("borderBottom");
        entryDoor = assets.mainAtlas.findRegion("entryDoor");
        exitDoorBehindTile = assets.mainAtlas.findRegion("doorBehindTile");
        exitDoor = new Animation(0.05f, assets.mainAtlas.findRegions("exitDoor"), Animation.PlayMode.NORMAL);
        white = assets.white;

        beams = new Array<>(10);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 2; j++) {
                GlowSpot glowSpot = new GlowSpot();
                glowSpot.x = 33 + 44 * i;
                glowSpot.y = -4.5f + 141 * j;
                glowSpot.spotlight = true;
                glowSpot.width = 60;
                glowSpot.height = 100;
                glowSpot.angle = 90 + 180 * j;
                glowSpot.glowColor = BEAM_GLOW;
                glowSpot.lightColor = BEAM_LIGHT;
                glowSpot.glowSizeFraction = 0.5f;
                beams.add(glowSpot);
            }
        }

        exitDoorGlow = new GlowSpot();
        exitDoorGlow.x = 15;
        exitDoorGlow.y = 20;
        exitDoorGlow.width = 70;
        exitDoorGlow.height = 70;
        exitDoorGlow.glowSizeFraction = 0.2f;

        entryDoorGlow = new GlowSpot();
        entryDoorGlow.x = 7;
        entryDoorGlow.y = 20;
        entryDoorGlow.width = 70;
        entryDoorGlow.height = 70;
        entryDoorGlow.glowSizeFraction = 0.2f;
        entryDoorGlow.lightColor = DOOR_LIGHT_ENTRY;
        entryDoorGlow.glowColor = DOOR_GLOW_ENTRY;
    }

    /**
     * @param doorOpenTime Use 0 for closed door
     */
    public void drawBackground(Batch batch, int exitDoorY, float doorOpenTime, int entryDoorY){
        batch.setColor(Color.WHITE);
        batch.draw(left, -129, 0);
        batch.draw(right, 242, 0);

        if (doorOpenTime != 0)
            batch.draw(exitDoorBehindTile, -SCALE, exitDoorY);

        batch.draw(entryDoor, Board.WIDTH * SCALE, entryDoorY);
    }

    /**
     * @param doorOpenTime Use 0 for closed door
     */
    public void drawForeground (Batch batch, int exitDoorY, float doorOpenTime){
        batch.setColor(Color.WHITE);
        batch.draw(exitDoor.getKeyFrame(doorOpenTime), -SCALE, exitDoorY + EXIT_DOOR_Y_OFFSET);
        batch.draw(top, -129, 132);
        batch.draw(bottom, -129, -27);
    }

    public void drawLight (Batch batch, int exitDoorY, float doorOpenTime, int entryDoorY){
        //top and bottom space
        batch.setColor(Color.WHITE);
        batch.draw(white, -129, 158, 500, 18);
        batch.draw(white, -129, -44, 500, 18);
        for (GlowSpot spot : beams){
            spot.drawLight(batch, assets, 0, 0);
        }

        exitDoorGlow.lightColor = doorOpenTime > DOOR_ANIMATION_GREEN_TIME ? DOOR_LIGHT_OPEN : DOOR_LIGHT_CLOSED;
        exitDoorGlow.drawLight(batch, assets, -SCALE, exitDoorY);
        entryDoorGlow.drawLight(batch, assets, Board.WIDTH * SCALE, entryDoorY);
    }

    public void drawGlow (Batch batch, int exitDoorY, float doorOpenTime, int entryDoorY){
        for (GlowSpot spot : beams){
            spot.drawGlow(batch, assets, 0, 0);
        }

        exitDoorGlow.glowColor = doorOpenTime > DOOR_ANIMATION_GREEN_TIME ? DOOR_GLOW_OPEN : DOOR_GLOW_CLOSED;
        exitDoorGlow.drawGlow(batch, assets, -SCALE, exitDoorY);
        entryDoorGlow.drawGlow(batch, assets, Board.WIDTH * SCALE, entryDoorY);
    }
}
