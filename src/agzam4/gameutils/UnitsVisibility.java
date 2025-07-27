package agzam4.gameutils;

import agzam4.UnitTextures;
import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.geom.Rect;
import arc.util.Reflect;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.entities.EntityGroup;
import mindustry.gen.*;
import mindustry.graphics.Layer;

public class UnitsVisibility {

	public static boolean hide = false;
	
	private static UnitTextures[] unitTextures;
	private static TextureRegion minelaser, minelaserEnd;
	
	private static EntityGroup<Drawc> src, custom;
	
	public static void init() {
		src = Groups.draw;
		Groups.draw = custom = new MyDrawComponentGroup(src);

//		minelaser = Core.atlas.find("minelaser");
//		minelaserEnd = Core.atlas.find("minelaser-end");
//		unitTextures = new UnitTextures[Vars.content.units().size];
//		for (int i = 0; i < unitTextures.length; i++) {
//			unitTextures[i] = new UnitTextures(Vars.content.unit(i));
//		}
	}

	public static void dispose() {
		if(hide) toggle();
		Groups.draw = src;
	}
	
	public static void draw() {
		if(!hide) return;
		
//		float opacity = Vars.renderer.animateShields ? 1f : .25f; // TODO: configurable 
//		
//        Rect rect = Core.camera.bounds(new Rect());
//		Draw.reset();
//		Draw.z(Layer.buildBeam);
//		Groups.draw.each(d -> {
//			if(d instanceof Unit u && rect.overlaps(Tmp.r1.setCentered(u.x, u.y, u.clipSize()))) {
//				Tmp.c1.set(u.team().color);
//				Tmp.c1.lerp(Color.black, .25f);
//				
//				Draw.color(Tmp.c1, opacity);
//				Fill.circle(u.x, u.y, u.hitSize * Vars.unitCollisionRadiusScale-1);
//				
//				Draw.color(u.team().color, opacity);
//				Lines.stroke(1f);
//				Lines.circle(u.x, u.y, u.hitSize * Vars.unitCollisionRadiusScale-.5f);
//				Draw.reset();
////				Groups.draw.remove(d);
//			}
//		});
//		Draw.reset();
	}

	public static void toggle() {
		visibility(!hide);
	}

	public static void visibility(boolean b) {
		hide = b;
//		if(hide) {
//			for (int i = 0; i < unitTextures.length; i++) {
//				unitTextures[i].hideTextures();
//				unitTextures[i].hideEngines();
//			}
//			Core.atlas.addRegion("minelaser", UnitTextures.none);
//			Core.atlas.addRegion("minelaser-end", UnitTextures.none);
//			return;
//		}
//		for (int i = 0; i < unitTextures.length; i++) {
//			unitTextures[i].returnTextures();
//			unitTextures[i].returnEngines();
//		}
//		Core.atlas.addRegion("minelaser", minelaser);
//		Core.atlas.addRegion("minelaser-end", minelaserEnd);
	}

	private static class MyDrawComponentGroup extends EntityGroup<Drawc> {

        Rect bounds = new Rect();
        
		public MyDrawComponentGroup(EntityGroup<Drawc> src) {
			super(Drawc.class, src.useTree(), src.mappingEnabled(), Reflect.get(Groups.draw, "indexer"));
		}
		
		
		@Override
		public void draw(Cons<Drawc> cons) {
			if(!hide) {
				super.draw(cons);
				return;
			}

			float opacity = Vars.renderer.animateShields ? 1f : .25f; // TODO: configurable 
			Core.camera.bounds(bounds);
			Draw.reset();
			Draw.z(Layer.buildBeam);
			
			super.draw(d -> {
				if(d instanceof Unit u && bounds.overlaps(Tmp.r1.setCentered(u.x, u.y, u.clipSize()))) {
					Tmp.c1.set(u.team().color);
					Tmp.c1.lerp(Color.black, .25f);
					
					Draw.color(Tmp.c1, opacity);
					Fill.circle(u.x, u.y, u.hitSize * Vars.unitCollisionRadiusScale-1);
					
					Draw.color(u.team().color, opacity);
					Lines.stroke(1f);
					Lines.circle(u.x, u.y, u.hitSize * Vars.unitCollisionRadiusScale-.5f);
					Draw.reset();
					return;
				}
			});
		}
		
	}
}
