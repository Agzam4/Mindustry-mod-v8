package agzam4.render;

import agzam4.AgzamMod;
import arc.Core;
import arc.Files.FileType;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.gl.Shader;

public class ModShaders {

	public static LoadShader light;
	
    public static class LightShader extends LoadShader{
        public Color ambient = new Color(0.01f, 0.01f, 0.04f, 0.99f);

        public LightShader(){
            super("light");
        }

        @Override
        public void apply(){
            setUniformf("u_ambient", ambient);
        }
    }
    
    public static class LoadShader extends Shader{
        public LoadShader(String frag){
            super(Core.files.internal("shaders/screenspace.vert"), getShaderFi(frag + ".frag"));
        }
    }

    public static Fi getShaderFi(String file){
    	new Fi("", FileType.classpath);
        return AgzamMod.mod.file.child("shaders/" + file);
    }
    
}
