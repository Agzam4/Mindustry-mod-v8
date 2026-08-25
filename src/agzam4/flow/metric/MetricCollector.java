package agzam4.flow.metric;

import agzam4.flow.metric.collectors.Collector;
import arc.func.Func;
import arc.util.Log;
import mindustry.world.Block;

public class MetricCollector<P> {

	private ClassRegistry<Collector<?, ?, P>> registry = new ClassRegistry<>();
	private Func<P, Object> extractor;
	
	public MetricCollector(Func<P, Object> extractor) {
		this.extractor = extractor;
	}

	private Metrics metrics = new Metrics();
	
	public MetricCollector<P> metric(Metric metric) {
		metrics.metric(metric);
		return this;
	}
	
	public void reset() {
		metrics.reset();
	}

	public <T> void register(Class<T> cls, Collector<?, T, P> collector) {
		registry.register(cls, collector);
	}
	
	public void build() {
		registry.each(e -> e.init(metrics));
	}
	
	public void collect(Block block, P payload) {
		var object = extractor.get(payload);
		Log.info("[@, @, @]", block, payload, object);
		if(object == null) return;
		Log.info("object: @", object.getClass());
		var collector = registry.get(object.getClass());
		Log.info("collector: @", collector);
		if(collector == null) return;
		collector.collectRaw(block, object, payload);
	}

	public <T extends Metric> T metric(Class<T> c) {
		return metrics.get(c);
	}
	
}
