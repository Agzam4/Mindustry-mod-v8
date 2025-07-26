package agzam4.industry;

import static agzam4.ModWork.*;

import agzam4.AgzamMod;
import agzam4.Events;
import agzam4.ModWork;
import agzam4.debug.Debug;
import agzam4.debug.ObjectInspector;
import agzam4.render.MyDraw;
import agzam4.render.Text;
import agzam4.utils.Prefs;
import agzam4.ModWork.KeyBinds;
import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.scene.ui.layout.*;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.core.World;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.*;
import mindustry.gen.Building;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.Fonts;
import mindustry.world.*;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import mindustry.world.blocks.campaign.LandingPad;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.BaseTurret.BaseTurretBuild;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.heat.HeatProducer;
import mindustry.world.blocks.power.HeaterGenerator;
import mindustry.world.consumers.Consume;

public class IndustryCalculator {

	private static final Seq<Drill> drills = ModWork.getBlocks(Drill.class);
	private static final Seq<Pump> pumps = ModWork.getBlocks(Pump.class);
	private static final Seq<HeaterGenerator> heatGenerators = ModWork.getBlocks(HeaterGenerator.class);
	private static final Seq<HeatProducer> heatProducers = ModWork.getBlocks(HeatProducer.class);
	
	private static final Seq<Block>[] crafters = createCrafters();
	private static final Seq<Block>[] liquidCrafters = createLiquidCrafters();
	
	
	public static boolean[] hasLiquid = new boolean[Vars.content.liquids().size]; // that can be got using pumps
	
	static BalanceFragment balanceFragment;
	
	public static void init() {
		balanceFragment = new BalanceFragment();
		balanceFragment.build();

		Events.on(WorldLoadEndEvent.class, e -> {
			for (int i = 0; i < hasLiquid.length; i++) {
				hasLiquid[i] = false;
			}

			for (Tile t : Vars.world.tiles) {
				if(t.block().isAir() && t.floor().liquidDrop != null) {
					hasLiquid[t.floor().liquidDrop.id] = true;
				}
			}
		});

		Events.on(TileChangeEvent.class, e -> {
			if(e.tile.block().isAir() && e.tile.floor().liquidDrop != null) {
				hasLiquid[e.tile.floor().liquidDrop.id] = true;
			}
		});
	}
	

	public static BuildTooltip buildTooltip = new BuildTooltip();

	public static void draw() {
		drawSelect();
	}
	
	public static void drawUi() {
		
		buildTooltip.rebuild();
		
		final float mouseX = Core.input.mouseWorldX();
		final float mouseY = Core.input.mouseWorldY();
//		MyDraw.textColor(debug, mouseX, mouseY, 0, 0, 1f, 1, Align.center);
		
		Tile tile = Vars.world.tileWorld(mouseX, mouseY);
		if(tile == null) return;
		if(tile.build == null) return;
		Building building = tile.build;
//		if(building.team != Vars.player.team()) return;

		float multiplier = Vars.state.rules.blockHealthMultiplier*building.team.rules().blockHealthMultiplier;
		float health = building.health()*multiplier;
		float maxHealth = building.maxHealth()*multiplier;
		
		int index = ModWork.getGradientIndex(health, maxHealth);
		
		if(Prefs.settings.bool("show-units-health")) {
			Text.size(1f);
			Draw.color(rs[index], gs[index], bs[index]);
			Text.at(ModWork.roundSimple(health), building.x, building.y + (building.block.size+1)*Vars.tilesize/2f, Align.center);
		}
		

		if(Debug.debug && Core.input.keyTap(KeyCode.mouseMiddle) && Core.input.ctrl()) {
//			ObjectInspector.show(building);
			new ObjectInspector(building).show();
		}
		
		if(building.team == Vars.player.team() && Prefs.settings.bool("show-blocks-tooltip")) {
			Block block = building.block;
			if(building instanceof ConstructBuild) {
				ConstructBuild cb = (ConstructBuild) building;
				buildTooltip.line(cb.current, "[white]" + cb.current.localizedName.toUpperCase());
				ModWork.getRequired(cb, (item, amount) -> {
					buildTooltip.line(item, "[white]" + amount);
				});
			}
			
			ModWork.getCraftSpeed(building, (craftSpeed, craftSpeedMultiplier) -> {
				if(building instanceof ConstructBuild) {
					Draw.z(Layer.playerName);
					buildTooltip.draw(mouseX, mouseY);
					return;
				}
				
//					return;

//				StringBuilder info = new StringBuilder(block.emoji() + " " + block.localizedName.toUpperCase());
				buildTooltip.line(block, "[white]" + block.localizedName.toUpperCase());
				
				if(Debug.debug) {
					buildTooltip.line(block, "[royal]craftSpeed:[lightgray]" + craftSpeed + "/" + craftSpeedMultiplier);
					buildTooltip.line(block, "[royal]rid:[lightgray]" + AgzamMod.modRandom);

					if(block instanceof LandingPad landingPad) {
						buildTooltip.line(block, "[royal]cooldownTime:[lightgray]" + landingPad.cooldownTime);
						buildTooltip.line(block, "[royal]arrivalDuration:[lightgray]" + landingPad.arrivalDuration);
					}
				}
//				buildTooltip.color(Pal.accent);

				if(craftSpeed > 0) {
					if(block.consumers != null) {
						for (int i = 0; i < block.consumers.length; i++) {
							ModWork.consumeItems(block.consumers[i], building, craftSpeed, (item, ips) -> {
								addItemInfo(buildTooltip, block, item, ips, false);
							});
							ModWork.consumeLiquids(block.consumers[i], building, craftSpeedMultiplier, (liquid, lps) -> {
								addLiquidInfo(buildTooltip, block, liquid, lps, false);
							});
						}
					}
				}
				
				float heat = ModWork.consumeHeat(building, craftSpeed);
				if(heat > 0) {
					buildTooltip.line("[red]" + Iconc.waves + " [lightgray]" + ModWork.round(heat) + "/sec");
					addHeatCrafters(buildTooltip, block, heat);
				}
						
				Draw.z(Layer.playerName);
				buildTooltip.draw(mouseX, mouseY);
			});
		}
	}
	
	private static void drawSelect() {
		if(!Prefs.settings.bool("selection-calculations")) return;
        final float ts = Vars.tilesize/2f;
		if(selectStart.x != -1 && selectEnd.x != -1) {
			int minX = Math.min(selectStart.x, selectEnd.x);
			int maxX = Math.max(selectStart.x, selectEnd.x)+1;
			int minY = Math.min(selectStart.y, selectEnd.y);
			int maxY = Math.max(selectStart.y, selectEnd.y);

			Draw.z(Layer.plans);
	        Lines.stroke(2f);
	        Draw.color(selectBack);
	        Lines.rect(minX*Vars.tilesize - ts, minY*Vars.tilesize - 1 - ts, (maxX-minX)*Vars.tilesize, (maxY-minY+1)*Vars.tilesize);
	        Draw.color(select);
	        Lines.rect(minX*Vars.tilesize - ts, minY*Vars.tilesize - ts, (maxX-minX)*Vars.tilesize, (maxY-minY+1)*Vars.tilesize);

	        Lines.stroke(1);
	        for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x < maxX; x++) {
					Tile tile = Vars.world.tile(x, y);
					if(tile.build == null) continue;
					if(tile.build.team != Vars.player.team()) continue;
					boolean needDraw = true;
					final int size = tile.block().size;
					int zeroX = tile.build.tileX() - Mathf.floor((size-1)/2f);
					int zeroY = tile.build.tileY() - Mathf.floor((size-1)/2f);

					int tx = Math.min(Math.max(minX, zeroX), maxX);
					int ty = Math.min(Math.max(minY, zeroY), maxY);

					needDraw = x == tx && y == ty;
					
					if(!tile.block().isMultiblock()) needDraw = true;
					if(needDraw) {
						float dSize = tile.block().size*Vars.tilesize;
						float dx = zeroX*Vars.tilesize + dSize/2f - ts;//(tile.build.tileX() - size/2f)*Vars.tilesize;
						float dy = zeroY*Vars.tilesize + dSize/2f - ts;//(tile.build.tileY() - size/2f)*Vars.tilesize;
						Draw.z(Layer.blockAdditive);
				        Draw.color(select);
				        Lines.rect(dx-dSize/2f, dy-dSize/2f, dSize, dSize);
						Draw.z(Layer.blockAdditive);
				        Draw.color(selectHower);
				        Fill.rect(dx, dy, dSize, dSize);
					}
				}
			}
		}
	        
		for (int i = 0; i < selected.size; i++) {
			Tile tile = selected.get(i);
			Block block = tile.block();
			int zeroX = tile.centerX() - Mathf.floor((block.size-1)/2f);
			int zeroY = tile.centerY() - Mathf.floor((block.size-1)/2f);

			float dSize = tile.block().size*Vars.tilesize;
			float dx = zeroX*Vars.tilesize + dSize/2f - ts;
			float dy = zeroY*Vars.tilesize + dSize/2f - ts;
			Draw.z(Layer.blockAdditive);
			Draw.color(select);
			Lines.rect(dx-dSize/2f, dy-dSize/2f, dSize, dSize);
			Draw.z(Layer.blockAdditive);
			Draw.color(selectHower);
			Fill.rect(dx, dy, dSize, dSize);
		}
	}
	
	
	// a73e3e
	// D8D8D8 A3A3A3
	// 00DB00 3EA53E 00DB00
	private static final Color select = Color.valueOf("ffffff"), 
			selectBack = Color.valueOf("a3a3a3"), selectHower = Color.valueOf("ffffff").a(.5f);
	
	private static Seq<Tile> selected = new Seq<>();

	private static Point2 selectStart = new Point2(-1, -1);
	private static Point2 selectEnd = new Point2(-1, -1);
	
	static String debug = "none";
	
	public static void update() {
		if(!Prefs.settings.bool("selection-calculations")) return;
		int tileX = World.toTile(Core.input.mouseWorldX());
		int tileY = World.toTile(Core.input.mouseWorldY());
		if(tileX < 0) return;
		if(tileY < 0) return;
		if(tileX >= Vars.world.width()) return;
		if(tileY >= Vars.world.height()) return;
		
		if(ModWork.acceptKey()) {
			if(ModWork.keyDown(KeyBinds.clearSelection)) {
				clearSelection();
			}
		}
		
		if(ModWork.acceptKey() && (ModWork.hasKeyBoard() ? 
				ModWork.keyDown(KeyBinds.selection) 
				: (ModWork.keyDown(KeyBinds.selection) && Core.input.isTouched()))) {
			if(selectStart.x == -1 || selectStart.y == -1) {
				selectStart.x = tileX;
				selectStart.y = tileY;
			}
			selectEnd.x = tileX;
			selectEnd.y = tileY;
		} else {
			if(selectStart.x != -1) {
				int minX = Math.min(selectStart.x, selectEnd.x);
				int maxX = Math.max(selectStart.x, selectEnd.x)+1;
				int minY = Math.min(selectStart.y, selectEnd.y);
				int maxY = Math.max(selectStart.y, selectEnd.y);
				
				Building startBuilding = Vars.world.build(selectStart.x, selectStart.y);
				boolean add = true;
				if(startBuilding != null) {
					if(selected.contains(startBuilding.tileOn())) {
						add = false;
					}
				}
		        for (int y = minY; y <= maxY; y++) {
					for (int x = minX; x < maxX; x++) {
						Building build = Vars.world.build(x, y);
						if(build == null) continue;
						if(add) {
							if(!selected.contains(build.tileOn())) {
								selected.add(build.tileOn());
							}
						} else {
							selected.remove(build.tileOn());
						}
					}
		        }
				
			}
			selectStart.x = -1;
			selectStart.y = -1;
		}
		
		StringBuilder info = new StringBuilder();
		for (int i = 0; i < selected.size; i++) {
//			info.append("[white]" + selected.get(i).block().emoji() + " [gold]" + selected.get(i).block().localizedName.toUpperCase());
			info.append("[white] Line [gold]#" + i + "/" + selected.size);
			info.append('\n');
		}
		
		calcBalance();
//		balanceFragment.setText(info.toString());
	}
	
	public static void clearSelection() {
		if(selected.size > 0) {
			selected.clear();
			return;
		}
	}
	
	public static Seq<Tile> selected() {
		return selected;
	}

	public static float itemsBalance[] = new float[Vars.content.items().size];
	private static float liquidBalance[] = new float[Vars.content.liquids().size];

	private static float itemsBalanceTotal[] = new float[Vars.content.items().size];
	private static float liquidBalanceTotal[] = new float[Vars.content.liquids().size];
	private static boolean itemsWarn[] = new boolean[Vars.content.items().size];
	
	private static float itemsBalanceFixed[] = new float[Vars.content.items().size];
	private static float liquidBalanceFixed[] = new float[Vars.content.liquids().size];

	private static float airDps = 0;
	private static float groundDps = 0;
	private static float power = 0;
	private static float heat = 0;

	static Seq<Tile> selected_ = new Seq<>();
	
	static int updates = 0;
	private static void calcBalance() {
//		StringBuilder info = new StringBuilder();
		
		
		for (int i = 0; i < itemsBalance.length; i++) {
			itemsBalance[i] = 0;
			itemsWarn[i] = false;
		}
		for (int i = 0; i < liquidBalance.length; i++) {
			liquidBalance[i] = 0;
		}
		
		if(selected.size == 0) {
			for (int i = 0; i < itemsBalance.length; i++) {
				itemsBalanceFixed[i] = itemsBalanceTotal[i] = 0;
			}
			for (int i = 0; i < liquidBalance.length; i++) {
				liquidBalanceFixed[i] = liquidBalanceTotal[i] = 0;
			}
			updates = 0;
		}

		airDps = 0;
		groundDps = 0;
		power = 0;
		heat = 0;

		Cons<Float> heatProduce = hps -> heat += hps;
		Cons<Float> heatConsume = hps -> heat -= hps;
		
		Cons<Float> powerProduce = pps -> power += pps;
		Cons<Float> powerConsume = pps -> power -= pps;

		balanceFragment.element.rebuild();
		
		boolean buildPlans = false;
		if(Prefs.settings.bool("buildplans-calculations")) {
			if(Vars.player.unit() != null) {
				if(Vars.player.unit().plans != null) {
					if(Vars.player.unit().plans().size > 0) {
						for (int i = 0; i < Vars.player.unit().plans().size; i++) {
							BuildPlan buildPlan = Vars.player.unit().plans().get(i);
							if(buildPlan.breaking) continue;
							float craftSpeed = ModWork.getCraftSpeed(buildPlan.block, buildPlan.x, buildPlan.y, buildPlan.config);
							ModWork.consumeBlock(buildPlan.block, buildPlan.x, buildPlan.y, 
									buildPlan.config, craftSpeed, 
									(item, ips) -> itemsBalance[item.id] -= ips,
									(liquid, lps) -> liquidBalance[liquid.id] -= lps,
									powerConsume, heatConsume);
							ModWork.produceBlock(buildPlan.block, buildPlan.x, buildPlan.y, 
									buildPlan.config, craftSpeed, 
									(item, ips) -> itemsBalance[item.id] += ips,
									(liquid, lps) -> liquidBalance[liquid.id] += lps,
									powerProduce, heatProduce);
						}
						
						buildPlans = true;
					}
				}
			}
//			Log.info("1 heat: @", heat);
			if(Vars.control.input.selectPlans.size > 0) {
				for (int i = 0; i < Vars.control.input.selectPlans.size; i++) {
					BuildPlan buildPlan = Vars.control.input.selectPlans.get(i);
					if(buildPlan.breaking) continue;
					float craftSpeed = ModWork.getCraftSpeed(buildPlan.block,
							buildPlan.x, buildPlan.y, buildPlan.config);
					ModWork.consumeBlock(buildPlan.block, buildPlan.x, buildPlan.y, 
							buildPlan.config, craftSpeed, 
							(item, ips) -> itemsBalance[item.id] -= ips,
							(liquid, lps) -> liquidBalance[liquid.id] -= lps,
							powerConsume, heatConsume);
					ModWork.produceBlock(buildPlan.block, buildPlan.x, buildPlan.y, 
							buildPlan.config, craftSpeed, 
							(item, ips) -> itemsBalance[item.id] += ips,
							(liquid, lps) -> liquidBalance[liquid.id] += lps,
							powerProduce, heatProduce);
				}
				buildPlans = true;
			}
//			Log.info("2 heat: @", heat);
			
			if(Vars.control.input.linePlans.size > 0) {
				for (int i = 0; i < Vars.control.input.linePlans.size; i++) {
					BuildPlan buildPlan = Vars.control.input.linePlans.get(i);
					if(buildPlan.breaking) continue;
					float craftSpeed = ModWork.getCraftSpeed(buildPlan.block,
							buildPlan.x, buildPlan.y, buildPlan.config);
					ModWork.consumeBlock(buildPlan.block, buildPlan.x, buildPlan.y, 
							buildPlan.config, craftSpeed, 
							(item, ips) -> itemsBalance[item.id] -= ips,
							(liquid, lps) -> liquidBalance[liquid.id] -= lps,
							powerConsume, heatConsume);
					ModWork.produceBlock(buildPlan.block, buildPlan.x, buildPlan.y, 
							buildPlan.config, craftSpeed, 
							(item, ips) -> itemsBalance[item.id] += ips,
							(liquid, lps) -> liquidBalance[liquid.id] += lps,
							powerProduce, heatProduce);
				}
				buildPlans = true;
			}
			
//			Log.info("3 heat: @", heat);
		}
		
		
		if(selected.size > 0) {
			balanceFragment.element.line("[accent]Selected" + (buildPlans ? " & build plans" : ""));
		} else if(buildPlans) {
			balanceFragment.element.line("[accent]Build plans");
		}

		Seq<Tile> selected_ = new Seq<>();
		
		for (int s = 0; s < selected.size; s++) {
			Tile tile = selected.get(s);
			if(tile.build == null) continue;
			if(selected_.contains(tile.build.tile)) continue;
			selected_.add(tile);
		}
		
		selected = selected_;
		
		ObjectMap<Block, Integer> count = new ObjectMap<>();
		
		for (int s = 0; s < selected.size; s++) {
			Tile tile = selected.get(s);
			Building building = tile.build;
			Block block = tile.block();
			if(building == null) continue;
			
			count.put(block, count.get(block, 0)+1);
			
			if(building instanceof BaseTurretBuild && block instanceof BaseTurret) {
				BaseTurretBuild baseTurretBuild = (BaseTurretBuild) building;
				float dps = baseTurretBuild.estimateDps();
				BaseTurret baseTurret = (BaseTurret) block;
				if(baseTurret.coolant != null && block instanceof ReloadTurret && building.liquids != null) {
					Liquid liquid = building.liquids.current();
					if(building.liquids.get(liquid) > 0.01f) {
						ReloadTurret reloadTurret = (ReloadTurret) block;
						float reload = reloadTurret.reload;
						float maxUsed = baseTurret.coolant.amount;
						float multiplier = baseTurret.coolantMultiplier;
						
						// reload, coolant.amount, coolantMultiplier
	                    float reloadRate = 1f + maxUsed * multiplier * liquid.heatCapacity;
	                    float standardReload = reload;
	                    float result = standardReload / (reload / reloadRate);
						dps *= result;//efficiency(building);
					}
				}
				dps *= building.team().rules().blockDamageMultiplier*Vars.state.rules.blockDamageMultiplier;
				if(block instanceof Turret) {
					Turret turret = (Turret) block;
					if(turret.targetAir) airDps += dps;
					if(turret.targetGround) groundDps += dps;
				} else {
					airDps += dps;
					groundDps += dps;
				}
			}

			ModWork.getCraftSpeed(building, (craftSpeed, craftSpeedMultiplier) -> {
				ModWork.produceItems(building, craftSpeed, (item, ips) -> {
					itemsBalance[item.id] += ips;
					if(building.items != null) {
						if(building.items.get(item) >= building.getMaximumAccepted(item)) {
							itemsWarn[item.id] = true;
						}
					}
				});

				ModWork.produceLiquids(building, craftSpeed, (liquid, lps) -> {
					liquidBalance[liquid.id] += lps;
				});

				ModWork.producePower(building, craftSpeed, powerProduce);
				ModWork.produceHeat(building, craftSpeed, heatProduce);
				
				for (int c = 0; c < block.consumers.length; c++) {
					Consume consume = block.consumers[c];
					ModWork.consumeItems(consume, building, craftSpeed, (item, ips) -> {
						itemsBalance[item.id] -= ips;
					});
					ModWork.consumeLiquids(consume, building, craftSpeedMultiplier, (liquid, lps) -> {
						liquidBalance[liquid.id] -= lps;
					});
					ModWork.consumePower(consume, building, powerConsume);
				}
				heat -= ModWork.consumeHeat(building, craftSpeed);
			});
		}

		for (int i = 0; i < itemsBalance.length; i++) {
			itemsBalanceTotal[i] += itemsBalance[i];
		}
		for (int i = 0; i < liquidBalance.length; i++) {
			liquidBalanceTotal[i] += liquidBalance[i];
		}

		if(updates%60 == 0 && updates > 0) {
			for (int i = 0; i < itemsBalance.length; i++) {
				itemsBalanceFixed[i] = itemsBalanceTotal[i]/60f;
				itemsBalanceTotal[i] = 0;//itemsBalance[i];
			}
			for (int i = 0; i < liquidBalance.length; i++) {
				liquidBalanceFixed[i] = liquidBalanceTotal[i]/60f;
				liquidBalanceTotal[i] = 0;//liquidBalance[i];
			}
		} else {
		}

		if(power != 0) {
			balanceFragment.element.line(Icon.power.getRegion(), (power > 0 ? "[green]" : "[scarlet]") + ModWork.round(power) + "/sec");
			balanceFragment.element.color(Pal.engine);
		}
		if(heat != 0) {
			balanceFragment.element.line("[red]" + Iconc.waves + (heat > 0 ? " [green]" : " [scarlet]") + ModWork.round(heat) + "/sec");
			if(heat < 0) addHeatCrafters(balanceFragment.element, null, -heat);
		}
		if(airDps != 0 || groundDps != 0) {
			balanceFragment.element.line(Icon.modeAttack.getRegion(), "[sky]" + ModWork.round(airDps) + " air damage/sec");
			balanceFragment.element.line(Icon.modeAttack.getRegion(), "[olive]" + ModWork.round(groundDps) + " ground damage/sec");
		}

		for (int i = 0; i < itemsBalance.length; i++) {
			Item item = Vars.content.item(i);
			float ips = itemsBalanceFixed[i];
			if(updates < 60) ips = itemsBalance[i];
			if(ips == 0) {
				if(itemsWarn[i]) {
					balanceFragment.element.line(item, "[yellow]0/sec " + Iconc.warning);
//					info.append();
				}
				continue;
			}
			if(ips < 0) {
				addItemInfo(balanceFragment.element, null, item, ips, itemsWarn[i]);
			} else {
				balanceFragment.element.line(item, " [green]+" + ModWork.round(ips) + "/sec" + (itemsWarn[i] ? (" [yellow]" + Iconc.warning) : ""));
			}
		}
		
		for (int i = 0; i < liquidBalance.length; i++) {
			Liquid liquid = Vars.content.liquid(i);
			float lps = liquidBalanceFixed[i];
			if(updates < 60) lps = liquidBalance[i];
			if(lps == 0) continue;
			if(lps < 0) {
				addLiquidInfo(balanceFragment.element, null, liquid, lps, false);
			} else {
				balanceFragment.element.line(liquid, " [green]+" + ModWork.round(lps) + "/sec");
//				info.append("\n[white]" + liquid.emoji() + " [green]+" + ModWork.round(lps) + "/sec");
			}
		}

		count.each((block, c) -> {
			balanceFragment.element.line(block, "x" + c);
		});
//		for (int s = 0; s < selected.size; s++) {
//			Tile tile = selected.get(s);
//			Building building = tile.build;
//			Block block = tile.block();
//		}
		
		
		updates++;
		
//		balanceFragment.setText(info.toString());
	}
	
//	static int tileX(float cursorX){
//        Vec2 vec = Core.input.mouseWorld(cursorX, 0);
//        if(selectedBlock()){
//            vec.sub(block.offset, block.offset);
//        }
//        return World.toTile(vec.x);
//    }
//
//	static int tileY(float cursorY){
//        Vec2 vec = Core.input.mouseWorld(0, cursorY);
//        if(selectedBlock()){
//            vec.sub(block.offset, block.offset);
//        }
//        return World.toTile(vec.y);
//    }
    
	private static void addItemInfo(IndustryElement element, @Nullable Block block, Item item, float ips, boolean warn) {
		if(ips < 0) {
			element.line(item, " [scarlet]" + ModWork.round(ips) + "/sec" + (warn ? "[yellow]" + Iconc.warning : ""));
			ips = -ips;
		} else {
			element.line(item, " [lightgray]" + ModWork.round(ips) + "/sec" + (warn ? "[yellow]" + Iconc.warning : ""));
		}
		addDrills(element, block, item, ips);
		addCrafters(element, block, item, ips);
	}
	
	private static void addLiquidInfo(IndustryElement element, @Nullable Block block, Liquid liquid, float lps, boolean warn) {
		if(lps < 0) {
			element.line(liquid, " [scarlet]" + ModWork.round(lps) + "/sec " + (warn ? "[yellow]" + Iconc.warning : ""));
			lps = -lps;
		} else {
			element.line(liquid, " [lightgray]" + ModWork.round(lps) + "/sec" + (warn ? "[yellow]" + Iconc.warning : ""));
		}
//		if(warn) info.append(warn);
		addPumps(element, block, liquid, lps);
		addLiquidCrafters(element, block, liquid, lps);
	}

	private static Seq<Block>[] createCrafters() {
		@SuppressWarnings("unchecked")
		Seq<Block>[] crafters = new Seq[Vars.content.items().size];
		for (int i = 0; i < crafters.length; i++) {
			Seq<Block> crafter = new Seq<>();
			crafters[i] = crafter;
		}

		Vars.content.blocks().each(b -> {
			if(b instanceof GenericCrafter) {
				GenericCrafter crafter = (GenericCrafter) b;
				if(crafter.outputItem != null) {
					crafters[crafter.outputItem.item.id].add(b);
				}
				if(crafter.outputItems != null) {
					for (int i = 0; i < crafter.outputItems.length; i++) {
						if(!crafters[crafter.outputItems[i].item.id].contains(b))
						crafters[crafter.outputItems[i].item.id].add(b);
					}
				}
			}
			// TODO: Separator
		});
		
		return crafters;
	}
	
	private static Seq<Block>[] createLiquidCrafters() {
		@SuppressWarnings("unchecked")
		Seq<Block>[] crafters = new Seq[Vars.content.liquids().size];
		for (int i = 0; i < crafters.length; i++) {
			Seq<Block> crafter = new Seq<>();
			crafters[i] = crafter;
		}

		Vars.content.blocks().each(b -> {
			if(b instanceof GenericCrafter) {
				GenericCrafter crafter = (GenericCrafter) b;
				if(crafter.outputLiquid != null) {
					crafters[crafter.outputLiquid.liquid.id].add(b);
				}
				if(crafter.outputLiquids != null) {
					for (int i = 0; i < crafter.outputLiquids.length; i++) {
						if(!crafters[crafter.outputLiquids[i].liquid.id].contains(b))
						crafters[crafter.outputLiquids[i].liquid.id].add(b);
					}
				}
			}
		});
		return crafters;
	}
	
	/**
	 * Add pumps info
	 * @param builder - StringBuilder to append text
	 * @param item - type of liquid
	 * @param lps - required liquid per second
	 */
	private static void addPumps(IndustryElement element, @Nullable Block block, Liquid liquid, float lps) {
		// TODO: hid if world has not liquid
		for (int d = 0; d < pumps.size; d++) {
			Pump pump = pumps.get(d);
			if(!pump.environmentBuildable()) continue;
			if(!pump.isPlaceable()) continue;
			float pumpLps = pump.pumpAmount*60;
			if(pump instanceof SolidPump) {
				if(((SolidPump) pump).result != liquid) continue;
			} else {
				if(!hasLiquid[liquid.id]) continue;
				pumpLps *= pump.size*pump.size;
			}
			float count = lps/pumpLps;
			if(block != null) {
				element.line("[lightgray]> ", 
						pump, " [white]x" + ModWork.round(count) + "[lightgray] or ",
						block, "[white] x" + ModWork.round(1/count));
			} else {
				element.line("[lightgray]> ", pump, " [white]x" + ModWork.round(count));
			}
		}
	}
	
	/**
	 * Add drills info
	 * @param builder - StringBuilder to append text
	 * @param item - type of item
	 * @param ips - required items per second
	 */
	private static void addDrills(IndustryElement element, @Nullable Block block, Item item, float ips) {
		if(!Vars.indexer.hasOre(item)) return;
		for (int d = 0; d < drills.size; d++) {
			Drill drill = drills.get(d);
			if(!drill.environmentBuildable()) continue;
			if(!drill.isPlaceable()) continue;
			if(item.hardness > drill.tier) continue;
			boolean liquid = ModWork.needDrillWaterBoost(drill, item);
			float count = ips/ModWork.drillSpeed(drill, item, liquid);
			if(block != null) {
				element.line("[lightgray]> [white]",
						drill, (liquid ? " [sky]" : " [white]") + "x" + ModWork.round(count) + "[lightgray] or [white]",
						block, "[white] x" + ModWork.round(1/count)
				);
			} else {
				element.line("[lightgray]> [white]",
						drill, (liquid ? " [sky]" : " [white]") + "x" + ModWork.round(count)
				);
			}
		}
	}
	
	/**
	 * Add GenericCrafter info
	 * @param builder - StringBuilder to append text
	 * @param item - type of item
	 * @param ips - required items per second
	 */
	private static void addCrafters(IndustryElement element, @Nullable Block block, Item item, float ips) {
		for (int c = 0; c < crafters[item.id].size; c++) {
			Block crafter = crafters[item.id].get(c);
			if(!crafter.environmentBuildable()) continue;
			if(!crafter.isPlaceable()) continue;
			float cps = 0f; // crafts per second
			if(crafter instanceof GenericCrafter) {
				GenericCrafter gCrafter = (GenericCrafter) crafter;
				if(gCrafter.outputItem != null) {
					if(gCrafter.outputItem.item == item) cps = gCrafter.outputItem.amount;
				}
				if(gCrafter.outputItems != null) {
					for (int oi = 0; oi < rs.length; oi++) {
						if(gCrafter.outputItems[oi].item == item) {
							cps = gCrafter.outputItems[oi].amount;
							break;
						}
					}
				}
				cps *= 60f / gCrafter.craftTime;
			}
			
			float count = ips/cps;
			if(block != null)  {
				element.line("[lightgray]> [white]", 
						crafter, " [white]" + "x" + ModWork.round(count) + "[lightgray] or [white]",
						block, "[white] x" + ModWork.round(1/count)
						);
			} else {
				element.line("[lightgray]> [white]", crafter, " [white]" + "x" + ModWork.round(count));
			}
		}
	}

	/**
	 * Add GenericCrafter info
	 * @param builder - StringBuilder to append text
	 * @param liquid - type of liquid
	 * @param lps - required liquid per second
	 */
	private static void addLiquidCrafters(IndustryElement element, @Nullable Block block, Liquid liquid, float lps) {
		for (int c = 0; c < liquidCrafters[liquid.id].size; c++) {
			Block crafter = liquidCrafters[liquid.id].get(c);
			if(!crafter.environmentBuildable()) continue;
			if(!crafter.isPlaceable()) continue;
			float cps = 0f; // crafts per second
			if(crafter instanceof GenericCrafter) {
				GenericCrafter gCrafter = (GenericCrafter) crafter;
				if(gCrafter.outputItem != null) {
					if(gCrafter.outputLiquid.liquid == liquid) cps = gCrafter.outputItem.amount;
				}
				if(gCrafter.outputLiquids != null) {
					for (int ol = 0; ol < rs.length; ol++) {
						if(gCrafter.outputLiquids[ol].liquid == liquid) {
							cps = gCrafter.outputLiquids[ol].amount;
							break;
						}
					}
				}
				cps *= 60f;// / gCrafter.craftTime;
			}
			
			float count = lps/cps;
			if(block != null) {
				element.line("[lightgray]> ", crafter, " [white]" + "x" + ModWork.round(count) + "[lightgray] or [white]", block, "[white] x" + ModWork.round(1/count));
			} else {
				element.line("[lightgray]> ", crafter, " [white]" + "x" + ModWork.round(count));
			}
		}
	}

	/**
	 * 
	 * @param element
	 * @param block
	 * @param hps
	 */
	private static void addHeatCrafters(IndustryElement element, @Nullable Block block, float hps) {
		for (HeaterGenerator generator : heatGenerators) {
			if(!generator.environmentBuildable()) continue;
			if(!generator.isPlaceable()) continue;
			float count = hps/generator.heatOutput;
			if(block != null) {
				element.line("[lightgray]> ", generator, " [white]" + "x" + ModWork.round(count) + "[lightgray] or [white]",
						block, "[white] x" + ModWork.round(1f/count));
			} else {
				element.line("[lightgray]> ", generator, " [white]" + "x" + ModWork.round(count));
			}
		}
		for (HeatProducer producer : heatProducers) {
			if(!producer.environmentBuildable()) continue;
			if(!producer.isPlaceable()) continue;
			float count = hps/producer.heatOutput;
			if(block != null) {
				element.line("[lightgray]> ", producer, " [white]" + "x" + ModWork.round(count) + "[lightgray] or [white]", block, "[white] x" + ModWork.round(1f/count));
			} else {
				element.line("[lightgray]> ", producer, " [white]" + "x" + ModWork.round(count));
			}
		}
	}
	
	
	static class BalanceFragment extends Table {

		BuildTooltip element = new BuildTooltip();
	    
	    private void build() {
			Core.scene.add(this);
		}
	    
	    String text = "";
	    
//	    private void setText(String text) {
//	    	this.text = text;
//		}
	    
		@Override
		public void draw() {
			if(!Prefs.settings.bool("selection-calculations")) return;
			if(element.isEmpty()) return;
			if(Vars.state.isMenu()) return;
			if(Vars.ui.schematics.isShown()) return;
			if(Vars.ui.content.isShown()) return;
			if(Vars.ui.database.isShown()) return;
			if(!Vars.ui.hudfrag.shown) return;
			
			Font font = Fonts.outline;
			

			GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
			font.setUseIntegerPositions(false);
			font.getData().setScale(1f);
			font.getData().setLineHeight(MyDraw.textHeight*2f * Scl.scl(1f));
			layout.setText(font, text);
			
			float width = Math.max(element.width(), MyDraw.textHeight*20) + Fonts.outline.getLineHeight();
			float x = Core.scene.getWidth() - width;
			float y = Core.scene.getHeight();
			
			element.padX = Fonts.outline.getLineHeight();
			element.padY = Fonts.outline.getLineHeight();

			element.draw(x, y-element.height()-Fonts.outline.getLineHeight()*2, width);
		} 
		
		protected void rect(float x, float y, float w, float h){
	        Draw.rect("whiteui", x + w/2f, y + h/2f, w, h);
	    }
	}

}