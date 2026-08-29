package agzam4.flow.metric.collectors.production;

import mindustry.world.blocks.production.HeatCrafter;
import mindustry.world.blocks.production.HeatCrafter.HeatCrafterBuild;

public class HeatCrafterCollector<T extends HeatCrafter, B extends HeatCrafterBuild> extends GenericCrafterCollector<T, B> {
	
	@Override
	public float craftSpeed(B building) {
		return 60f / block(building).craftTime * building.efficiencyScale();
	}

	@Override
	protected void consume(B building) {
		super.consume(building);
		consumeHeat(building);
	}

	protected void consumeHeat(B building) {
		super.consume(building);
		heat.sub(building.heatRequirement() * block(building).maxEfficiency);
	}
	
}
