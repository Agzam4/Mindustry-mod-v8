package agzam4.flow.metric;

import agzam4.flow.metric.collectors.BaseCollector;
import agzam4.flow.metric.metrics.Metric;
import agzam4.flow.metric.metrics.Metrics;
import arc.util.ArcRuntimeException;
import mindustry.world.Block;

public class MetricCollector {

	private ClassRegistry<BaseCollector<?, ?>> registry = new ClassRegistry<>();
	private Metrics metrics = new Metrics();
	
	public MetricCollector metric(Metric metric) {
		metrics.metric(metric);
		return this;
	}
	
	public void reset() {
		metrics.reset();
	}

	public <B extends Block> void register(Class<B> cls, BaseCollector<B, ?> collector) {
		registry.register(cls, collector);
	}
	
	public boolean registrated(Class<?> cls) {
		return registry.registrated(cls);
	}
	
	boolean builded = false;
	
	public void build() {
		registry.each(e -> e.metrics = metrics);
		registry.each(e -> e.setupMetric());
		builded = true;
	}
	
	public BaseCollector<?, ?> collector(Block block) {
		if(!builded) throw new ArcRuntimeException("Not builded");
		var collector = registry.get(block.getClass());
		if(collector == null) return null;
		return collector;
	}

	public <T extends Metric> T metric(Class<T> c) {
		return metrics.get(c);
	}
	
}
