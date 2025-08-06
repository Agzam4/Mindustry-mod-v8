package agzam4.gameutils;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.geom.Rect;
import arc.util.Reflect;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.EntityGroup;
import mindustry.gen.*;
import mindustry.graphics.Layer;

public class UnitsVisibility {

	public static boolean hide = false;
	
	private static EntityGroup<Drawc> src;
	
	public static void init() {
		src = Groups.draw;
		Groups.draw = new MyDrawComponentGroup(src);
	}

	public static void dispose() {
		if(hide) toggle();
		Groups.draw = src;
	}
	
	public static void draw() {
		if(!hide) return;
	}

	public static void toggle() {
		visibility(!hide);
	}

	public static void visibility(boolean b) {
		hide = b;
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
