package agzam4.render.light;

import static mindustry.Vars.renderer;
import static mindustry.Vars.state;

import agzam4.AgzamMod;
import agzam4.utils.Prefs;
import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.FrameBuffer;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Reflect;
import arc.util.Strings;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.graphics.Shaders;

public class LightRenderer extends mindustry.graphics.LightRenderer {

	private static LightRenderer custom = null;
	private static mindustry.graphics.LightRenderer source = null;
	
	public static float opacity = 0;
	public static LightTypes type = null;

	public static void init() {
		source = Vars.renderer.lights;
		LightTypes type = LightTypes.of(Prefs.settings.string("custom-render", ""));
		apply(type);
	}
	
	
	public static void set(@Nullable LightTypes type) {
		Prefs.settings.put("custom-render", type == null ? "" : type.name());
		apply(type);
	}
	
	public static void apply(@Nullable LightTypes type) {
		LightRenderer.type = type;
		if(type == null) {
			unapply();
			return;
		}
		if(custom == null) custom = new LightRenderer();
		if(custom == Vars.renderer.lights) return;
		Reflect.set(Vars.renderer, "lights", custom);
	}

	public static void unapply() {
		if(source == Vars.renderer.lights) return;
		Reflect.set(Vars.renderer, "lights", source);
		custom = null;
	}

	
    private static final int scaling = 4;
    private float[] vertices = new float[24];
    private FrameBuffer buffer = new FrameBuffer();
    private Seq<Runnable> lights = new Seq<>();
    private Seq<CircleLight> circles = new Seq<>(CircleLight.class);
    private int circleIndex = 0;
    private TextureRegion circleRegion;

    @Override
    public void add(Runnable run) {
//        if(!enabled()) return;
        lights.add(run);
    }

    @Override
    public void add(float x, float y, float radius, Color color, float opacity){
        if(!enabled() || radius <= 0f) return;

        //TODO: clipping.

        float res = Color.toFloatBits(color.r, color.g, color.b, opacity*LightRenderer.opacity);

        if(circles.size <= circleIndex) circles.add(new CircleLight());

        //pool circles to prevent runaway GC usage from lambda capturing
        var light = circles.items[circleIndex];
        light.set(x, y, res, radius);

        circleIndex++;
        
//        line(x, y, x, y, radius, color, opacity);
    }

    @Override
    public void add(float x, float y, TextureRegion region, Color color, float opacity){
        add(x, y, region, 0f, color, opacity);
    }

    @Override
    public void add(float x, float y, TextureRegion region, float rotation, Color color, float opacity){
        if(!enabled()) return;

        float res = color.toFloatBits();
        float xscl = Draw.xscl, yscl = Draw.yscl;
        add(() -> {
            Draw.color(res);
            Draw.alpha(opacity*LightRenderer.opacity);
            Draw.scl(xscl, yscl);
            Draw.rect(region, x, y, rotation);
            Draw.scl();
        });
    }

    @Override
    public void line(float x, float y, float x2, float y2, float stroke, Color tint, float alpha){
        if(!enabled()) return;

        
        add(() -> {
            Draw.color(tint, alpha*LightRenderer.opacity);

            float rot = Mathf.angleExact(x2 - x, y2 - y);
            TextureRegion ledge = Core.atlas.find("circle-end"), lmid = Core.atlas.find("circle-mid");

            float color = Draw.getColorPacked();
            float u = lmid.u;
            float v = lmid.v2;
            float u2 = lmid.u2;
            float v2 = lmid.v;

            Vec2 v1 = Tmp.v1.trnsExact(rot + 90f, stroke);
            float lx1 = x - v1.x, ly1 = y - v1.y,
            lx2 = x + v1.x, ly2 = y + v1.y,
            lx3 = x2 + v1.x, ly3 = y2 + v1.y,
            lx4 = x2 - v1.x, ly4 = y2 - v1.y;

            vertices[0] = lx1;
            vertices[1] = ly1;
            vertices[2] = color;
            vertices[3] = u;
            vertices[4] = v;
            vertices[5] = 0;

            vertices[6] = lx2;
            vertices[7] = ly2;
            vertices[8] = color;
            vertices[9] = u;
            vertices[10] = v2;
            vertices[11] = 0;

            vertices[12] = lx3;
            vertices[13] = ly3;
            vertices[14] = color;
            vertices[15] = u2;
            vertices[16] = v2;
            vertices[17] = 0;

            vertices[18] = lx4;
            vertices[19] = ly4;
            vertices[20] = color;
            vertices[21] = u2;
            vertices[22] = v;
            vertices[23] = 0;

            Draw.vert(ledge.texture, vertices, 0, vertices.length);

            Vec2 v3 = Tmp.v2.trnsExact(rot, stroke);

            u = ledge.u;
            v = ledge.v2;
            u2 = ledge.u2;
            v2 = ledge.v;

            vertices[0] = lx4;
            vertices[1] = ly4;
            vertices[2] = color;
            vertices[3] = u;
            vertices[4] = v;
            vertices[5] = 0;

            vertices[6] = lx3;
            vertices[7] = ly3;
            vertices[8] = color;
            vertices[9] = u;
            vertices[10] = v2;
            vertices[11] = 0;

            vertices[12] = lx3 + v3.x;
            vertices[13] = ly3 + v3.y;
            vertices[14] = color;
            vertices[15] = u2;
            vertices[16] = v2;
            vertices[17] = 0;

            vertices[18] = lx4 + v3.x;
            vertices[19] = ly4 + v3.y;
            vertices[20] = color;
            vertices[21] = u2;
            vertices[22] = v;
            vertices[23] = 0;

            Draw.vert(ledge.texture, vertices, 0, vertices.length);

            vertices[0] = lx2;
            vertices[1] = ly2;
            vertices[2] = color;
            vertices[3] = u;
            vertices[4] = v;
            vertices[5] = 0;

            vertices[6] = lx1;
            vertices[7] = ly1;
            vertices[8] = color;
            vertices[9] = u;
            vertices[10] = v2;
            vertices[11] = 0;

            vertices[12] = lx1 - v3.x;
            vertices[13] = ly1 - v3.y;
            vertices[14] = color;
            vertices[15] = u2;
            vertices[16] = v2;
            vertices[17] = 0;

            vertices[18] = lx2 - v3.x;
            vertices[19] = ly2 - v3.y;
            vertices[20] = color;
            vertices[21] = u2;
            vertices[22] = v;
            vertices[23] = 0;

            Draw.vert(ledge.texture, vertices, 0, vertices.length);
        });
    }

    @Override
    public boolean enabled(){
		return state.rules.lighting /* && state.rules.ambientLight.a > 0.0001f */ && renderer.drawLight;
    }
    

    public static enum LightTypes {
		
		none		(Gl.zero, 		Gl.one),
		dayColors	(Gl.one, 		Gl.one),
		dayGlow		(Gl.srcColor, 	Gl.one),
		dayLight	(Gl.srcAlpha, 	Gl.one),
		
		softNone	(Gl.zero, 		Gl.dstColor),
		softColors	(Gl.one, 		Gl.dstColor),
		softGlow	(Gl.srcColor, 	Gl.dstColor),
		softLight	(Gl.srcAlpha, 	Gl.dstColor),
		
		white		(Gl.zero, 		Gl.srcAlpha),
		nightColors	(Gl.one, 		Gl.srcAlpha),
		nightGlow	(Gl.srcColor, 	Gl.srcAlpha),
		nightLight	(Gl.srcAlpha, 	Gl.srcAlpha);
		
		Blending blending;

		private LightTypes(int s, int d) {
			blending = new Blending(s, d, Gl.one, Gl.oneMinusSrcAlpha);
		}
		
		public static @Nullable LightTypes of(String name) {
			for (var e : values()) {
				if(e.name().equals(name)) return e;
			}
			return null;
		}

		public String kebab() {
			return Strings.camelToKebab(name());
		}
	}

//	private FrameBuffer clip  = new FrameBuffer();

    @Override
    public void draw(){
    	if(type == null) {
    		super.draw();
    		return;
    	}
//    	
//		
//		if(Core.input.keyDown(KeyCode.o)) {
//			if(!wo) {
//				d = (d+1)%src.length;
//				Log.info(s + " " + d);
//				wo = true;
//			}
//		} else {
//			wo = false;
//		}
//		if(!Vars.enableLight){
//			lights.clear();
//			circleIndex = 0;
//			return;
//		}
		/*
		 * 
		 * 
        if(circleRegion == null) circleRegion = Core.atlas.find("circle-shadow");

        buffer.resize(Core.graphics.getWidth()/scaling, Core.graphics.getHeight()/scaling);

        Draw.color();
        buffer.begin(Color.clear);
        Draw.sort(false);
        Gl.blendEquationSeparate(Gl.funcAdd, Gl.max);
        //apparently necessary
        Blending.normal.apply();

        for(Runnable run : lights){
            run.run();
        }
        for(int i = 0; i < circleIndex; i++){
            var cir = circles.items[i];
            Draw.color(cir.color);
            Draw.rect(circleRegion, cir.x, cir.y, cir.radius * 2, cir.radius * 2);
        }
        Draw.reset();
        Draw.sort(true);
        buffer.end();
        Gl.blendEquationSeparate(Gl.funcAdd, Gl.funcAdd);

        Draw.color();
        Shaders.light.ambient.set(state.rules.ambientLight);
        buffer.blit(Shaders.light);

        lights.clear();
        circleIndex = 0;
		 */
		

        if(circleRegion == null) circleRegion = AgzamMod.sprite("circle-shadow");// Core.atlas.find("circle-shadow");

        buffer.resize(Core.graphics.getWidth()/scaling, Core.graphics.getHeight()/scaling);

        Draw.color();
        buffer.begin(Color.clear);
        Draw.sort(false);
        Gl.blendEquationSeparate(Gl.funcAdd, /*Gl.max*/ Gl.funcAdd);
        //apparently necessary
        Blending.additive.apply(); //  Blending.normal.apply();
        Draw.blend(Blending.additive);
        Gl.blendEquationSeparate(Gl.funcAdd, Gl.funcAdd);
        
		for(Runnable run : lights){
			run.run();
		}
        for(int i = 0; i < circleIndex; i++){
            var cir = circles.items[i];
            Draw.color(cir.color);
            Draw.alpha(Draw.getColorAlpha()/3f);
            Draw.rect(circleRegion, cir.x, cir.y, cir.radius * 2, cir.radius * 2);
        }
        
        Draw.reset();
        Draw.sort(true);
        buffer.end();
        Gl.blendEquationSeparate(Gl.funcAdd, Gl.funcAdd);

        Draw.color();
        Shaders.light.ambient.set(state.rules.ambientLight);

        type.blending.apply();//state.rules.ambientLight
		
        Draw.alpha(.5f);
        buffer.blit(Shaders.screenspace); // buffer.blit(Shaders.light);

//        Gl.enable(Gl.blend);
//        Gl.blendEquationSeparate(Gl.funcAdd, Gl.funcAdd);
//        Gl.blendFuncSeparate(Gl.srcColor, Gl.one, Gl.one, Gl.oneMinusSrcAlpha);
//        Blending.additive.apply();
        Blending.normal.apply();
        
        lights.clear();
        lastcircleIndex = circleIndex;
        circleIndex = 0;

//        Blending.additive.apply();
//        for(int i = 0; i < circleIndex; i++){
//            var cir = circles.items[i];
//            Draw.color(cir.color);
//            Draw.rect(circleRegion, cir.x, cir.y, cir.radius * 2, cir.radius * 2);
//        }
//        Blending.normal.apply();
        
    }
    
    int lastcircleIndex = 0;
    

    static class CircleLight{
    	
        float x, y, color, radius;

        public void set(float x, float y, float color, float radius){
            this.x = x;
            this.y = y;
            this.color = color;
            this.radius = radius;
        }
    }

    @Override
    public String toString() {
    	return "Custom" + super.toString();
    }
}
