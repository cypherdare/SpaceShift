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
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.*;

public class Assets implements Disposable{
    public static final String MAIN_ATLAS = "main";
    public static final String ATLAS_EXTENSION = ".atlas";
    static final String UI_SKIN = "uiskin.json";
    static final String FONT = "shareTechMono";
    public static final String CATALOGUE_NAME = "catalogue.txt";

    //A is for left and right(when flipped). B is for middle
    Array<Pixmap> typeAMaps, typeBMaps;
    ObjectMap<String, Pixmap> completeMaps;
    ObjectMap<String, FaceMood> faceMoods;
    ObjectMap<String, Sound> sfx;
    ObjectMap<String, FileHandle> music;
    String[] gameOverDialogue;
    FaceMood[] gameOverDialogueMoods;
    String[] prologueDialogue;
    FaceMood[] prologueDialogueMoods;
    String[] epilogueDialoguePart1;
    String[] epilogueDialoguePart2;
    FaceMood[] epilogueMoods;
    Array<TextureAtlas.AtlasRegion> monchuPieces;
    ObjectSet<Disposable> disposables = new ObjectSet<>();
    ObjectSet<Shader> shaders = new ObjectSet<>();
    Board.Parameters[] boardParameters;

    AssetManager assetManager;

    BitmapFont font;
    public TextureAtlas mainAtlas;
    public Skin skin;
    public final Shader curvatureShader;
    public final Shader curvatureDamageShader;
    public final Shader fxaaShader;
    public final Shader solidColorShader;
    public final Shader glassShader;

    ObjectMap<String, Sprite> sprites;
    public ObjectMap<String, Array<Tile>> tiles;
    public TextureRegion white;
    public TextureRegion lightBeam;
    public TextureRegion lightPoint;
    Texture backgroundTexture;
    Texture brokenGlassTexture;

    Tile switchPressedTile;
    Tile secondarySwitchPressedTile;
    Tile switchUnpressedTile;
    Tile secondarySwitchUnpressedTile;

    public Assets (){
        Json json = new Json(){
            public <T> T readValue (Class<T> type, Class elementType, JsonValue jsonData) {
                try {
                    return super.readValue(type, elementType, jsonData);
                } catch (SerializationException e){
                    if (jsonData.isString()) {
                        String string = jsonData.asString();
                        if (type == int.class || type == Integer.class) {
                            string = string.charAt(0) == '#' ? string.substring(1) : string;
                            int r = Integer.valueOf(string.substring(0, 2), 16);
                            int g = Integer.valueOf(string.substring(2, 4), 16);
                            int b = Integer.valueOf(string.substring(4, 6), 16);
                            int a = string.length() != 8 ? 255 : Integer.valueOf(string.substring(6, 8), 16);
                            Integer intValue = (r << 24) | (g << 16) | (b << 8) | a;
                            return (T) intValue;
                        }
                    }
                }
                return null;
            }
        };
        boardParameters = json.fromJson(Board.Parameters[].class, Gdx.files.internal("boardParams.json"));


        typeAMaps = new Array<>();
        typeBMaps = new Array<>();
        completeMaps = new ObjectMap<>();
        populateMapArray("typeAMaps", typeAMaps);
        populateMapArray("typeBMaps", typeBMaps);
        populateNamedMapMap("completeMaps", completeMaps);

        sfx = new ObjectMap<>();
        populateSFXMap("sfx", sfx);

        music = new ObjectMap<>();
        populateMusicMap("music", music);

        assetManager = new AssetManager();
        assetManager.load(UI_SKIN, Skin.class, new SkinLoader.SkinParameter(MAIN_ATLAS + ATLAS_EXTENSION));

        assetManager.finishLoading();

        skin = assetManager.get(UI_SKIN, Skin.class);
        mainAtlas = assetManager.get(MAIN_ATLAS + ATLAS_EXTENSION, TextureAtlas.class);

        white = mainAtlas.findRegion("white");
        lightBeam = mainAtlas.findRegion("lightBeam");
        lightPoint = mainAtlas.findRegion("lightPoint");

        SpriteData spriteData = json.fromJson(SpriteData.class, Gdx.files.internal("sprites.json"));
        sprites = new ObjectMap<>(spriteData.data.size);
        for (ObjectMap.Entry<String, Sprite.Parameters> entry : spriteData.data.entries()){
            sprites.put(entry.key, new Sprite(entry.value, mainAtlas));
        }

        TileData tileData = json.fromJson(TileData.class, Gdx.files.internal("tiles.json"));
        tiles = new ObjectMap<>(tileData.data.size);
        for (ObjectMap.Entry<String, Tile.Parameters[]> entry : tileData.data.entries()){
            Array<Tile> array = new Array<>(entry.value.length * 2);
            for (Tile.Parameters parameters : entry.value) {
                array.add(new Tile(parameters, mainAtlas));
                if (parameters.includeFlipped) array.add(new Tile(parameters, mainAtlas, true));
            }
            tiles.put(entry.key, array);
        }
        switchPressedTile = tiles.get("switchPressedTile").first();
        secondarySwitchPressedTile = tiles.get("secondarySwitchPressedTile").first();
        switchUnpressedTile = tiles.get("switchUnpressedTile").first();
        secondarySwitchUnpressedTile = tiles.get("secondarySwitchUnpressedTile").first();

        font = new BitmapFont(Gdx.files.internal(FONT + ".fnt"), mainAtlas.findRegion(FONT));
        disposables.add(font);

        backgroundTexture = new Texture(Gdx.files.internal("background.png"));
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        backgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        disposables.add(backgroundTexture);

        brokenGlassTexture = new Texture(Gdx.files.internal("brokenGlass.png"));
        brokenGlassTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        brokenGlassTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        disposables.add(brokenGlassTexture);

        curvatureShader = new Shader("curvature");
        shaders.add(curvatureShader);

        curvatureDamageShader = new Shader("curvature", "curvature", "#define DAMAGE");
        shaders.add(curvatureDamageShader);

        fxaaShader = new Shader("fxaa");
        shaders.add(fxaaShader);

        solidColorShader = new Shader("solidColor");
        shaders.add(solidColorShader);

        glassShader = new Shader("glass");
        glassShader.setUniformInt("u_glassTexture", 1);
        shaders.add(glassShader);

        faceMoods = json.fromJson(FaceMoodData.class, Gdx.files.internal("faceMoods.json")).data;
        for (FaceMood mood : faceMoods.values())
            mood.init(mainAtlas);

        monchuPieces = mainAtlas.findRegions("monchu-pieces");

        SpeechData dialogue = json.fromJson(SpeechData.class, Gdx.files.internal("speeches.xjson"));
        gameOverDialogue = dialogue.strings.get("gameOverDialogue");
        gameOverDialogueMoods = new FaceMood[dialogue.moods.get("gameOverDialogue").length];
        populateMoods(gameOverDialogueMoods, dialogue.moods.get("gameOverDialogue"));
        prologueDialogue = dialogue.strings.get("prologueDialogue");
        prologueDialogueMoods = new FaceMood[dialogue.moods.get("prologueDialogue").length];
        populateMoods(prologueDialogueMoods, dialogue.moods.get("prologueDialogue"));
        epilogueDialoguePart1 = dialogue.strings.get("epilogueDialoguePart1");
        epilogueDialoguePart2 = dialogue.strings.get("epilogueDialoguePart2");
        epilogueMoods = new FaceMood[dialogue.moods.get("epilogueDialogue").length];
        populateMoods(epilogueMoods, dialogue.moods.get("epilogueDialogue"));
    }

    /**For safety, pads moods with "default" if the count doesn't match up correctly. */
    public void populateMoods (FaceMood[] boardMoods, String[] moodNames){
        for (int i = 0; i < boardMoods.length; i++) {
            if (moodNames != null && moodNames.length > i)
                boardMoods[i] = faceMoods.get(moodNames[i]);
            else
                boardMoods[i] = faceMoods.get("default");
        }
    }

    private FileHandle[] readDirectoryCatalogue (String directory){
        String[] fileNames = Gdx.files.internal(directory + "/" + CATALOGUE_NAME).readString().split("\n");
        FileHandle[] files = new FileHandle[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            files[i] = Gdx.files.internal(directory + "/" + fileNames[i].replaceAll("\\s+",""));
        }
        return files;
    }

    private void populateMapArray (String directory, Array<Pixmap> target){
        FileHandle[] files = readDirectoryCatalogue(directory);
        for (FileHandle file : files){
            if (file.extension().equalsIgnoreCase("png")){
                target.add(new Pixmap(file));
            }
        }
        disposables.addAll(target);
    }

    private void populateNamedMapMap (String directory, ObjectMap<String, Pixmap> target){
        FileHandle[] files = readDirectoryCatalogue(directory);
        for (FileHandle file : files){
            if (file.extension().equalsIgnoreCase("png")){
                Pixmap pixmap = new Pixmap(file);
                target.put(file.nameWithoutExtension(), pixmap);
                disposables.add(pixmap);
            }
        }
    }

    private void populateSFXMap (String directory, ObjectMap<String, Sound> target){
        FileHandle[] files = readDirectoryCatalogue(directory);
        for (FileHandle file : files){
            String fileExtension = file.extension();
            if (fileExtension.equalsIgnoreCase("mp3") || fileExtension.equalsIgnoreCase("wav")
                    || fileExtension.equalsIgnoreCase("ogg")){
                Sound sound = Gdx.audio.newSound(file);
                target.put(file.nameWithoutExtension().replace("DIS_SFX_", ""), sound);
                disposables.add(sound);
            }
        }
    }

    private void populateMusicMap (String directory, ObjectMap<String, FileHandle> target){
        FileHandle[] files = readDirectoryCatalogue(directory);
        for (FileHandle file : files){
            String fileExtension = file.extension();
            if (fileExtension.equalsIgnoreCase("mp3") || fileExtension.equalsIgnoreCase("wav")
                    || fileExtension.equalsIgnoreCase("ogg")){
                target.put(file.nameWithoutExtension().replace("DIS_MX_", ""), file);
            }
        }
    }

    public void reloadShaders (){
        for (Shader shader : shaders){
            shader.load();
        }
    }

    public void dispose (){
        for (Disposable disposable : disposables) disposable.dispose();
        for (Shader shader : shaders) shader.dispose();
    }
}
