package agzam4.flow.metric.collectors.consumers.payload;

import agzam4.flow.metric.collectors.ConsumeCollector;
import agzam4.flow.metric.metrics.PayloadMetric;
import mindustry.content.Blocks;
import mindustry.gen.Building;
import mindustry.type.PayloadStack;
import mindustry.world.consumers.ConsumePayloads;

public class ConsumePayloadsCollector<T extends ConsumePayloads> extends ConsumeCollector<T> {

	public PayloadMetric payloads;
	
	@Override
	public void setupMetric() {
		super.setupMetric();
		payloads = metrics.get(PayloadMetric.class);
	}
	
	@Override
	protected void collect(T cons, Building building, float scale) {
        float mult = cons.multiplier.get(building);
        for(PayloadStack stack : cons.payloads){
        	payloads.sub(stack.item, Math.round(stack.amount * mult) * scale);
        }
	}
	
	
}
