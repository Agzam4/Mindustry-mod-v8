package agzam4.flow.metric.collectors.consumers.payload;

import agzam4.flow.metric.collectors.ConsumeCollector;
import agzam4.flow.metric.metrics.ItemsMetric;
import agzam4.flow.metric.metrics.PayloadMetric;
import arc.Events;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.game.EventType.WorldLoadEndEvent;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.PayloadStack;
import mindustry.world.consumers.ConsumeItemFilter;
import mindustry.world.consumers.ConsumePayloadDynamic;
import mindustry.world.consumers.ConsumePayloadFilter;

public class ConsumePayloadDynamicCollector<T extends ConsumePayloadDynamic> extends ConsumeCollector<T> {

	public PayloadMetric payloads;
	
	@Override
	public void setupMetric() {
		super.setupMetric();
		payloads = metrics.get(PayloadMetric.class);
	}
	
	@Override
	protected void collect(T cons, Building building, float scale) {
		super.collect(cons, building, scale);
        float mult = cons.multiplier.get(building);
        for(PayloadStack stack : cons.payloads.get(building)){
        	payloads.sub(stack.item, Math.round(stack.amount * mult) * scale);
        }
	}
	
}
