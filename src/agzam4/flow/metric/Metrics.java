package agzam4.flow.metric;

import arc.struct.ObjectMap;

public class Metrics {

	private ObjectMap<Class<?>, Metric> metrics = new ObjectMap<>();

	public Metrics metric(Metric metric) {
		metrics.put(metric.getClass(), metric);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Metric> T get(Class<T> cls) {
		return (T) metrics.get(cls);
	}
	
	public void reset() {
		metrics.values().forEach(Metric::reset);
	}
	
}
