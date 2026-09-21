package agzam4.flow.metric.collectors.consumers.payload;

import static mindustry.Vars.content;

import agzam4.flow.metric.collectors.ConsumeCollector;
import agzam4.flow.metric.metrics.PayloadMetric;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Building;
import mindustry.world.consumers.ConsumePayloadFilter;

public class ConsumePayloadFilterCollector<T extends ConsumePayloadFilter> extends ConsumeCollector<T> {

	public PayloadMetric payloads;
	
	@Override
	public void setupMetric() {
		super.setupMetric();
		payloads = metrics.get(PayloadMetric.class);
	}
	
	@Override
	protected void collect(T cons, Building building, float scale) {
		super.collect(cons, building, scale);
		UnlockableContent[] fitting = Vars.content.blocks().copy().<UnlockableContent>as().add(content.units().as()).select(cons.filter).toArray(UnlockableContent.class);
        for(var block : fitting){
            payloads.sub(block, 1f*scale); // XXX: 1?
        }
	}
	
}
