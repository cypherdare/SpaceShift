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
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Disposable;
import com.cyphercove.dayinspace.gameplayscene.simpledata.MusicTrack;

//Hacked in last minute. Ideally, this wouldn't be hard-coded to the three music tracks I used.
public class MusicManager implements Disposable{

    FileHandle ambience, intense, gravity;
    Music music1;
    Music music2;
    MusicTrack currentTrack;
    MusicTrack nextTrack;
    float crossFadeTime;
    float crossFadeDuration;
    boolean fading;
    float fadeTime;

    private static final float FADE_DURATION = 0.5f;

    @Override
    public void dispose() {
        if (music1 != null) {
            music1.stop();
            music1.dispose();
        }
        if (music2 != null){
            music2.stop();
            music2.dispose();
        }
    }

    public MusicManager (Assets assets){
        ambience = assets.music.get("Level1");
        intense = assets.music.get("Level2");
        gravity = assets.music.get("Level3");
    }

    private void begin (MusicTrack track){
        fading = false;

        if (currentTrack == track && music1 != null) { //no track change. make sure it's playing
            if (!music1.isPlaying())
                music1.play();
            return;
        }

        if (music1 != null) { //track change. stop and unload current.
            if (music1.isPlaying())
                music1.stop();
            music1.dispose();
        }

        currentTrack = track;

        music1 = loadMusic(track);
        music1.setLooping(true);
        music1.play();
    }

    private Music loadMusic(MusicTrack track){
        switch (track){
            case Ambient:
                return Gdx.audio.newMusic(ambience);
            case Intense:
                return Gdx.audio.newMusic(intense);
            case Grave:
            default:
                return Gdx.audio.newMusic(gravity);
        }
    }

    public void setNextTrack(MusicTrack track, float crossFadeDuration){
        fading = false;
        if (music1 == null || !music1.isPlaying()){
            begin(track);
            return;
        }

        if (track == currentTrack)
            return;

        nextTrack = track;
        music2 = loadMusic(track);
        music2.setLooping(true);
        music2.play();
        music2.setVolume(0);
        this.crossFadeDuration = crossFadeDuration;
        crossFadeTime = 0;
    }

    public void stop (boolean fade){
        if (music2 != null)
            music2.stop();
        if (fade){
            nextTrack = null;
            fading = true;
            fadeTime = 0;
            return;
        }
        nextTrack = null;
        if (music1 != null)
            music1.stop();
    }

    public void update (float delta){
        if (music1 != null) {
            if (fading) {
                fadeTime += delta;
                if (fadeTime > FADE_DURATION) {
                    fading = false;
                    stop(false);
                    return;
                }
                float fadeAlpha = 1f - Interpolation.pow2Out.apply(fadeTime / FADE_DURATION);
                music1.setVolume(fadeAlpha);
            } else {
                music1.setVolume(1f);
            }
        }

        //cross fade
        if (nextTrack != null){
            crossFadeTime += delta;
            float fadeAlpha = crossFadeTime / crossFadeDuration;
            if (fadeAlpha < 1f){
                music1.setVolume(1f - fadeAlpha);
                music2.setVolume(fadeAlpha);
            } else {
                music1.stop();
                music1.dispose();
                music1 = music2;
                music2 = null;
                music1.setVolume(1);
                currentTrack = nextTrack;
                nextTrack = null;
            }
        }
    }
}
