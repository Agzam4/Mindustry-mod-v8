package agzam4.flow.metric.metrics;

import arc.func.Cons2;
import arc.struct.ObjectMap;
import mindustry.ctype.UnlockableContent;

public class PayloadMetric extends Metric {

	private ObjectMap<UnlockableContent, Counter> payloads = ObjectMap.of();
	
	@Override
	public void reset() {
		payloads.clear();
	}

	public void add(UnlockableContent content, float pps) {
		payloads.get(content, () -> new Counter()).value += pps;
	}
	
	public void sub(UnlockableContent content, float pps) {
		payloads.get(content, () -> new Counter()).value -= pps;
	}

	public int size() {
		return payloads.size;
	}

	public float get(UnlockableContent content) {
		var c = payloads.get(content);
		if(c == null) return 0;
		return c.value;
	}

	@Override
	public Class<? extends Metric> key() {
		return PayloadMetric.class;
	}
	
	private static class Counter {
		
		float value;
		
	}

	public void each(Cons2<UnlockableContent, Float> cons) {
		payloads.each((c,v) -> cons.get(c, v.value));
	}
	
	
}