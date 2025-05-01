package agzam4;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.core.World;
import mindustry.game.EventType.TapEvent;
import mindustry.game.EventType.Trigger;
import mindustry.game.EventType.WaveEvent;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.game.SpawnGroup;
import mindustry.gen.Iconc;
import mindustry.type.UnitType;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Tile;

public class WaveViewer {

	public static Seq<SpawnInfo> spawns = new Seq<>();
	static int wave = -1;
	
	static boolean updateSpawns = false;
	
	public static void init() {
		Events.on(TapEvent.class, e -> {
			final int pos = e.tile.pos();
			for (int ti = 0; ti < spawns.size; ti++) {
				if(e.tile == spawns.get(ti).tile) {
					BaseDialog waveDialog = new BaseDialog("Total Waves");
					waveDialog.title.setColor(Color.white);
					waveDialog.closeOnBack();
					waveDialog.cont.pane(op -> {
						Table p = new Table();
						p.defaults().left().pad(15);
						
						Table t = new Table();
						t.defaults().pad(10);
						
						SpawnInfo current = new SpawnInfo(e.tile);
						SpawnInfo all = new SpawnInfo(null);
						
						for (int w = -1; w < 100; w++) {
							int wave = Vars.state.wave + w;
							all.reset();
							current.reset();
							for (int g = 0; g < Vars.state.rules.spawns.size; g++) {
								SpawnGroup group = Vars.state.rules.spawns.get(g);
								if(group.spawn == -1 || group.spawn == pos) {
									current.addInfo(group, wave);
								}
								all.addInfo(group, wave);
							}

					        t.add("Wave "  + (wave+1)).color(Pal.accent).colspan(4).pad(10).padBottom(4).growX().row();
							t.image().color(Pal.accent).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).growX().row();		
//							t.labelWrap("Wave " + wave).growX().pad(10).padBottom(4).fillX().row();		
							if(spawnsCount == 1) {
								t.add(current.toString()).growX().pad(10).padBottom(4).fillX().wrapLabel(false).row();		
							} else {
								t.add("[tan]Selected:[]\n" + current.toString()).growX().pad(10).padBottom(4).fillX().wrapLabel(false).row();		
								t.add("[tan]All:[]\n" + all.toString()).growX().pad(10).padBottom(4).fillX().wrapLabel(false).row();
							}
							t.row();
						}
						p.add(t).row();
						op.add(p).row();
					});
					waveDialog.show();
				}
			}
		});
//		Vars.spawner;

		Events.run(Trigger.update, () -> { // Changed by rules
			if(!ModWork.setting("wave-viewer")) return;
			Vars.spawner.isSpawning(); //asdasdiasfd
			if(spawnsCount != Vars.spawner.getSpawns().size || (updateSpawns && updates%60 == 0)) {
				updateSpawns = false;
				createSpawns();
				return;
			}
			updates++;
			if(Vars.state.wave-1 != wave) update(Vars.state.wave-1);
		});
		
		Events.on(WaveEvent.class, e -> {
			createSpawns();
		});
	}
	
	private static int spawnsCount = -1;
	static int updates = 0;
	
	private static void createSpawns() {
		spawns.clear();

		spawnsCount = Vars.spawner.getSpawns().size;
		for (int i = 0; i < Vars.spawner.getSpawns().size; i++) {
			spawns.add(new SpawnInfo(Vars.spawner.getSpawns().get(i)));
		}		
		update(Vars.state.wave-1);
	}

	private static void update(int wave) {
		WaveViewer.wave = wave;
		for (int i = 0; i < spawns.size; i++) {
			spawns.get(i).reset();
		}		
		for (int g = 0; g < Vars.state.rules.spawns.size; g++) {
			SpawnGroup group = Vars.state.rules.spawns.get(g);
			for (int i = 0; i < Vars.spawner.getSpawns().size; i++) {
				spawns.get(i).tile = Vars.spawner.getSpawns().get(i);
			}		
			spawnByPos(spawns, group.spawn, spawn -> spawn.addInfo(group, wave));
		}
	}
	
	private static void spawnByPos(Seq<SpawnInfo> spawns, int pos, Cons<SpawnInfo> cons) {
		for (int i = 0; i < spawns.size; i++) {
			if(spawns.get(i).tile.overlay() != Blocks.spawn) updateSpawns = true;
			if(pos == -1 || pos == spawns.get(i).tile.pos()) {
				cons.get(spawns.get(i));
			}
		}
	}
	
	static float size = 1f;

	public static void draw() {
		if(spawns == null) return;
		if(!ModWork.setting("wave-viewer")) return;
		
		for (int s = 0; s < spawns.size; s++) {
			SpawnInfo info = spawns.get(s);
			
			MyDraw.text(info.toString() + "\n[gray]Tap for more waves", info.tile.worldx(), info.tile.worldy() + MyDraw.textHeight*1, true);

			Draw.z(Layer.effect);
			Draw.color(Vars.state.rules.waveTeam.color);
			Lines.stroke(1.5f);

			float mouseX = Core.input.mouseWorldX();
			float mouseY = Core.input.mouseWorldY();
			
			Tile tile = Vars.world.tile(World.toTile(mouseX), World.toTile(mouseY));
			
			if(tile != null) {
				if(tile.pos() == info.tile.pos()) {
					size = (size-1)*0.9f+1;
				} else {
					size = (size-.7f)*0.9f+.7f;
				}
				
				MyDraw.rotatingArcs(info.tile, Vars.tilesize * size, 1f);
			}
		}
	}

	private static class SpawnInfo {

		Tile tile;

		int unitsCount[] = new int[Vars.content.units().size];
		int unitsShield = 0;
		int unitsHealth = 0;
		
		public SpawnInfo(Tile tile) {
			this.tile = tile;
		}
		
		public void reset() {
			unitsShield = 0;
			unitsHealth = 0;
			for (int i = 0; i < unitsCount.length; i++) {
				unitsCount[i] = 0;
			}
		}

		public void addInfo(SpawnGroup group, int wave) {
			int spawned = group.getSpawned(wave);
			addUnits(group.type, spawned);
			unitsShield += group.getShield(wave);
			float health = group.type.health * spawned;
			if(group.effect != null) {
				health *= group.effect.healthMultiplier;
			}
			unitsShield += group.getShield(wave) * spawned;
			unitsHealth += health;
		}

		private void addUnits(UnitType type, int amount) {
			unitsCount[type.id] += amount;
		}
		
		@Override
		public String toString() {
			StringBuilder info = new StringBuilder();
			for (int i = 0; i < unitsCount.length; i++) {
				if(unitsCount[i] == 0) continue;
				
				if(info.length() != 0) info.append(' ');
				info.append((Vars.content.unit(i).hasEmoji() ? Vars.content.unit(i).emoji() : Vars.content.unit(i).name) + " " + unitsCount[i]);
			}
			info.append("\n[green]" + ModWork.round(unitsHealth) + "[lightgray] + " + Iconc.statusShielded + " [gold]" + ModWork.round(unitsShield));
			return info.toString();
		}
	}
}
