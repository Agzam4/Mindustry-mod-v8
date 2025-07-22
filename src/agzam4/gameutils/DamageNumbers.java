package agzam4.gameutils;

import static agzam4.ModWork.*;

import agzam4.AgzamMod;
import agzam4.ModWork;
import agzam4.render.Text;
import agzam4.utils.Prefs;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Align;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.game.EventType.UnitDamageEvent;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.ui.Fonts;

public class DamageNumbers {

	private static Seq<UnitNumbers> damages = null;
	
	public static void init() {
		damages = new Seq<UnitNumbers>();
	}
	
	public static void dispose() {
		damages = null;
	}
	
	private static int updates = 0;
	
	public static void draw() {
		if(damages == null) return;
		if(!Prefs.settings.bool("show-units-health")) return;
		
		for (int i = 0; i < Groups.unit.size(); i++) {
			Unit u = Groups.unit.index(i);
			if(u.dead()) continue;
			
			if(AgzamMod.hideUnits) {
				Draw.reset();
				Draw.z(Layer.buildBeam);
				Draw.color(u.team().color, Color.black, .25f);
				Fill.square(u.x, u.y, u.hitSize/2f + 1, 45);
				Draw.color(u.team().color);
				Fill.square(u.x, u.y, u.hitSize/2f, 45);
				Draw.reset();
			}
			
			boolean found = false;

			Text.font(Fonts.outline);
			Draw.z(Layer.playerName);
			for (int j = 0; j < damages.size; j++) {
				UnitNumbers numbers = damages.get(j);
				if(numbers.id == u.id) {
					numbers.unit = u;
					numbers.draw(updates);
					found = true;
					break;
				}
			}
			Text.size();
			Draw.color();
			
			if(!found) {
				damages.add(new UnitNumbers(u, updates));
			}
		}
		
		for (int i = 0; i < damages.size; i++) {
			if(damages.get(i).lastUpdate != updates) {
				damages.remove(i);
				break;
			}
		}
		
		updates++;
	}

	public static void unitDamageEvent(UnitDamageEvent e) {
//		if(e.unit == null) return;
//		if(e.bullet == null) return;
//		
//		if(damages.size > maxNumsSize) return;
//		int unitId = e.unit.id;
//		for (int i = 0; i < damages.size; i++) {
//			if(damages.get(i).unitId == unitId) {
////				e.bullet.type().status;
//				DamageNumber n = damages.get(i);
//				n.increase(e.bullet.damage);
//				return;
//			}
//		}
//		damages.add(new DamageNumber(e.unit, e.bullet.damage, e.bullet.vel, e.unit.id));
	}
	
	private static class UnitNumbers {

		public Unit unit;
		public int lastUpdate;
		public final int id;

		float lastHealth;
		
		float hScale = 1f;
		int hColor = 0;
		
		int hTime = 0;

		float dmg;
		int elapsed;
		float dps;
		
		public UnitNumbers(Unit unit, int updates) {
			lastUpdate = updates;
			id = unit.id;
			this.unit = unit;
		}
		
		public void draw(int lastUpdate) {
			if(unit == null) return;
			this.lastUpdate = lastUpdate;
			
			float change = 0;
			if(unit.health+unit.shield == lastHealth) {
				if(elapsed != 0 && dmg != 0) {
					dps = dmg/elapsed;
				}
				dmg = 0;
				elapsed = 0;
			} else {
				change = unit.health+unit.shield-lastHealth;
				if(change > 0) {
					if(hColor < 100) hColor+=10;
				} else {
					if(hColor > -100) hColor-=10;
				}
				hTime = 60*2;
				hScale = 1.5f;
				dmg-=change;
				elapsed++;
			}
			lastHealth = unit.health+unit.shield;
			
			if(elapsed > 60) {
				if(dmg != 0) {
					dps = dmg/elapsed;
					dmg = 0;
					elapsed = 0;
				}
			}

			float cursorX = Core.input.mouseWorldX();
			float cursorY = Core.input.mouseWorldY();
			
			if(Mathf.len2(unit.getX()-cursorX, unit.getY()-cursorY) <= unit.hitSize()*unit.hitSize()/2f) {
				hTime = 60;
			}

			if(hTime > 0) {
				hTime--;
				if(hTime < 110)
				hScale = (hScale - 1)/1.2f + 1;
			} else {
				hScale = hScale/1.5f;
			}

			if(hScale > 0.1f) {
				int index = (int) (unit.health*gradient/unit.maxHealth);
				if(index < 0) index = 0;
				if(index >= gradient) index = gradient-1;

				Text.size(hScale);

				float x = unit.x;
				float y = unit.y + unit.hitSize*hScale/2f + unit.hitSize/2f;
				Draw.color();
				Text.at("|", x, y);
				
				if(unit.shield > 0) {
					Draw.color(1f, .9f, .5f);
					Text.at(" " + StatusEffects.shielded.emoji() + roundSimple(unit.shield), x, y, Align.left);
				} else {
					Draw.color(rs[index], gs[index], bs[index]);
					Text.at(" " + roundSimple(unit.health*unit.team.rules().unitHealthMultiplier*Vars.state.rules.unitHealthMultiplier), x, y, Align.left);
				}
				
				if(dps == 0) {
					Draw.color(unit.team.color);
					Text.at(unit.type.localizedName + " ", x, y, Align.right);
				} else {
					boolean a = dps > 0;
					Draw.color(a ? 1f : .2f, a ? .2f : .8f, a ? .1f : .4f);
					Text.at((a ? "" : "+") + roundSimple(Math.abs(dps*unit.team.rules().unitHealthMultiplier*Vars.state.rules.unitHealthMultiplier)) + " ", x, y, Align.right);
				}
			} else {
				dps = 0;
				elapsed = 0;
			}
			
//			if(dScale > 0.1f && damage != 0) {
//				float t = t(dTime, 60, 120)/2f;//dTime > 120 ? (1f - (dTime-120)/60f) : 1;
//				MyDraw.textColor("" + round(unit.health), 
//						unit.getX() + dx*dScale, unit.getY()+unit.hitSize,
//						1f, .2f + t*.8f, .1f + t*.9f, dScale);
//			} else {
//				if(dScale <= 0.1f) damage = 0;
//			}
		}
	}

	
	/*
	private static float t(final float value, final float from, final float to) {
		if(value <= from) return 0;
		if(value >= to) return 1;
		return (value-from)/(to-from);
	}
	
	
	
	private static class DamageNumber {

		public boolean needRemove = false;
		private float x, y;
		private float damage;
		private float angle;
		private float acceleration;
		private float accelerationY;
		private float vel;
		private float velY;
		float scale = 1f;
		private int lifetime;
		Unit position;
		
		private final int unitId;
		
		public DamageNumber(Unit position, float damage, Vec2 v, int unitId) {
			this.unitId = unitId;
			this.position = position;
			x = position.getX();
			y = position.getY() + 16;
			this.damage = damage;
			angle = v.angle();
			acceleration = 0;
			accelerationY = 0;
			
			lifetime = 0;
			scale = 2f;
			
			vel = 3;
			
			Log.info("Damage: @", damage);
		}
		
		private void increase(float damage) {
			scale = 2f;
			lifetime = 0;
			this.damage += damage;
		}

		public void draw() {
//			vel -= acceleration;
//			acceleration += .01f;
//			accelerationY += .01f;
//			velY += accelerationY;
//			if(vel > 0) {
//				x += Mathf.cosDeg(angle)*vel;
//				y += Mathf.sinDeg(angle)*(vel+velY);
//			}
			lifetime++;
			
			float nor = 0;
			if(lifetime < 60) {
				scale = (scale - 1)/1.2f + 1;
				nor = 1f - lifetime/60f;
			} else {
				scale /= 1.5;
				if(scale < .01f) {
					needRemove = true;
				}
			}
			
			if(position == null) {
				needRemove = true;
				return;
			}
			
//			MyDraw.textColor("" + damage, position.getX(), position.getY()+position.hitSize, 1f, nor, nor, 1f, scale);
			
//			float nor = Mathf.sinDeg(90 + lifetime*5f);
//			if(nor < 0) nor = -nor;
//					
//			if(lifetime > 60*5) {
//				MyDraw.textColor(damage, x, y, 1f, nor, nor, lifetime/60f/5f);
//			} else {
//			}
			
//			MyDraw.normal(cursor, damageColor, x, y, Layer.playerName);
		}
	}
	*/
}
