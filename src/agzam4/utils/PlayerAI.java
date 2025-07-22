package agzam4.utils;

import static arc.Core.input;
import static arc.Core.scene;
import agzam4.MyIndexer;
import agzam4.ModWork;
import agzam4.ModWork.KeyBinds;
import agzam4.gameutils.Afk;
import arc.Core;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.input.Binding;
import mindustry.input.DesktopInput;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

public class PlayerAI {
	
	public static boolean enabled = false;

	static int mineCoolDown = 0;
	
	
	static int searchCoolDown = 0;
	
	static Vec2 camera = new Vec2();
	static boolean requireUpdateCamera = false;
	
	
	public static void preDraw() {
		if(Vars.ui.minimapfrag.shown() || playerFragVisible()) {
			requireUpdateCamera = true;
			return;
		}
		if(enabled() && camera != null && !Vars.mobile) {
			Core.camera.position.set(camera);
		}
	}

	private static boolean playerFragVisible() {
		if(Vars.ui.listfrag.content == null) return false;
		if(Vars.ui.listfrag.content.parent == null) return false;
		if(Vars.ui.listfrag.content.parent.parent == null) return false;
		if(Vars.ui.listfrag.content.parent.parent.parent == null) return false;
		if(!Vars.ui.listfrag.content.parent.parent.parent.visible) return false;
		for (Player p : Groups.player) {
			if(p == Vars.player) continue;
			if(p.x != p.x) continue;
			if(p.y != p.y) continue;
			return panning();
		}
		return panning();
	}
	
	private static boolean panning() {
        if(Vars.control.input instanceof DesktopInput){
        	return ((DesktopInput)Vars.control.input).panning;
        }
        return false;
	}
	
	public static float panSlowSpeed = 4.5f;
	public static float panSpeed = 7f;
	public static float panBoostSpeed = 30f;
	public static float panScale = 0.005f;
	
	
	public static boolean enabled() {
		return enabled || (Afk.autoAI && Afk.isAfk());
	}
	
	public static void updatePlayer() {
		if(!enabled()) {
			requireUpdateCamera = true;
			return;
		}
		if(Vars.player == null) return;
		Unit unit = Vars.player.unit();
		if(unit == null) return;
//		if(unit.isBuilding()) return;

		if(requireUpdateCamera) {
			camera.set(Core.camera.position);
			requireUpdateCamera = false;
		}

        if(ModWork.acceptKey()) camera.add(Tmp.v1.setZero().add(Core.input.axis(Binding.moveX), Core.input.axis(Binding.moveY)).nor()
				.scl(panSpeed() * Time.delta));
		
        if(input.keyDown(Binding.pan) && !scene.hasField() && !scene.hasDialog() && ModWork.acceptKey()){
            camera.x += Mathf.clamp((Core.input.mouseX() - Core.graphics.getWidth() / 2f) * panScale, -1, 1) * panSpeed();
            camera.y += Mathf.clamp((Core.input.mouseY() - Core.graphics.getHeight() / 2f) * panScale, -1, 1) * panSpeed();
        }
        
//        DesktopInput
//        MobileInput
		
		if(unit.canBuild()) {
			BuildPlan plan = unit.buildPlan();
			if(plan != null) {
				boolean canBuild = ModWork.canBuild(unit, plan);
				if(!canBuild) {
					for (int i = 0; i < unit.plans.size; i++) {
						if(!ModWork.canBuild(unit, unit.plans.get(i))) continue;
						BuildPlan n = unit.plans.get(i);
						unit.plans.add(unit.plans.removeFirst());
						if(unit.plans.remove(n)) {
							unit.plans.addFirst(n);
							canBuild = true;
						}
						break;
					}
				}
				if(plan != null && canBuild) {
					if(unit.within(plan, Vars.tilesize)) return;
					circle(plan, 1, unit.speed());
					return;
				}
				unit.updateBuilding(canBuild);
			}
		}
		
		if(!unit.canMine()) return;
		
		
		if(unit.hasItem()) {
			if(unit.stack.amount < unit.itemCapacity()) {
				if(unit.mining()) return;
				if(MyIndexer.hasOre(unit.stack.item, unit.type.mineFloor, unit.type.mineWalls)) {
					updateSearchAndMine(unit.stack.item);
					return;
				}
				updateDropToCore();
				return;
			}
			if(unit.stack.amount == unit.itemCapacity()) {
				updateDropToCore();
				return;
			}
		} else {
			if(unit.mining() && mineCoolDown > 0) {
				if(unit.mineTile != null) {
					if(unit.mineTile.drop() != null) {
						mineCoolDown--;
						return;
					}
				}
			}
			CoreBuild core = unit.closestCore();
			if(core == null) return;
			Item target = null;
			int minCount = core.storageCapacity;
			for (int i = 0; i < Vars.content.items().size; i++) {
				Item item = Vars.content.item(i);
				if(!unit.canMine(item)) continue;
				if(!MyIndexer.hasOre(item, unit.type.mineFloor, unit.type.mineWalls)) continue;
				
				if(core.items.get(item) < minCount) {
					minCount = core.items.get(item);
					target = item;
				}
			}
			
			if(target != null) {
				updateSearchAndMine(target);
			}
		}
	}

	private static float panSpeed() {
		if(Core.input.keyDown(KeyBinds.slowMovement.key)) return panSlowSpeed;
		if(Core.input.keyDown(Binding.boost)) return panBoostSpeed;
		return panSpeed;
	}

	private static void updateDropToCore() {
		Unit unit = Vars.player.unit();
		CoreBuild core = unit.closestCore();
		if(core == null) return;
		if(unit.within(core, 2)) {
			Call.transferInventory(Vars.player, core);
			return;
		}
		circle(core, 1, unit.speed());
	}

	private static void updateSearchAndMine(Item item) {
		if(Vars.player == null) return;
		Unit unit = Vars.player.unit();
		
		Tile ore = MyIndexer.findClosestOre(unit, item, unit.type.mineFloor, unit.type.mineWalls);
		if(ore == null) return;
		
		if(unit.within(ore, 2)) {
			unit.mineTile(ore);
			mineCoolDown = 60*30;
			return;
		}
		
		circle(ore, 1, unit.speed());
	}

    public static void circle(Position target, float circleLength, float speed) {
		if(Vars.player == null) return;
		Unit unit = Vars.player.unit();
        if(target == null) return;

        unit.vel.set(target).sub(unit);

        if(unit.vel.len() < circleLength){
        	unit.vel.rotate((circleLength - unit.vel.len()) / circleLength * 180f);
        }

        unit.vel.setLength(speed);

        unit.moveAt(unit.vel);
    }


}
