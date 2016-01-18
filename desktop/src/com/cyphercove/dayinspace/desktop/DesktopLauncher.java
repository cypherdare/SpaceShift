package com.cyphercove.dayinspace.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.Array;
import com.cyphercove.dayinspace.Constants;
import com.cyphercove.dayinspace.Coordinate;
import com.cyphercove.dayinspace.GameMain;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.Display;

public class DesktopLauncher {
	public static void main (String[] arg) {

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = Constants.DEFAULT_WINDOW_WIDTH;
		config.height = Constants.DEFAULT_WINDOW_HEIGHT;
		config.title = "Space Shift";
		new LwjglApplication(new GameMain(platformResolver), config);

	}

	static GameMain.PlatformResolver platformResolver = new GameMain.PlatformResolver() {
		@Override
		public Coordinate getFullScreenResolution() {
			Array<DisplayMode> shortList = new Array<>();

			try {
				DisplayMode[] modes = Display.getAvailableDisplayModes(720, 480, 1920, 1080, 32, 32, 30, 120);

				double longestHypotenuse = 0;
				for (int i = 0; i < modes.length; i++) {
					DisplayMode current = modes[i];
					double hypot = hypot(current);
					if (hypot > longestHypotenuse){
						shortList.clear();
						shortList.add(current);
						longestHypotenuse = hypot;
					} else if (hypot == longestHypotenuse)
						shortList.add(current);
				}
			}catch (LWJGLException e){

			}


			System.out.println(shortList);
			DisplayMode best = null;
			for (DisplayMode displayMode : shortList){
				if (best == null || displayMode.getFrequency() > best.getFrequency()){
					best = displayMode;
				}
			}
			if (best != null){
				return new Coordinate(best.getWidth(), best.getHeight());
			}
			return null;
		}
	};

	private static double hypot (DisplayMode mode){
		return Math.sqrt(mode.getWidth() * mode.getWidth() + mode.getHeight() * mode.getHeight());
	}

}
