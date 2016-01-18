package com.cyphercove.dayinspace.desktop;

import com.badlogicgames.packr.Packr;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Darren on 1/12/2016.
 */
public class RunPackr {

    public static void main (String[] args){
        String workingDir = System.getProperty("user.dir");

        Packr.Config config = new Packr.Config();
        config.platform = Packr.Platform.mac;
        config.jdk = workingDir + "/openjdk/openjdk-1.7.0-u80-unofficial-macosx-x86_64-image.zip";
        config.executable = "Day In Space";
        config.jar = "desktop/build/libs/desktop-1.0.jar";
        config.mainClass = "com.cyphercove.dayinspace.desktop.DesktopLauncher";
        config.resources.add(workingDir + "/core/assets/");
        config.vmArgs = Arrays.asList("-Xmx1G");
        config.minimizeJre = new String[] { "jre/lib/rt/com/sun/corba", "jre/lib/rt/com/sun/jndi" };
        config.outDir = workingDir + "out-mac";

        try {
            new Packr().pack(config);
        } catch (IOException e){
            System.out.print(e.toString());
        }
    }
}
