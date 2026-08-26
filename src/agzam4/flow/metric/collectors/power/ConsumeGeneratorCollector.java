package agzam4.flow.metric.collectors.power;

import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.blocks.power.ConsumeGenerator.ConsumeGeneratorBuild;

public class ConsumeGeneratorCollector<T extends ConsumeGenerator, B extends ConsumeGeneratorBuild> extends PowerGeneratorCollector<T, B> {

	
	@Override
	public void collect(B building) {
		super.collect(building);
	}


	
	@Override
	public void producePower(B building) {
		super.producePower(building);
	}
	
}
