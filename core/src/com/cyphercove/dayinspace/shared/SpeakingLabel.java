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

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.actions.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class SpeakingLabel extends Label {

    private CharSequence completeText;
    private float charsPerSecond;

    Sound longSound, shortSound;

    private float longSoundDuration = 1.5f;
    private float longSoundMinDuration = 1.0f;
    private float shortSoundMinDuration = 0.4f;

    public SpeakingLabel(CharSequence initialText, Skin skin, String styleName, float charsPerSecond,
                         Sound longSound, float longSoundDuration, float longSoundMinDuration, Sound shortSound,
                         float shortSoundMinDuration) {
        super("", skin, styleName);
        completeText = initialText;
        this.charsPerSecond = charsPerSecond;

        this.longSound = longSound;
        this.shortSound = shortSound;
        this.longSoundDuration = longSoundDuration;
        this.longSoundMinDuration = longSoundMinDuration;
        this.shortSoundMinDuration = shortSoundMinDuration;

        speak(0);
    }

    public void speak (float delay){
        super.setText("");
        clearActions();
        if (completeText.length() == 0)
            return;
        mainAction.restart();
        delayAction.setDuration(delay);
        speakAction.setEnd(completeText.length());
        float duration = completeText.length() / charsPerSecond;
        speakAction.setDuration(duration);
        addAction(speakAction);

        int count = 0;
        while (duration > longSoundDuration){
            duration -= longSoundDuration;
            sound(longSound, delay + count++ * longSoundDuration);
        }
        if (duration >= shortSoundMinDuration || count == 0)
            sound(duration > longSoundMinDuration ? longSound : shortSound, delay + count * longSoundDuration);
    }

    public boolean isSpeaking (){
        return getActions().contains(speakAction, true);
    }

    public void setText (CharSequence newText){
        completeText = newText;
        speak(0);
    }

    public void setText (CharSequence newText, float delay){
        completeText = newText;
        speak(delay);
    }

    private IntAction speakAction = new IntAction(){
        {setInterpolation(Interpolation.linear);}
        protected void update (float percent) {
            super.update(percent);
            SpeakingLabel.super.setText(completeText.subSequence(0, Math.min(completeText.length(), getValue())));
        }
    };

    private DelayAction delayAction = new DelayAction();

    private SequenceAction mainAction = Actions.sequence(
            delayAction, speakAction
    );

    private void sound (final Sound sound){
        addAction(run(new Runnable() {
            @Override
            public void run() {
                sound.play();
            }
        }));
    }

    private void sound (final Sound sound, final float delay){
        if (delay == 0) {
            sound(sound);
            return;
        }

        addAction(sequence(
                delay(delay),
                run(new Runnable() {
                    @Override
                    public void run() {
                        sound.play();
                }})
        ));
    }

}
