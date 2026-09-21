package agzam4.flow.metric.collectors.consumers.liquids;

import agzam4.flow.metric.collectors.ConsumeCollector;
import agzam4.flow.metric.metrics.LiquidMetric;
import mindustry.gen.Building;
import mindustry.world.consumers.ConsumeLiquids;

public class ConsumeLiquidsCollector<T extends ConsumeLiquids> extends ConsumeCollector<T> {

	public LiquidMetric liquids;
	
	@Override
	public void setupMetric() {
		super.setupMetric();
		liquids = metrics.get(LiquidMetric.class);
	}
	
	@Override
	protected void collect(T cons, Building building, float scale) {
        float mult = cons.multiplier.get(building);
        for(var stack : cons.liquids){
        	liquids.sub(stack.liquid, stack.amount * scale * mult);
        }
	}
	
	
}
