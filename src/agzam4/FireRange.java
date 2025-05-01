package agzam4;

import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.game.Teams.TeamData;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.blocks.defense.turrets.Turret;

import static arc.graphics.g2d.Draw.color;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;

public class FireRange {

	public static final Seq<Turret> turrets = ModWork.getBlocks(Turret.class);
	
	public static void draw() {
		if(!ModWork.setting("show-turrets-range")) return;
		Draw.z(Layer.effect);
		
//        color(Color.scarlet);

		final float x = Vars.player.x;
		final float y = Vars.player.y;

		final float hitSize2 = getPlayerHitSize2();
		
		for (int team = 0; team < Vars.state.teams.active.size; team++) {
			TeamData data = Vars.state.teams.active.get(team);
			Lines.stroke(1f, data.team.color);
			if(data.team == Vars.player.team()) continue;
			for (int t = 0; t < turrets.size; t++) {
				if(!canBeAttacked(turrets.get(t))) continue;
				final float tRange = turrets.get(t).range;
				final float extraRange = Vars.tilesize*Vars.tilesize*25*25+hitSize2;
//				final float locateRange2 = tRange*tRange ;//+ getPlayerHitSize2() + ;
				Seq<Building> builds = data.getBuildings(turrets.get(t));
				
				for (int i = 0; i < builds.size; i++) {
					Building b = builds.get(i);
					final float len2 = Mathf.len2(b.getX()-x, b.getY()-y);
					if(len2 > tRange*tRange+extraRange) continue;
					float a = 1f;
					float arc = 1f;
					if(len2 > tRange*tRange) {
						float range = len2-tRange*tRange;
						a = 1f - range/extraRange;
						arc = a;
					}
					
			        color(new Color(data.team.color).a(a));

					final float angle = Mathf.angle(x-b.getX(), y-b.getY()) - 180*arc;
					Lines.arc(b.getX(), b.getY(), tRange, arc, angle);
					if(len2 < tRange*tRange+hitSize2) {
						Lines.line(b.getX(), b.getY(), x, y);
					}
				}
			}
		}
		color();
		
		int tileX = World.toTile(Core.input.mouseWorldX());
		int tileY = World.toTile(Core.input.mouseWorldY());
		if(tileX < 0) return;
		if(tileY < 0) return;
		if(tileX >= Vars.world.width()) return;
		if(tileY >= Vars.world.height()) return;
		
		if(Vars.world.build(tileX, tileY) != null) {
			Building b = Vars.world.build(tileX, tileY);
			if(b.team != Vars.player.team()) {
				if(b.block instanceof Turret) {
					Turret turret = (Turret) b.block;
			        color(b.team.color);
					Lines.arc(b.getX(), b.getY(), turret.range, 1f);
				}
			}
		}
//        Vec2 e = new Vec2(Vars.player.getX(), Vars.player.getY());
//        stroke(e.fout() * 0.9f + 0.6f);
//
//        Fx.rand.setSeed(e.id);
//        for(int i = 0; i < 7; i++){
//            Fx.v.trns(e.rotation, Fx.rand.random(8f, v.dst(e.x, e.y) - 8f));
//            Lines.lineAngleCenter(e.x + Fx.v.x, e.y + Fx.v.y, e.rotation + e.finpow(), e.foutpowdown() * 20f * Fx.rand.random(0.5f, 1f) + 0.3f);
//        }
//
//        e.scaled(14f, b -> {
//            stroke(b.fout() * 1.5f);
//            color(e.color);
//            Lines.line(e.x, e.y, v.x, v.y);
//        });
	}
	
	private static boolean canBeAttacked(Turret turret) {
		if(Vars.player.unit() == null) return false;
		if(Vars.player.unit().type.canBoost) return true;
		if(turret.targetGround && !Vars.player.unit().type.flying) return true;
		if(turret.targetAir && Vars.player.unit().type.flying)return true;
		return false;
	}

	private static float getPlayerHitSize2() {
		if(Vars.player.unit() == null) return 0;
		// hitSize / SQRT(2)
		return Vars.player.unit().hitSize*Vars.player.unit().hitSize/2f;
	}
}
