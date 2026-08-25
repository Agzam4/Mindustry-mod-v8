package agzam4.flow.metric;

import agzam4.flow.metric.collectors.PowerCollectors.PowerGeneratorCollector;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.blocks.power.PowerGenerator.GeneratorBuild;

public class Collectors {
	
	public static MetricCollector<Building> buildingCollector = new MetricCollector<>(p -> p);
	public static MetricCollector<BuildPlan> buildPlanCollector = new MetricCollector<>(p -> p.block);
	
	public static void init() {
		PowerMetrics power = new PowerMetrics();
		

		buildingCollector.metric(power);
		buildingCollector.register(GeneratorBuild.class, new PowerGeneratorCollector());
		buildingCollector.build();
	}
	
	
}
