package com.cyphercove.dayinspace;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.*;

public class Sprite {

    TextureRegion region;
    Animation animation;
    private int naturalWidth, naturalHeight;
    private int originX, originY;

    public static final int DIR_UP = 12;
    public static final int DIR_RIGHT = 3;
    public static final int DIR_LEFT = 9;
    public static final int DIR_DOWN = 6;

    public static class Parameters {
        String regionName;
        float frameTime; //anything greater than zero is an animation.
        Coordinate customOrigin;
        OriginMode originMode; //if no custom origin, how to set origin.
        Animation.PlayMode playMode; //if animated, whether to loop the animation
    }

    enum OriginMode {
        Zero, FeetCenterTile, CenterTile
    }

    public Sprite (Parameters paramaters, TextureAtlas atlas) {
        this(paramaters, atlas, false);
    }

    public Sprite (Parameters paramaters, TextureAtlas atlas, boolean reverseAnimation) {
        if (paramaters.frameTime > 0){
            Animation.PlayMode playMode = paramaters.playMode == null ? Animation.PlayMode.NORMAL : paramaters.playMode;
            if (reverseAnimation){
                switch (playMode){
                    case NORMAL:
                        playMode = Animation.PlayMode.REVERSED;
                        break;
                    case LOOP:
                        playMode = Animation.PlayMode.LOOP_REVERSED;
                        break;
                }
            }

            Array<? extends TextureRegion> keyFrames = atlas.findRegions(paramaters.regionName);
            if (keyFrames.size == 0)
                Util.logError("Animation " + "\"" + paramaters.regionName + "\" doesn't exist.");
            setAnimation(new Animation(paramaters.frameTime, atlas.findRegions(paramaters.regionName), playMode));
        } else {
            TextureRegion region = atlas.findRegion(paramaters.regionName);
            if (region == null)
                Util.logError("AtlasRegion " + "\"" + paramaters.regionName + "\" doesn't exist.");
            setRegion(region);
        }

        if (paramaters.customOrigin != null){
            setOrigin(paramaters.customOrigin.x, paramaters.customOrigin.y);
        } else if (paramaters.originMode == OriginMode.FeetCenterTile){
            setOriginToPlantFeetCenter();
        } else if (paramaters.originMode == OriginMode.CenterTile){
            setOriginToCenter();
        } else { //OriginMode.Zero
            originX = 0;
            originY = 0;
        }
    }

    public Sprite(TextureRegion region) {
        setRegion(region);
    }

    public Sprite(Animation animation) {
        setAnimation(animation);
    }

    public float getDuration (){
        if (region != null)
            return 0;
        return animation.getAnimationDuration();
    }

    public void setRegion (TextureRegion region){
        this.region = region;
        naturalWidth = region.getRegionWidth();
        naturalHeight = region.getRegionHeight();
    }

    public void setAnimation (Animation animation){
        this.animation = animation;
        if (animation.getKeyFrames().length == 0){
            Util.log("zero length animation");
        }
        TextureRegion region = animation.getKeyFrame(0);
        naturalWidth = region.getRegionWidth();
        naturalHeight = region.getRegionHeight();
    }

    public void setOrigin (int originX, int originY){
        this.originX = originX;
        this.originY = originY;
    }

    public void setOriginToCenter (){
        TextureRegion region = this.region != null ? this.region : animation.getKeyFrame(0);
        originX = region.getRegionWidth() / 2;
        originY = region.getRegionHeight() / 2;
    }

    public int getNaturalWidth() {
        return naturalWidth;
    }

    public int getNaturalHeight() {
        return naturalHeight;
    }

    public void setOriginToPlantFeetCenter (){
        TextureRegion region = this.region != null ? this.region : animation.getKeyFrame(0);
        originX = region.getRegionWidth() / 2;
        originY = Constants.SCALE / 2;
    }

    public void draw (Batch batch, float x, float y, float age, boolean hFlip){
        if (age < 0) age = 0;
        TextureRegion region = this.region != null ? this.region : animation.getKeyFrame(age);
        int width = region.getRegionWidth();
        int height = region.getRegionHeight();
        if (hFlip){
            batch.draw(region, x + width - originX, y - originY, -width, height);
        } else {
            batch.draw(region, x - originX, y - originY, width, height);
        }
    }

}
