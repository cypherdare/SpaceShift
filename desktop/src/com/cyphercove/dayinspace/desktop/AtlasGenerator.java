/*******************************************************************************
 * Copyright 2015 Cypher Cove LLC
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
package com.cyphercove.dayinspace.desktop;

import com.badlogic.gdx.tools.*;
import com.badlogic.gdx.tools.texturepacker.*;
import com.cyphercove.dayinspace.*;
import org.apache.commons.io.*;
import org.apache.commons.io.filefilter.*;

import java.io.*;
import java.util.*;

public class AtlasGenerator {

    static final String SOURCE_DIR = "planning/Texture Atlas";
    static final String TARGET_DIR = "core/assets";

    public static void main (String[] args) throws Exception {

        //Delete old pack
        File oldPackFile = new File(TARGET_DIR + "/" + Assets.MAIN_ATLAS + Assets.ATLAS_EXTENSION);
        if (oldPackFile.exists()){
            System.out.println("Deleting old pack file");
            oldPackFile.delete();
        }

        //Delete old font files
        Collection<File> oldFontFiles = FileUtils.listFiles(
                new File(TARGET_DIR),
                new RegexFileFilter(".*\\.fnt"),
                TrueFileFilter.INSTANCE
        );
        for (File file : oldFontFiles){
            System.out.println("Copying font file: " + file.getName());
            FileUtils.deleteQuietly(file);
        }

        //Create PNGs for GIF frames
        GifProcessor gifProcessor = new GifProcessor(0.015f);
        ArrayList<FileProcessor.Entry> gifFrames = gifProcessor.process(SOURCE_DIR, SOURCE_DIR);

        //Pack them
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.atlasExtension = Assets.ATLAS_EXTENSION;
        TexturePacker.process(
                settings,
                SOURCE_DIR,
                TARGET_DIR,
                Assets.MAIN_ATLAS);


        //Copy over any fonts
        Collection<File> fontFiles = FileUtils.listFiles(
                new File(SOURCE_DIR),
                new RegexFileFilter(".*\\.fnt"),
                TrueFileFilter.INSTANCE
        );
        File destDir = new File(TARGET_DIR);
        for (File file : fontFiles){
            System.out.println("Copying font file: " + file.getName());
            FileUtils.copyFileToDirectory(file, destDir);
        }

        //Delete the GIF frames that were generated.
        for (File file : gifProcessor.getGeneratedFiles())
            file.delete();
    }


}