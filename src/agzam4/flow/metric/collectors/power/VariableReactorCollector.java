package agzam4.flow.metric.collectors.power;

import mindustry.world.blocks.power.VariableReactor;
import mindustry.world.blocks.power.VariableReactor.VariableReactorBuild;

public class VariableReactorCollector<T extends VariableReactor, B extends VariableReactorBuild> extends PowerGeneratorCollector<T, B> {

	@Override
	public void collect(B building) {
		super.collect(building);
	}
	
	@Override
	public void producePower(B building) {
		super.producePower(building);
	}
	
	@Override
	protected void consume(B building) {
		super.consume(building);
		consumeHeat(building);
	}

	protected void consumeHeat(B building) {
		heat.sub(building.heatRequirement());
	}
}
