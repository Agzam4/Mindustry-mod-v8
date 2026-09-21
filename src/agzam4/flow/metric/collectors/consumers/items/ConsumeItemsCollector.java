package agzam4.flow.metric.collectors.consumers.items;

import agzam4.flow.metric.collectors.ConsumeCollector;
import agzam4.flow.metric.metrics.ItemsMetric;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.world.consumers.ConsumeItems;

public class ConsumeItemsCollector<T extends ConsumeItems> extends ConsumeCollector<T> {

	public ItemsMetric items;
	
	@Override
	public void setupMetric() {
		super.setupMetric();
		items = metrics.get(ItemsMetric.class);
	}
	
	@Override
	protected void collect(T cons, Building building, float scale) {
		super.collect(cons, building, scale);
		ItemStack[] stacks = cons.items;
		for (int item = 0; item < stacks.length; item++) {
			ItemStack stack = stacks[item];
			items.sub(stack.item, scale*Math.round(stack.amount*cons.multiplier.get(building)));
		}
	}
	
	
}
