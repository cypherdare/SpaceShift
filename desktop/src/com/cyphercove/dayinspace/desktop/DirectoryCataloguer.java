package com.cyphercove.dayinspace.desktop;

import com.cyphercove.dayinspace.*;
import org.apache.commons.io.*;

import java.io.*;

/**
 * Created by dkeese on 1/13/2016.
 */
public class DirectoryCataloguer {

    static final String[] directories = {
            "completeMaps",
            "typeAMaps",
            "typeBMaps",
            "sfx",
            "music"
    };

    public static void main (String[] args){
        String workingDir = System.getProperty("user.dir");
        for (String dir : directories){
            File directory = new File(workingDir + "/" + dir);
            File outputFile = new File(directory, Assets.CATALOGUE_NAME);
            FileUtils.deleteQuietly(outputFile);
            File[] files = directory.listFiles();
            try {
                for (int i = 0; i < files.length; i++) {
                    FileUtils.write(outputFile, files[i].getName() + (i == files.length - 1 ? "" : "\n"), true);
                }
            } catch (IOException e){
                Util.logError(e);
            }
        }
    }
}
