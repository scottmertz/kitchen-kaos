package com.kitchenkaos.game.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.kitchenkaos.game.KitchenKaosGame;

/**
 * Desktop entry point. This is the class Windows/Mac/Linux actually
 * runs — its only job is window configuration, then it hands off to
 * KitchenKaosGame (in the core module) for everything else. No
 * gameplay logic belongs in this file, ever.
 */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Kitchen Kaos");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new KitchenKaosGame(), config);
    }
}