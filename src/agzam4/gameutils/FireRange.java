package agzam4.gameutils;

import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.game.Teams.TeamData;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.defense.turrets.Turret;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;

import static arc.graphics.g2d.Draw.color;

import agzam4.ModWork;

public class FireRange {

	public static final Seq<BaseTurret> turrets = ModWork.getBlocks(BaseTurret.class);
	
	public static void draw() {
		if(!ModWork.setting("show-turrets-range")) return;
		Draw.z(Layer.effect);

		final float x = Vars.player.x;
		final float y = Vars.player.y;

		final float hitSize2 = getPlayerHitSize2();
		
		for (int team = 0; team < Vars.state.teams.present.size; team++) {
			TeamData data = Vars.state.teams.present.get(team);
			Lines.stroke(1f, data.team.color);
			if(data.team == Vars.player.team()) continue;
			for (int t = 0; t < turrets.size; t++) {
				if(!canBeAttacked(turrets.get(t))) continue;
				final float tRange = turrets.get(t).range;
				
				final float extraRange = Vars.tilesize*Vars.tilesize*25*25+hitSize2;
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
	}
	
	private static boolean canBeAttacked(BaseTurret baseTurret) {
		if(Vars.player.unit() == null) return false;
		if(Vars.player.unit().type.canBoost) return true;
		if(baseTurret instanceof Turret) {
			Turret t = (Turret) baseTurret;
			if(t.targetGround && !Vars.player.unit().type.flying) return true;
			if(t.targetAir && Vars.player.unit().type.flying) return true;
			return false;
		}
		return true;
	}

	private static float getPlayerHitSize2() {
		if(Vars.player.unit() == null) return 0;
		return Vars.player.unit().hitSize*Vars.player.unit().hitSize/2f;
	}
}
