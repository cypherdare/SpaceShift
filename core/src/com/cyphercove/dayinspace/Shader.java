package com.cyphercove.dayinspace;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.utils.*;

/**
 * ShaderProgram wrapper that makes it easy to reload the shaders and reapply uniforms while the app is running.
 */
public class Shader implements Disposable {
    public static final String VERT_FILE_SUFFIX = ".vert.glsl";
    public static final String FRAG_FILE_SUFFIX = ".frag.glsl";


    private ShaderProgram program;
    private String vertFileName;
    private String fragFileName;
    private String preprocessorDirective;

    private ObjectMap<String, float[]> floatUniforms = new ObjectMap<String, float[]>();
    private ObjectIntMap<String> intUniforms = new ObjectIntMap<>();
    private boolean floatUniformsDirty;
    private boolean intUniformsDirty;

    public Shader (String filesName){
        this(filesName, filesName);
    }

    public Shader (String vertFileName, String fragFileName){
        this(vertFileName, fragFileName, null);
    }

    public Shader (String vertFileName, String fragFileName, String preprocessorDirective){
        this.vertFileName = vertFileName + VERT_FILE_SUFFIX;
        this.fragFileName = fragFileName + FRAG_FILE_SUFFIX;
        this.preprocessorDirective = preprocessorDirective;
        load();
    }

    public void load (){
        if (program != null) program.dispose();
        floatUniformsDirty = true;
        intUniformsDirty = true;
        String vertex = Gdx.files.internal(vertFileName).readString();
        String fragment = Gdx.files.internal(fragFileName).readString();
        if (preprocessorDirective != null){
            vertex = preprocessorDirective + "\n" + vertex;
            fragment = preprocessorDirective + "\n" + fragment;
        }
        program = new ShaderProgram(vertex, fragment){
            public void begin () {
                super.begin();
                applyUniforms();
            }
        };
        if (! program.isCompiled()) {
            Util.logError(vertFileName + " and " + fragFileName + " failed to compile:");
            Util.logError(program.getLog());
        }
    }

    public void setUniformFloat (String uniformName, float... values){
        floatUniforms.put(uniformName, values);
        floatUniformsDirty = true;
    }

    public void setUniformInt (String uniformName, int value){
        intUniforms.put(uniformName, value);
        intUniformsDirty = true;
    }

    private void applyUniforms (){
        if (floatUniformsDirty) {
            for (ObjectMap.Entry<String, float[]> entry : floatUniforms) {
                float[] value = entry.value;
                switch (entry.value.length) {
                    case 1:
                        program.setUniformf(entry.key, value[0]);
                        break;
                    case 2:
                        program.setUniformf(entry.key, value[0], value[1]);
                        break;
                    case 3:
                        program.setUniformf(entry.key, value[0], value[1], value[2]);
                        break;
                    case 4:
                        program.setUniformf(entry.key, value[0], value[1], value[2], value[3]);
                        break;

                }
            }
            floatUniformsDirty = false;
        }
        if (intUniformsDirty) {
            for (ObjectIntMap.Entry<String> entry : intUniforms){
                program.setUniformi(entry.key, entry.value);
            }
            intUniformsDirty = false;
        }
    }

    public void begin (){
        program.begin();
    }

    public void end(){
        program.end();
    }

    public ShaderProgram getProgram (){
        return program;
    }

    @Override
    public void dispose() {
        program.dispose();
    }
}
