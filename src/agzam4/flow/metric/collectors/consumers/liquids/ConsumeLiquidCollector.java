package agzam4.flow.metric.collectors.consumers.liquids;

import agzam4.flow.metric.collectors.ConsumeCollector;
import agzam4.flow.metric.metrics.LiquidMetric;
import mindustry.gen.Building;
import mindustry.world.consumers.ConsumeLiquid;

public class ConsumeLiquidCollector<T extends ConsumeLiquid> extends ConsumeCollector<T> {

	public LiquidMetric liquids;
	
	@Override
	public void setupMetric() {
		super.setupMetric();
		liquids = metrics.get(LiquidMetric.class);
	}
	
	@Override
	protected void collect(T cons, Building building, float scale) {
		liquids.sub(cons.liquid, scale*cons.amount*cons.multiplier.get(building)*60f);
	}
	
	
}
