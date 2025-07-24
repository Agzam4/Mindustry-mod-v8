package agzam4;

import java.lang.reflect.Field;

import agzam4.utils.Bungle;
import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Pixmaps;
import arc.input.KeyBind;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Planets;
import mindustry.core.UI;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.WorldLoadEndEvent;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.HeatCrafter;
import mindustry.world.blocks.production.HeatCrafter.HeatCrafterBuild;
import mindustry.world.blocks.production.Pump;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.blocks.units.UnitFactory.UnitFactoryBuild;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.consumers.ConsumeItemFilter;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.consumers.ConsumeLiquids;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import mindustry.world.blocks.campaign.LandingPad;
import mindustry.world.blocks.campaign.LaunchPad;
import mindustry.world.blocks.defense.ForceProjector;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.ItemTurret.ItemTurretBuild;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.blocks.heat.HeatConductor.HeatConductorBuild;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.blocks.heat.HeatProducer;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.blocks.power.PowerGenerator.GeneratorBuild;
import mindustry.world.blocks.production.AttributeCrafter;
import mindustry.world.blocks.production.AttributeCrafter.AttributeCrafterBuild;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.production.Drill.DrillBuild;
import mindustry.world.blocks.production.Pump.PumpBuild;
import mindustry.world.blocks.storage.StorageBlock.StorageBuild;
import mindustry.world.blocks.production.Separator;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.blocks.production.SolidPump.SolidPumpBuild;

public class ModWork {

	public static final int gradient = 30;
	public static final float rs[] = new float[gradient];
	public static final float gs[] = new float[gradient];
	public static final float bs[] = new float[gradient];
	
	static {
		for (int i = 0; i < gradient; i++) {
			// 6 - 136
			Color c = Color.HSVtoRGB(6 + i*130/gradient, 90, 100);
			rs[i] = c.r;
			gs[i] = c.g;
			bs[i] = c.b;
		}
	}
	
	public static int[][] lastItems = null;
	public static void init() {
		Events.on(WorldLoadEndEvent.class, e -> {
			lastItems = new int[Vars.world.width()][Vars.world.height()];
		});
	}
	
	public enum KeyBinds {
		openUtils("open-utils", KeyCode.u),
		slowMovement("slow-movement", KeyCode.altLeft),
		hideUnits("hide-units", KeyCode.h),
		selection("selection", KeyCode.g),
		clearSelection("clear-selection", KeyCode.q);

		public KeyCode def;
		public KeyCode key;
		public String keybind;
		
		private final KeyBind keyBind;
		
		KeyBinds(String keybind, KeyCode def) {
			this.def = def;
			this.keybind = keybind;
			int bind = Core.settings.getInt("agzam4mod.settings.keybinds." + keybind, def.ordinal());
			if(bind < 0 || bind >= KeyCode.all.length) {
				key = def;
			} else {
				key = KeyCode.all[bind];
			}
			
			keyBind = KeyBind.add(keybind, def, AgzamMod.mod.name);
		}

		void put() {
			Core.settings.put("agzam4mod.settings.keybinds." + keybind, key.ordinal());
		}

		public boolean isDown = false;

		boolean isDown() {
			return isDown;
		}

		public static void dispose() {
			for (var v : values()) {
				KeyBind.all.remove(v.keyBind);
			}
		}
		
	}

	public static boolean keyDown(KeyBinds key) {
		if(key.isDown()) return true;
		return Core.input.keyDown(key.key);
	}

	public static boolean keyJustDown(KeyBinds key) {
		return Core.input.keyTap(key.key);
	}
	
	public static int getGradientIndex(float health, float maxHealth) {
		int index = (int) (health*gradient/maxHealth);
		if(index < 0) return 0;
		if(index >= gradient) return gradient-1;
		return index;
	}

	public static String roundSimple(final float d) {
		if(Mathf.round(d)*100 == Mathf.round(d*100)) return "" + ((int)d);
		return "" + Mathf.round(d*100)/100f;
	}
	
	public static String round(final float d) {
		if(d >= 1000 || d <= -1000) return UI.formatAmount((long) d);
		if(Mathf.round(d)*100 == Mathf.round(d*100)) return "" + ((int)d);
		return "" + Mathf.round(d*100)/100f;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Seq<T> getBlocks(Class<T> type) {
		Seq<T> seq = new Seq<T>();
		Vars.content.blocks().each(b -> {
			if(type.isInstance(b)) seq.add((T) b);
		});
		return seq;
	}

	public static void getCraftSpeed(Building building, Cons2<Float, Float> cons) {
		Block block = building.block; // TODO: cache block's craft speed
		float craftSpeed = 1f;
		float craftSpeedMultiplier = 1f;
		if(block instanceof GenericCrafter crafter) craftSpeed = 60f / crafter.craftTime;
		if(block instanceof HeatCrafter crafter && building.efficiencyScale() == 0) {
			craftSpeedMultiplier = crafter.maxEfficiency;
			cons.get(craftSpeed*craftSpeedMultiplier, craftSpeedMultiplier);
			return;
		}
		if(block instanceof LandingPad landingPad) {
			craftSpeedMultiplier *= 1f / (landingPad.cooldownTime + landingPad.arrivalDuration);
		}
		if(building instanceof AttributeCrafterBuild crafterBuild) craftSpeedMultiplier = crafterBuild.efficiencyMultiplier();
		if(building instanceof UnitFactoryBuild factoryBuild && block instanceof UnitFactory factory) {
			int plan = factoryBuild.currentPlan;
			if(plan == -1) return;
			craftSpeed = 60f/factory.plans.get(plan).time;
		}
		if(block instanceof Reconstructor reconstructor) craftSpeed = 60f/reconstructor.constructTime;
		if(block instanceof Separator separator) craftSpeed = separator.craftTime/60f;
		if(block instanceof ConsumeGenerator generator) {
			craftSpeed = 60f/generator.itemDuration;
		} else {
			Field[] fields = block.getClass().getFields();
			for (int i = 0; i < fields.length; i++) {
				if(fields[i].getName().equals("itemDuration")) {
					try {
						craftSpeed = 60/fields[i].getFloat(block);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				if(fields[i].getName().equals("useTime")) {
					try {
						craftSpeed = 60/fields[i].getFloat(block);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(block instanceof ForceProjector projector) craftSpeed = 60f/projector.phaseUseTime;
		craftSpeedMultiplier *= building.efficiencyScale();
		cons.get(craftSpeed*craftSpeedMultiplier, craftSpeedMultiplier);
	}


	public static float getCraftSpeed(Block block, int x, int y, Object config) {
		if(block.consumers.length == 0) return 0;
		boolean hasConsumer = false;
		for (int i = 0; i < block.consumers.length; i++) {
			if(block.consumers[i] instanceof ConsumeItems
					|| block.consumers[i] instanceof ConsumeLiquid
					|| block.consumers[i] instanceof ConsumeItemDynamic
					|| block.consumers[i] instanceof ConsumeItemFilter) {
				hasConsumer = true;
				break;
			}
		}
		if(!hasConsumer) return 0;
		float craftSpeed = 1f;
		if(block instanceof GenericCrafter crafter) craftSpeed = 60f / crafter.craftTime;
		if(block instanceof AttributeCrafter attribute) {
			craftSpeed *= attribute.baseEfficiency + Math.min(attribute.maxBoost, attribute.boostScale * block.sumAttribute(attribute.attribute, x, y));
		}
		if(block instanceof UnitFactory factory && block instanceof UnitFactory && config instanceof Integer) {
			int plan = (Integer)config;
			if(plan == -1) return 0;
			craftSpeed = 60f/factory.plans.get(plan).time;
		}
		if(block instanceof Reconstructor reconstructor) craftSpeed = 60f/reconstructor.constructTime;
		if(block instanceof Separator separator) craftSpeed = separator.craftTime/60f;
		if(block instanceof ForceProjector projector) craftSpeed = 60f/projector.phaseUseTime;
		if(block instanceof ConsumeGenerator generator) {
			craftSpeed = 60f/generator.itemDuration;
		} else {
			Field[] fields = block.getClass().getFields();
			for (int i = 0; i < fields.length; i++) {
				if(fields[i].getName().equals("itemDuration")) {
					try {
						craftSpeed = 60/fields[i].getFloat(block);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				if(fields[i].getName().equals("useTime")) {
					try {
						craftSpeed = 60/fields[i].getFloat(block);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return craftSpeed;
	}

	public static boolean isBuildingZero(Building build, int x, int y) {
		final int size = build.block.size;
    	final int from = (size/2)+1-size;
    	final int tx = build.tileX();
    	final int ty = build.tileY();
    	if(tx + from == x && ty + from == y) return true;
		return false;
	}

	@Deprecated
	public static String bungle(String string) {
		return Bungle.get(string);//Core.bundle.get("agzam4mod." + string, "[red]??" + string + "??[]");
	}

	public static String settingName(String string) {
		return "agzam4mod.settings." + string;
	}
	
	@Deprecated
	public static boolean setting(String string) {
		return Core.settings.getBool("agzam4mod.settings." + string, true);
	}

	
	public static boolean settingDef(String string, boolean def) {
		try {
			return Core.settings.getBool("agzam4mod.settings." + string, def);
		} catch (Exception e) {
			return def;
		}
	}

	public static void setting(String string, boolean value) {
		Core.settings.put("agzam4mod.settings." + string, value);
	}

	public static void setting(String string, int value) {
		Core.settings.put("agzam4mod.settings." + string, value);
	}
	
	public static void setting(String string, float value) {
		Core.settings.put("agzam4mod.settings." + string, value);
	}
	
	public static float settingFloat(String string, float def) {
		return Core.settings.getFloat("agzam4mod.settings." + string, def);
	}

	public static int settingInt(String string, int def) {
		try {
			return Core.settings.getInt("agzam4mod.settings." + string, def);
		} catch (Exception e) {
			return def;
		}
	}

	public static void setting(String string, String value) {
		Core.settings.put("agzam4mod.settings." + string, value);
	}
	public static String settingDef(String string, String def) {
		return Core.settings.getString("agzam4mod.settings." + string, def);
	}
	

	public static String strip(String name) {
		return Strings.stripGlyphs(Strings.stripColors(name));
	}

	public static final String enToRu[][] = {
			{"OO", "\u0423"},
			{"A", "\u0410"},
			{"B", "\u0411"},
			{"C", "\u0421"},
			{"D", "\u0414"},
			{"E", "\u0415"},
			{"F", "\u0424"},
			{"G", "\u0413"},
			{"H", "\u0425"},
			{"I", "\u0418"},
			{"J", "\u0414\u0416"},
			{"K", "\u041a"},
			{"L", "\u041b"},
			{"M", "\u041c"},
			{"N", "\u041d"},
			{"O", "\u041e"},
			{"P", "\u041f"},
			{"Q", "\u041a"},
			{"R", "\u0420"},
			{"S", "\u0421"},
			{"T", "\u0422"},
			{"U", "\u0423"},
			{"V", "\u0412"},
			{"W", "\u0412"},
			{"X", "\u0425"},
			{"Y", "\u0418"},
			{"Z", "\u0417"},
	};
	
	public static String toRus(String stripName) {
		stripName = stripName.toUpperCase();
		for (int i = 0; i < enToRu.length; i++) {
			stripName = stripName.replaceAll(enToRu[i][0], enToRu[i][1]);
		}
		return stripName.toLowerCase();
	}


	public static void consumeItems(Consume consume, Building building, float craftSpeed, Cons2<Item, Float> cons) {
		if(consume instanceof ConsumeItems) {
			ConsumeItems items = (ConsumeItems) consume;
			ItemStack[] stacks = items.items;
			for (int item = 0; item < stacks.length; item++) {
				ItemStack stack = stacks[item];
				float ips = craftSpeed*stack.amount*building.timeScale();
				cons.get(stack.item, ips);
			}
			return;
		}
		if(consume instanceof ConsumeItemDynamic) {
			ConsumeItemDynamic dynamic = (ConsumeItemDynamic) consume;
			ItemStack[] stacks = dynamic.items.get(building);
			if(stacks == null) return;
			for (int item = 0; item < stacks.length; item++) {
				ItemStack stack = stacks[item];
				float ips = craftSpeed*stack.amount*building.timeScale();
				cons.get(stack.item, ips);
			}
			return;
		}
		if(consume instanceof ConsumeItemFilter) {
			ConsumeItemFilter filter = (ConsumeItemFilter) consume;
			Item consumed = filter.getConsumed(building);
			if(consumed == null) {
				if(lastItems == null) return;
				int id = lastItems[building.tileX()][building.tileY()];
				if(id < 1) return;
				consumed = Vars.content.item(id-1);
				if(consumed == null) return;
			}
			float ips = craftSpeed*building.timeScale();
			cons.get(consumed, ips);
			if(lastItems != null) {
				lastItems[building.tileX()][building.tileY()] = 1+consumed.id;
			}
			return;
		}
	}

	public static void produceItems(Building building, float craftSpeed, Cons2<Item, Float> cons) {
		if(building instanceof DrillBuild drill) cons.get(drill.dominantItem, drill.lastDrillSpeed*60*drill.timeScale());
		if(building.block instanceof GenericCrafter crafter) {
			if(crafter.outputItems != null) {
				for (int i = 0; i < crafter.outputItems.length; i++) {
					ItemStack output = crafter.outputItems[i];
					cons.get(output.item, craftSpeed*output.amount*building.timeScale());
				}
			}
		}
		if(building.block instanceof Separator separator) {
			if(separator.results != null) {
				for (int i = 0; i < separator.results.length; i++) {
					ItemStack output = separator.results[i];
					cons.get(output.item, craftSpeed*output.amount*building.timeScale()/separator.results.length);
				}
			}
		}
	}
	
	public static void consumeLiquids(Consume consume, Building building, float craftSpeedMultiplier, Cons2<Liquid, Float> cons) {
		if(consume instanceof ConsumeLiquid liquid) {
			float lps = liquid.amount*building.timeScale()*craftSpeedMultiplier*60f;
			cons.get(liquid.liquid, lps);
			return;
		}
		if(consume instanceof ConsumeLiquids liquids) {
			LiquidStack[] stacks = liquids.liquids;
			if(stacks == null) return;
			for (int liquid = 0; liquid < stacks.length; liquid++) {
				LiquidStack stack = stacks[liquid];
				float lps = stack.amount*building.timeScale()*craftSpeedMultiplier*60f;
				cons.get(stack.liquid, lps);
			}
			return;
		}
	}

	public static void consumePower(Consume consume, Building building, Cons<Float> cons) {
		if(consume instanceof ConsumePower power) cons.get(power.usage * 60f * building.timeScale());
	}

	public static float consumeHeat(Building building, float craftSpeed) {
		if(building instanceof HeatConductorBuild) return 0;
		if(building instanceof HeatCrafterBuild crafterBuild && building.block instanceof HeatCrafter crafter) {
			return crafterBuild.heatRequirement() * crafter.maxEfficiency;
		}
		if(building instanceof HeatConsumer consumer) return consumer.heatRequirement();
		return 0;
	}

	public static void produceLiquids(Building building, float craftSpeed, Cons2<Liquid, Float> con) {
		if(building instanceof PumpBuild pump && building.block instanceof Pump pb) {
			if(pump.liquidDrop != null) {
	            float fraction = pump.amount;
	            if(building instanceof SolidPumpBuild sp && building.block instanceof SolidPump spb) {
	            	fraction = Math.max(sp.validTiles + sp.boost + (spb.attribute == null ? 0 : spb.attribute.env()), 0);
	            }
				con.get(pump.liquidDrop, fraction * pb.pumpAmount * 60f * building.timeScale());
			}
		}
		if(building.block instanceof GenericCrafter crafter) {
			if(crafter.outputLiquids != null) {
				for (int i = 0; i < crafter.outputLiquids.length; i++) {
					LiquidStack output = crafter.outputLiquids[i];
					con.get(output.liquid, 60*output.amount*building.timeScale());
				}
			}
		}
	}

	public static void producePower(Building building, float craftSpeed, Cons<Float> con) {
		if(building instanceof GeneratorBuild generator) con.get(generator.getPowerProduction() * 60 * building.timeScale());
	}

	public static void produceHeat(Building building, float craftSpeed, Cons<Float> con) {
		if(building instanceof HeatConductorBuild) return;
		if(building instanceof HeatBlock heatBlock) con.get(heatBlock.heat());
	}
	
	
	
	public static void produceBlock(Block block, int x, int y, Object config, float craftSpeed,
			Cons2<Item, Float> itemCons, Cons2<Liquid, Float> liquidCons, Cons<Float> powerCons, Cons<Float> heatCons) {
		if(block instanceof Drill drill) {
			ItemStack stack = countOre(drill, Vars.world.tile(x, y));
			if(stack != null) {
				float speed = drillSpeed(drill, stack.item, needDrillWaterBoost(drill, stack.item));
				speed /= (drill.size*drill.size);
				itemCons.get(stack.item, (float) stack.amount*speed);
			}
		}
		if(block instanceof GenericCrafter crafter) {
			if(crafter.outputItems != null) {
				for (int i = 0; i < crafter.outputItems.length; i++) {
					ItemStack output = crafter.outputItems[i];
					itemCons.get(output.item, craftSpeed*output.amount);
				}
			}
			if(crafter.outputLiquids != null) {
				for (int i = 0; i < crafter.outputLiquids.length; i++) {
					LiquidStack output = crafter.outputLiquids[i];
					liquidCons.get(output.liquid, 60*output.amount);
				}
			}
		}
		if(block instanceof Separator separator) {
			if(separator.results != null) {
				for (int i = 0; i < separator.results.length; i++) {
					ItemStack output = separator.results[i];
					itemCons.get(output.item, craftSpeed*output.amount/separator.results.length);
				}
			}
		}
		if(block instanceof Pump pump) {
			LiquidStack liquidStack = countLiquid(pump, Vars.world.tile(x, y));
			if(liquidStack != null) {
				if(block instanceof SolidPump) {
					liquidCons.get(liquidStack.liquid, liquidStack.amount * pump.pumpAmount * 60f / block.size / block.size);
				} else {
					liquidCons.get(liquidStack.liquid, liquidStack.amount * pump.pumpAmount * 60f);
				}
			}
		}
		if(block instanceof PowerGenerator g) powerCons.get(g.powerProduction*60f);
		if(block instanceof HeatProducer p) heatCons.get(p.heatOutput);
	}

	protected static LiquidStack countLiquid(Pump pump, Tile tile){
		if(tile == null) return null;
        final Seq<Tile> tempTiles = new Seq<>();

        if(pump instanceof SolidPump solidPump) {
        	float amount = 0;
    		for(Tile other : tile.getLinkedTilesAs(pump, tempTiles)){
    	     	if(other != null && !other.floor().isLiquid) {
    				amount += solidPump.baseEfficiency;
    				if(solidPump.attribute != null) {
    					amount += other.floor().attributes.get(solidPump.attribute);
    				}
    	     	}
    		}
    		return new LiquidStack(solidPump.result, amount);
        }
        
        float amount = 0f;
		Liquid liquidDrop = null;

		for(Tile other : tile.getLinkedTilesAs(pump, tempTiles)){
	     	if(other != null && other.floor().liquidDrop != null) {
				liquidDrop = other.floor().liquidDrop;
				amount += other.floor().liquidMultiplier;
	     	}
		}
		if(liquidDrop == null) return null;
		return new LiquidStack(liquidDrop, amount);
	}
	
	protected static @Nullable ItemStack countOre(Drill drill, @Nullable Tile tile){
		if(tile == null) return null;
		
	    final ObjectIntMap<Item> oreCount = new ObjectIntMap<>();
	    final Seq<Item> itemArray = new Seq<>();
        final Seq<Tile> tempTiles = new Seq<>();
	    
        for(Tile other : tile.getLinkedTilesAs(drill, tempTiles)){
            if(drill.canMine(other)){
                oreCount.increment(drill.getDrop(other), 0, 1);
            }
        }

        for(Item item : oreCount.keys()){
            itemArray.add(item);
        }

        itemArray.sort((item1, item2) -> {
            int type = Boolean.compare(!item1.lowPriority, !item2.lowPriority);
            if(type != 0) return type;
            int amounts = Integer.compare(oreCount.get(item1, 0), oreCount.get(item2, 0));
            if(amounts != 0) return amounts;
            return Integer.compare(item1.id, item2.id);
        });

        if(itemArray.size == 0){
            return null;
        }

        return new ItemStack(itemArray.peek(), oreCount.get(itemArray.peek(), 0));
    }

	public static void consumeBlock(Block block, int x, int y, Object config, float craftSpeed,
			Cons2<Item, Float> itemCons, Cons2<Liquid, Float> liquidCons, Cons<Float> powerCons, Cons<Float> heatCons) {
		if(block.consumers != null) {
			for (int c = 0; c < block.consumers.length; c++) {
				Consume consume = block.consumers[c];
				if(consume instanceof ConsumeItems items) {
					ItemStack[] stacks = items.items;
					for (int i = 0; i < stacks.length; i++) {
						ItemStack stack = stacks[i];
						float ips = craftSpeed*stack.amount;
						itemCons.get(stack.item, ips);
					}
					continue;
				}
//				if(consume instanceof ConsumeItemDynamic && config instanceof Integer) {
//					int id = (Integer) config;
//					ConsumeItemDynamic dynamic = (ConsumeItemDynamic) consume;
//					dynamic.items.get(tmpBuild(config))
//					if(plan != -1) {
//						ItemStack[] requirements = ((UnitFactory)block).plans.get(plan).requirements;
//						for (int i = 0; i < requirements.length; i++) {
//							ItemStack stack = requirements[i];
//							float ips = craftSpeed*stack.amount;
//							itemCons.get(stack.item, ips);
//						}
//					}
//					continue;
//				}
//				if(consume instanceof ConsumeItemFilter) {
//					ConsumeItemFilter filter = (ConsumeItemFilter) consume;
////					liquidCons.get(filter.it, 60*liquid.amount);
////					continue;
//				}
				if(consume instanceof ConsumePower power) powerCons.get(power.usage*60);
				if(consume instanceof ConsumeLiquid liquid) {
					liquidCons.get(liquid.liquid, 60*liquid.amount);
					continue;
				}
			}
		}
		if(block instanceof HeatCrafter heatCrafter) heatCons.get(heatCrafter.heatRequirement * heatCrafter.maxEfficiency);
		if(block instanceof PowerTurret powerTurret) heatCons.get(powerTurret.heatRequirement);
	}

	
	public static float drillSpeed(Drill drill, Item item, boolean liquid) {
		float waterBoost = 1;
		if(liquid) waterBoost = drill.liquidBoostIntensity*drill.liquidBoostIntensity;
		int area = drill.size*drill.size;
		return 60f*area*waterBoost/drill.getDrillTime(item);
	}

	public static boolean needDrillWaterBoost(Drill drill, Item item) {
		return drillSpeed(drill, item, false) >= .75f; // /(drill.size*drill.size)
	}

	public static boolean acceptKey() {
		return !Vars.state.isMenu() 
        		&& !Vars.ui.chatfrag.shown() 
        		&& !Vars.ui.schematics.isShown() 
        		&& !Vars.ui.database.isShown() 
        		&& !Vars.ui.consolefrag.shown() 
        		&& !Vars.ui.content.isShown()
        		&& !Vars.ui.logic.isShown()
        		&& !Vars.ui.research.isShown()
        		&& Core.scene.getKeyboardFocus() == null;
	}
	

	public static boolean acceptKeyNoFocus() {
		return !Vars.state.isMenu() 
        		&& !Vars.ui.chatfrag.shown() 
        		&& !Vars.ui.schematics.isShown() 
        		&& !Vars.ui.database.isShown() 
        		&& !Vars.ui.consolefrag.shown() 
        		&& !Vars.ui.content.isShown()
        		&& !Vars.ui.logic.isShown()
        		&& !Vars.ui.research.isShown();
	}

	public static int getMaximumAccepted(Block block, Item item) {
		return block.newBuilding().getMaximumAccepted(item);
	}
	
	public static Seq<ItemStack> getMaximumAcceptedConsumers(Block block) {
		Building tmp = block.newBuilding();
		tmp.block = block;
		
		Seq<ItemStack> items = new Seq<ItemStack>();
		
		if(tmp instanceof StorageBuild) {
			if(Vars.state.getPlanet() == Planets.serpulo) {
				Items.serpuloItems.each(i -> items.add(new ItemStack(i, tmp.getMaximumAccepted(i))));
			}
			if(Vars.state.getPlanet() == Planets.erekir) {
				Items.serpuloItems.each(i -> items.add(new ItemStack(i, tmp.getMaximumAccepted(i))));
			}
			if(Vars.state.getPlanet() == Planets.sun) {
				Vars.content.items().each(i -> items.add(new ItemStack(i, tmp.getMaximumAccepted(i))));
			}
			return items;
		}
		
		if(block instanceof ItemTurret iTurret && tmp instanceof ItemTurretBuild turret) {
			for (int item = Vars.content.items().size-1; item >= 0; item--) {
				int maximumAccepted = turret.acceptStack(Vars.content.item(item), Integer.MAX_VALUE, null);
				if(maximumAccepted > 0) items.add(
						new ItemStack(Vars.content.item(item), maximumAccepted));
			}
			items.sort(s -> iTurret.ammoTypes.get(s.item).estimateDPS());
			return items;
		}
		if(block.consumers != null) {
			for (int c = 0; c < block.consumers.length; c++) {
				consumeItems(block.consumers[c], tmp, 1f, (item, ips) -> {
					items.add(new ItemStack(item, tmp.getMaximumAccepted(item)));
				});
			}
		}
		return items;
	}
	public static Seq<ItemStack> getMaximumAcceptedConsumers(Building build) {
		Block block = build.block;
		Seq<ItemStack> items = new Seq<ItemStack>();
		
		if(build instanceof StorageBuild) {
			if(Vars.state.getPlanet() == Planets.serpulo) {
				Items.serpuloItems.each(i -> items.add(new ItemStack(i, build.getMaximumAccepted(i))));
			}
			if(Vars.state.getPlanet() == Planets.erekir) {
				Items.serpuloItems.each(i -> items.add(new ItemStack(i, build.getMaximumAccepted(i))));
			}
			if(Vars.state.getPlanet() == Planets.sun) {
				Vars.content.items().each(i -> items.add(new ItemStack(i, build.getMaximumAccepted(i))));
			}
			return items;
		}
		
		if(block instanceof ItemTurret iTurret && build instanceof ItemTurretBuild turret) {
			for (int item = Vars.content.items().size-1; item >= 0; item--) {
				int maximumAccepted = turret.acceptStack(Vars.content.item(item), Integer.MAX_VALUE, null);
				if(maximumAccepted > 0) items.add(
						new ItemStack(Vars.content.item(item), maximumAccepted));
			}
			items.sort(s -> iTurret.ammoTypes.get(s.item).estimateDPS());
			return items;
		}
		if(block.consumers != null) {
			for (int c = 0; c < block.consumers.length; c++) {
				consumeItems(block.consumers[c], build, 1f, (item, ips) -> {
					items.add(new ItemStack(item, build.getMaximumAccepted(item)));
				});
			}
		}
		return items;
	}

	public static boolean hasKeyBoard() {
		if(Vars.mobile) return Core.input.useKeyboard();
		return true;
	}

	public static boolean isNetGame() {
		return Vars.net.active();
	}

	public static TextureRegionDrawable drawable(String name) {
		return new TextureRegionDrawable(AgzamMod.sprite(name));
	}

	public static Drawable drawable(String name, int scale) {
//		AtlasRegion region = Core.atlas.find("agzam4mod-" + name);
//		region.texture.setFilter(TextureFilter.mipMapLinearLinear);
//        if(region.splits != null){
//            int[] splits = region.splits;
//            NinePatch patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
//            int[] pads = region.pads;
//            if(pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
//            return new ScaledNinePatchDrawable(patch, scale);
//        }else{
//        }
    	return new TextureRegionDrawable(Core.atlas.find("agzam4mod-" + name), scale);
	}
	
	public static Pixmap createIcon(String name, Color outlineColor, int outlineRadius) {
		Pixmap p = Pixmaps.outline(Core.atlas.getPixmap("agzam4mod-" + name), outlineColor, outlineRadius);
        return p;
	}

	public static boolean canBuild(Unit unit, BuildPlan plan) {
		if(plan.breaking) return true;
        if(unit.core() == null) return false;
		for (int i = 0; i < plan.block.requirements.length; i++) {
			ItemStack req = plan.block.requirements[i];
			if(!unit.core().items.has(req.item)) return false;
		}
		return true;
	}


	public static void getRequired(ConstructBuild cb, Cons2<Item, Integer> amount) {
		for (int i = 0; i < cb.current.requirements.length; i++) {
			ItemStack stack = cb.current.requirements[i];
			amount.get(stack.item, (int) ((stack.amount*(1f-cb.progress))*Vars.state.rules.buildCostMultiplier));
		}
	}
	
	
//	private static Building tmpBuilding = new Buildi
	
//	private static Building tmpBuild(Object config) {
//		Blocks.additiveReconstructor.newBuilding();
//		return null;
//	}
	
	
	
}
