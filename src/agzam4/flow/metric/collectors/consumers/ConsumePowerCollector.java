package agzam4.flow.metric.collectors.consumers;

import agzam4.flow.metric.collectors.ConsumeCollector;
import agzam4.flow.metric.metrics.PowerMetrics;
import mindustry.gen.Building;
import mindustry.world.consumers.ConsumePower;

public class ConsumePowerCollector<T extends ConsumePower> extends ConsumeCollector<T> {

	public PowerMetrics power;
	
	@Override
	public void setupMetric() {
		super.setupMetric();
		power = metrics.get(PowerMetrics.class);
	}
	
	@Override
	protected void collect(T cons, Building building, float scale) {
		power.sub(cons.usage * 60f * scale);
	}
	
	
}
