package agzam4.flow.metric.collectors.heat;

import agzam4.flow.metric.collectors.BaseCollector;
import agzam4.flow.metric.metrics.CombatMetrics;
import agzam4.flow.metric.metrics.HeatMetric;
import mindustry.world.blocks.heat.HeatProducer;
import mindustry.world.blocks.heat.HeatProducer.HeatProducerBuild;

public class HeatProducerCollector<T extends HeatProducer, B extends HeatProducerBuild> extends BaseCollector<T, B> {

	public HeatMetric heat;
	
	@Override
	public void setupMetric() {
		super.setupMetric();
		heat = metrics.get(HeatMetric.class);
	}
	
	@Override
	protected void collect(B building) {
		super.collect(building);
		collectHeat(building);
	}

	protected void collectHeat(B building) {
		heat.add(building.heat());
	}

}
