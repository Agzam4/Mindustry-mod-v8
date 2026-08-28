package agzam4.flow.metric.metrics;

import arc.struct.ObjectMap;

public class Metrics {

	private ObjectMap<Class<?>, Metric> metrics = new ObjectMap<>();

	public Metrics metric(Metric metric) {
		metrics.put(metric.key(), metric);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Metric> T get(Class<T> cls) {
		return (T) metrics.get(cls);
	}
	
	public void reset() {
		//for-each, not forEach(): game's MapIterator.forEach crashes on Android (missing Iterable$-CC)
		for (Metric metric : metrics.values()) metric.reset();
	}
	
}
