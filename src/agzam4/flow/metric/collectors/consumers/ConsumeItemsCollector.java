package agzam4.flow.metric.collectors.consumers;

import agzam4.flow.metric.collectors.ConsumeCollector;
import agzam4.flow.metric.metrics.ItemsMetric;
import mindustry.type.ItemStack;
import mindustry.world.consumers.ConsumeItems;

public class ConsumeItemsCollector<T extends ConsumeItems> extends ConsumeCollector<T> {

	ItemsMetric items;
	
	@Override
	public void setupMetric() {
		super.setupMetric();
		items = metrics.get(ItemsMetric.class);
	}
	
	@Override
	protected void collect(T cons, float scale) {
		super.collect(cons, scale);
		ItemStack[] stacks = cons.items;
		for (int item = 0; item < stacks.length; item++) {
			ItemStack stack = stacks[item];
			items.sub(stack.item, scale*stack.amount);
		}
	}
	
	
	
	
}
