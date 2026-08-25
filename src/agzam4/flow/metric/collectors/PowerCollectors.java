package agzam4.flow.metric.collectors;

import agzam4.flow.metric.Metrics;
import agzam4.flow.metric.PowerMetrics;
import mindustry.gen.Building;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.blocks.power.PowerGenerator.GeneratorBuild;

public class PowerCollectors {
	
	public static class PowerGeneratorCollector implements Collector<PowerGenerator, GeneratorBuild, Building> {

		PowerMetrics power;
		
		@Override
		public void init(Metrics metrics) {
			power = metrics.get(PowerMetrics.class);
		}
		
		@Override
		public void collect(PowerGenerator block, GeneratorBuild g, Building payload) {
			power.power += block.powerProduction*60f;
		}
		
	}
	
}
