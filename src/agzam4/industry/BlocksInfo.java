package agzam4.industry;

import java.lang.reflect.Field;

import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.AttributeCrafter.AttributeCrafterBuild;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.blocks.units.UnitFactory.UnitFactoryBuild;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.consumers.ConsumeItemFilter;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquid;

public class BlocksInfo {

	public static float craftSpeed(Building building) {
		Block block = building.block;
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
		if(block instanceof GenericCrafter) {
			craftSpeed = 60f / ((GenericCrafter) block).craftTime;
		}
		if(building instanceof AttributeCrafterBuild) {
			craftSpeed *= ((AttributeCrafterBuild) building).efficiencyMultiplier();
		}
		if(building instanceof UnitFactoryBuild && block instanceof UnitFactory) {
			int plan = ((UnitFactoryBuild)building).currentPlan;
			if(plan == -1) return 0;
			craftSpeed = 60f/((UnitFactory)block).plans.get(plan).time;
		}
		if(block instanceof Reconstructor) {
			craftSpeed = 60f/((Reconstructor)block).constructTime;
		}
		if(block instanceof ConsumeGenerator) {
			craftSpeed = 60f/((ConsumeGenerator)block).itemDuration;
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
}
