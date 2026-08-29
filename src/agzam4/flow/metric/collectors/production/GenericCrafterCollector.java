package agzam4.flow.metric.collectors.production;

import agzam4.flow.metric.collectors.BlockCollector;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.GenericCrafter.GenericCrafterBuild;

public class GenericCrafterCollector<T extends GenericCrafter, B extends GenericCrafterBuild> extends BlockCollector<T, B> {
	
	@Override
	protected void produce(B building) {
		super.produce(building);
		produceItems(building);
		produceLiquids(building);
	}
	
	protected void produceItems(B building) {
		var crafter = block(building);
		if(crafter.outputItems != null) {
			for (int i = 0; i < crafter.outputItems.length; i++) {
				ItemStack output = crafter.outputItems[i];
				items.add(output.item, craftSpeed(building)*output.amount*building.timeScale());
			}
		}
	}

	protected void produceLiquids(B building) {
		var crafter = block(building);
		if(crafter.outputLiquids != null) {
			for (int i = 0; i < crafter.outputLiquids.length; i++) {
				LiquidStack output = crafter.outputLiquids[i];
				liquids.add(output.liquid, 60f*output.amount*building.timeScale());
			}
		}
	}


	public float craftSpeed(B building) {
		return 60f / block(building).craftTime * building.efficiencyScale();
	}
	
}
