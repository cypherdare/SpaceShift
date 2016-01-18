package com.cyphercove.dayinspace;

import com.badlogic.gdx.graphics.Color;

public final class Constants {

    public static final int DEFAULT_WINDOW_WIDTH = 800;
    public static final int DEFAULT_WINDOW_HEIGHT = 450;


    /** Scale of graphics to board. */
    public static final int SCALE = 22;

    public static final float ROUND_DIALOGUE_SPEED = 80f;
    public static final float NON_ROUND_DIALOGUE_SPEED = 60f;

    public static final float UI_SCREEN_PADDING = 10f;
    public static final float DIALOGUE_PRESENT_DURATION = 0.3f;
    public static final float DIALOGUE_HIDE_DURATION = 0.2f;
    public static final float DIALOGUE_LABEL_WIDTH = 400f;
    public static final float SPEECH_BOX_WIDTH = DIALOGUE_LABEL_WIDTH + 130;
    public static final float DIALOGUE_LABEL_HEIGHT = 90f;

    public static final int MAX_HEARTS = 4;
    public static final int MAX_STEPS_PER_TURN = 4;

    public static final float PLAYER_STEP_TIME = 0.45f;
    public static final float ENEMY_STEP_TIME = 0.35f;
    public static final float ENEMY_FLOAT_TIME = 0.2f;
    public static final float ENEMY_DIE_TIME = 1f;
    public static final float ENEMY_DEAD_FADE_TIME = 0.4f;
    public static final float PLAYER_ATTACK_ANIMATION_TIME = 0.8f;
    public static final float PLAYER_HURT_FROM_ENEMY_DELAY_TIME = 0.35f;
    public static final float PLAYER_HURT_SPRITE_TIME = 0.25f;
    public static final float GRAVITY_CHANGE_TIME = 5f;
    public static final float BACKGROUND_FULL_VELOCITY = 250;
    public static final float LIGHT_CHANGE_TIME = 3.5f;
    public static final float LIGHT_DIM_DELAY_TIME = 2.5f;

    public static final Color DIM_AMBIENT_COLOR = new Color(0x263239ff);
    public static final Color BOOKEND_BG_COLOR = new Color(0x5a5a5aff);

    public static final float NEW_STAGE_MUSIC_CROSS_FADE_TIME = 5f;
}
