package agzam4;

import arc.Events;
import arc.math.geom.Position;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType.TileChangeEvent;
import mindustry.game.EventType.TilePreChangeEvent;
import mindustry.game.EventType.WorldLoadEndEvent;
import mindustry.type.Item;
import mindustry.world.Tile;

public class MyIndexer {

	public static Seq<Tile>[] ores;
	
	@SuppressWarnings("unchecked")
	public static void init() {
		ores = new Seq[Vars.content.items().size];
		for (int i = 0; i < ores.length; i++) {
			ores[i] = new Seq<Tile>();
		}
        Events.on(WorldLoadEndEvent.class, e -> {
    		for (int i = 0; i < ores.length; i++) {
    			ores[i].clear();
    		}
        	for (int y = 0; y < Vars.world.height(); y++) {
        		for (int x = 0; x < Vars.world.width(); x++) {
        			Tile tile = Vars.world.tile(x, y);
        			if(tile == null) continue;
        			if(tile.drop() != null) {
        				ores[tile.drop().id].add(tile);
        			} else if(tile.block().itemDrop != null) {
        				ores[tile.block().itemDrop.id].add(tile);
        			}
        		}
        	}
        });

        Events.on(TilePreChangeEvent.class, e -> {
        	if(e.tile == null) return;
        	if(e.tile.drop() != null) {
            	ores[e.tile.drop().id].remove(e.tile);
        	} else if(e.tile.block().itemDrop != null) {
            	ores[e.tile.block().itemDrop.id].remove(e.tile);
        	}
        });

        Events.on(TileChangeEvent.class, e -> {
        	if(e.tile == null) return;
        	if(e.tile.drop() != null) {
            	ores[e.tile.drop().id].add(e.tile);
        	} else if(e.tile.block().itemDrop != null) {
            	ores[e.tile.block().itemDrop.id].add(e.tile);
        	}
        });
	}
	

	public static @Nullable Tile findClosestOre(Position pos, Item ore, boolean ground, boolean wall) {
		float minCost = Float.MAX_VALUE;
		int closest = -1;
		Seq<Tile> os = ores[ore.id];
		if(os.size == 0) return null;
		for (int i = 0; i < os.size; i++) {
			if(os.get(i).block() != Blocks.air && !wall) continue;
			if(!ground) {
				if(os.get(i).block().itemDrop == null && !os.get(i).overlay().wallOre) continue;
			}
			float dst = os.get(i).dst2(pos);
			if(dst < minCost) {
				minCost = dst;
				closest = i;
			}
		}
		if(closest == -1) return null;
		return os.get(closest);
	}


	public static boolean hasOre(Item ore, boolean ground, boolean wall) {
		Seq<Tile> os = ores[ore.id];
		if(os.size == 0) return false;
		for (int i = 0; i < os.size; i++) {
			if(os.get(i).block() != Blocks.air && !wall) continue;
			if(!ground) {
				if(os.get(i).block().itemDrop == null && !os.get(i).overlay().wallOre) continue;
			}
			return true;
		}
		return false;
	}
}
