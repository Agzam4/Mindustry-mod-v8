package agzam4.flow.metric.collectors.production;

import mindustry.world.blocks.production.AttributeCrafter;
import mindustry.world.blocks.production.AttributeCrafter.AttributeCrafterBuild;

public class AttributeCrafterCollector<T extends AttributeCrafter, B extends AttributeCrafterBuild> extends GenericCrafterCollector<T, B> {
	
	@Override
	public float craftSpeed(B building) {
		return super.craftSpeed(building) * building.efficiencyMultiplier();
	}
	
}
