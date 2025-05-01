package agzam4;

import static mindustry.Vars.player;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.graphics.Layer;

public class CursorTracker {

	public static TextureRegion pointer, target, drill;
	
	public static Seq<PlayerCursor> cursors;
	
	public static void init() {
		cursors = new Seq<>();
		pointer = AgzamMod.sprite("cursor-pointer");
		target = AgzamMod.sprite("cursor-target");
		drill = AgzamMod.sprite("cursor-drill");
	}
	
	private static int updates = 0;
	
	public static void draw() {
		if(!ModWork.setting("cursors-tracking")) return;
		for (int i = 0; i < Groups.player.size(); i++) {
			Player p = Groups.player.index(i);
			if(p == player) continue;
			
			boolean found = false;
			
			for (int j = 0; j < cursors.size; j++) {
				PlayerCursor cursor = cursors.get(j);
				if(cursor.id == p.id) {
					cursor.draw(p);
					found = true;
					break;
				}
			}
			
			if(!found) {
				cursors.add(new PlayerCursor(p.id));
			}
		}
		
		for (int i = 0; i < cursors.size; i++) {
			if(cursors.get(i).lastUpdate != updates) {
				cursors.remove(i);
				break;
			}
		}
		
		updates++;
	}


	private static class PlayerCursor {

		int id;
		int lastUpdate = 0;

		float x, y;
		
		public PlayerCursor(int id) {
			lastUpdate = updates;
			this.id = id;
		}
		
		private void draw(Player p) {
			lastUpdate = updates;
			float nx = p.mouseX;
			float ny = p.mouseY;
			

			TextureRegion cursor = pointer;
			if(p.shooting) {
				cursor = target;
			} else {
				if(p.unit() != null) {
					if(p.unit().mining()) {
						if(p.unit().mineTile() != null) {
							cursor = drill;
							nx = p.unit().mineTile().drawx();
							ny = p.unit().mineTile().drawy();
						}
					}
				}
			}
			
			if(nx == 0 && ny == 0) {
				nx = x;
				ny = y;
			}
			
			float dx = (nx - x)/10f;
			float dy = (ny - y)/10f;
			
			float len = Mathf.len(dx, dy);

			if(len < .01f) {
				x = nx;
				y = ny;
			} else {
				x += dx;
				y += dy;
			}

			Color color = p.team() == null ? Color.white : p.team().color;

			MyDraw.normal(cursor, color, x, y, Layer.playerName);
			MyDraw.text(p.coloredName(), x, y, .5f, true);
		}
	}
}
