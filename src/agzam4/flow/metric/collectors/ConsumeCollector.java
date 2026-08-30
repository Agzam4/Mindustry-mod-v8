package agzam4.flow.metric.collectors;

import agzam4.flow.metric.metrics.HeatMetric;
import agzam4.flow.metric.metrics.ItemsMetric;
import agzam4.flow.metric.metrics.LiquidMetric;
import agzam4.flow.metric.metrics.Metrics;
import agzam4.flow.metric.metrics.PowerMetrics;
import arc.util.Log;
import mindustry.gen.Building;
import mindustry.world.consumers.Consume;

public class ConsumeCollector<T extends Consume> {


	public Metrics metrics;

	protected PowerMetrics power;
	protected ItemsMetric items;
	protected LiquidMetric liquids;
	protected HeatMetric heat; // TODO: remove
	
	public ConsumeCollector() {
		
	}
	
	public void setupMetric() {
		items = metrics.get(ItemsMetric.class);
		liquids = metrics.get(LiquidMetric.class);
		power = metrics.get(PowerMetrics.class);
		heat = metrics.get(HeatMetric.class);
		Log.info("@, @, @, @", items, liquids, power, heat);
	}

	protected void collect(T cons, Building building, float scale) {}

	@SuppressWarnings("unchecked")
	public void from(Consume cons, Building building, float scale) {
		collect((T) cons, building, scale);
	}

	
}
