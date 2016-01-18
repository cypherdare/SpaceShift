package com.cyphercove.dayinspace;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class SpeakingLabel extends Label {

    private CharSequence completeText;
    private float charsPerSecond;

    Sound longSound, shortSound;

    private static final float LONG_TEXT_SOUND_DURATION = 1.5f;
    private static final float SHORT_TEXT_SOUND_MIN_DURATION = 0.4f;
    private static final float LONG_TEXT_SOUND_MIN_DURATION = 1.0f;

    public SpeakingLabel(Assets assets, CharSequence text, Skin skin, String styleName, float charsPerSecond) {
        super("", skin, styleName);
        completeText = text;
        this.charsPerSecond = charsPerSecond;

        longSound = assets.sfx.get("text");
        shortSound = assets.sfx.get("text_short");
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
        while (duration > LONG_TEXT_SOUND_DURATION){
            duration -= LONG_TEXT_SOUND_DURATION;
            sound(longSound, delay + count++ * LONG_TEXT_SOUND_DURATION);
        }
        if (duration >= SHORT_TEXT_SOUND_MIN_DURATION || count == 0)
            sound(duration > LONG_TEXT_SOUND_MIN_DURATION ? longSound : shortSound, delay + count * LONG_TEXT_SOUND_DURATION);
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