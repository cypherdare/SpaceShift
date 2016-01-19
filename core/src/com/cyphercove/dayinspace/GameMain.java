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

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.*;

public class GameMain extends ApplicationAdapter {

	private enum State {
		Title,
		Prologue,
		Round,
		RoundChange,
		Epilogue
	}

	public interface PlatformResolver {
		public Coordinate getFullScreenResolution ();
	}

	private State state = State.Title;
	private GamePlayScene gamePlayScene;
	private Story currentStory;
	private BookendScene bookendScene;
	TitleScreenScene titleScreenScene;

	MusicManager musicManager;
	private Assets assets;
	private SpriteBatch batch;
	private Texture img;
	private Viewport viewport;
	private FullScreenFader stageTransitionFader;
	private InputMultiplexer inputMultiplexer;
	private State afterTransitionState;
	private ObjectSet<Disposable> disposables = new ObjectSet<>();
	float frameRate;
	PlatformResolver resolver;
	Coordinate fullScreenResolution;
	private boolean fullScreen = false;

	public GameMain (PlatformResolver resolver){
		this.resolver = resolver;
	}

	public void setFullScreen (boolean fullScreen){
		if (this.fullScreen == fullScreen)
			return;
		if (fullScreen){
			if (fullScreenResolution == null)
				return;
			Gdx.graphics.setDisplayMode(fullScreenResolution.x, fullScreenResolution.y, true);
		} else {
			Gdx.graphics.setDisplayMode(Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT, false);
		}
		this.fullScreen = fullScreen;
	}

	public void toggleFullScreen (){
		setFullScreen(!fullScreen);
	}

	public boolean isFullScreenAvailable (){
		return fullScreenResolution != null;
	}

	public boolean isFullScreen() {
		return fullScreen;
	}

	@Override
	public void create () {
		if (resolver != null){
			fullScreenResolution = resolver.getFullScreenResolution();
			if (fullScreenResolution != null){
				Util.log("Full screen resolution: " + fullScreenResolution);
			}
		}

		batch = new SpriteBatch();
		disposables.add(batch);

		assets = new Assets();
		disposables.add(assets);

		musicManager = new MusicManager(assets);
		disposables.add(musicManager);

		gamePlayScene = new GamePlayScene(this, assets, musicManager, batch);
		disposables.add(gamePlayScene);

		bookendScene = new BookendScene(this, assets, batch);
		titleScreenScene = new TitleScreenScene(this, assets, batch);

		img = new Texture("badlogic.jpg");
		viewport = new ExtendViewport(240, 400);

		stageTransitionFader = new FullScreenFader(0.4f, true, 1f, Color.BLACK);
		disposables.add(stageTransitionFader);

		inputMultiplexer = new InputMultiplexer(firstInputProcessor);
		Gdx.input.setInputProcessor(inputMultiplexer);

		beginTitleState();

	}


	@Override
	public void resize (int width, int height){
		viewport.update(width, height, true);
		gamePlayScene.resize(width, height);
		bookendScene.resize(width, height);
		titleScreenScene.resize(width, height);
	}

	@Override
	public void render () {

		float dt = Math.min(0.05f, Gdx.graphics.getDeltaTime());
		musicManager.update(dt);



		boolean fadeInComplete = afterTransitionState == null && stageTransitionFader.isDone();

		switch (state){
			case Title:
				titleScreenScene.render(dt, fadeInComplete);
				break;
			case Prologue:
			case Epilogue:
				bookendScene.render(dt, fadeInComplete);
				break;
			case Round:
				gamePlayScene.render(dt, fadeInComplete);
				break;
			case RoundChange:
				beginNextRound();//nothing to draw. This state is only to wait for fader before changing the round.
				break;
		}

		stageTransitionFader.render(dt);
		if (afterTransitionState != null && stageTransitionFader.isDone()){
			finishStateTransition();
		}

		float newFrameRate = Gdx.graphics.getFramesPerSecond();
		if (newFrameRate != frameRate){
			frameRate = newFrameRate;
			if (Util.DEBUG) Util.log(frameRate);
		}
	}

	private void finishStateTransition (){
		stageTransitionFader.setOn(false);
		state = afterTransitionState;
		afterTransitionState = null;
		while (inputMultiplexer.size() > 1)
			inputMultiplexer.removeProcessor(inputMultiplexer.size() - 1);
		switch (state){
			case Title:
				inputMultiplexer.addProcessor(titleScreenScene.getInputProcessor());
				break;
			case Prologue:
			case Epilogue:
				inputMultiplexer.addProcessor(bookendScene.getInputProcessor());
				break;
			case Round:
				inputMultiplexer.addProcessor(gamePlayScene.getInputProcessor());
				break;
		}
	}

	void beginTitleState (){
		transitionState(State.Title);
	}

	public void beginNewGame (){
		bookendScene.reset(true, null);
		transitionState(State.Prologue);
		currentStory = new Story(assets);
	}

	void beginEpilogue (){
		bookendScene.reset(false, currentStory);
		transitionState(State.Epilogue);
	}

	void beginContinueGame (){
		transitionState(State.Round);
		//TODO load old story into currentStory
		currentStory.generateNextRound(gamePlayScene);
	}

	void onRoundEnded (){
		currentStory.onRoundCompleted(gamePlayScene);
		if (currentStory.hasNextRound())
			transitionState(State.RoundChange);
		else
			beginEpilogue();
	}

	void onIntroEnded (){
		transitionState(State.RoundChange);
	}

	void onGameOver (){
		transitionState(State.Title);
	}

	void beginNextRound (){
		currentStory.generateNextRound(gamePlayScene);
		transitionState(State.Round);
	}

	private void transitionState (State nextState){
		stageTransitionFader.setOn(true);
		afterTransitionState = nextState;
	}

	/** Handles debug keystrokes, and preventing any other input processors from accepting user input while the
	 * screen is transitioning with the fader.
	 */
	InputProcessor firstInputProcessor = new InputProcessor(){
		public boolean touchDown (int screenX, int screenY, int pointer, int button) {
			if (!stageTransitionFader.isDone()) return true;
			return false;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			if (!stageTransitionFader.isDone()) return true;
			return false;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			if (!stageTransitionFader.isDone()) return true;
			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			if (!stageTransitionFader.isDone()) return true;
			return false;
		}

		@Override
		public boolean scrolled(int amount) {
			if (!stageTransitionFader.isDone()) return true;
			return false;
		}

		public boolean keyDown (int keycode) {
			if (Util.DEBUG){
				if (keycode == Input.Keys.R)
					assets.reloadShaders();
				else if (keycode == Input.Keys.F)
					gamePlayScene.fxaa = !gamePlayScene.fxaa;
				else if (keycode == Input.Keys.S && state == State.Round)
					onRoundEnded();
				else if (keycode == Input.Keys.B)
					gamePlayScene.cheatGainStepBoostPickup();
			}
			if (keycode == Input.Keys.F12)
				toggleFullScreen();
			if (!stageTransitionFader.isDone()) return true;
			return false;
		}

		@Override
		public boolean keyUp(int keycode) {
			if (!stageTransitionFader.isDone()) return true;
			return false;
		}

		@Override
		public boolean keyTyped(char character) {
			if (!stageTransitionFader.isDone()) return true;
			return false;
		}
	};

	@Override
	public void dispose (){
		for (Disposable disposable : disposables) disposable.dispose();
	}
}
