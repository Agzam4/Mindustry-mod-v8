package agzam4.flow.metric;

import agzam4.flow.metric.collectors.BlockCollector;
import agzam4.flow.metric.collectors.ConsumeCollector;
import agzam4.flow.metric.metrics.Metric;
import agzam4.flow.metric.metrics.Metrics;
import arc.util.ArcRuntimeException;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;

public class MetricCollector {

	private ClassRegistry<BlockCollector<?, ?>> blockCollectors = new ClassRegistry<>();
	private ClassRegistry<ConsumeCollector<?>> consumeCollectors = new ClassRegistry<>();
	private Metrics metrics = new Metrics();
	
	public MetricCollector metric(Metric metric) {
		metrics.metric(metric);
		return this;
	}
	
	public void reset() {
		metrics.reset();
	}

	public <B extends Block> void register(Class<B> cls, BlockCollector<B, ?> collector) {
		blockCollectors.register(cls, collector);
	}

	public <C extends Consume> void register(Class<C> cls, ConsumeCollector<C> collector) {
		consumeCollectors.register(cls, collector);
	}
	
	public boolean registrated(Class<?> cls) {
		return blockCollectors.registrated(cls);
	}
	
	boolean builded = false;
	
	public void build() {
		blockCollectors.each(e -> {
			e.metrics = metrics;
			e.consumersRegistry = consumeCollectors;
			e.setupMetric();
		});
		consumeCollectors.each(e -> {
			e.metrics = metrics;
			e.setupMetric();
		});
		builded = true;
	}
	
	public BlockCollector<?, ?> collector(Block block) {
		if(!builded) throw new ArcRuntimeException("Not builded");
		var collector = blockCollectors.get(block.getClass());
		if(collector == null) return null;
		return collector;
	}

	public <T extends Metric> T metric(Class<T> c) {
		return metrics.get(c);
	}
	
}
