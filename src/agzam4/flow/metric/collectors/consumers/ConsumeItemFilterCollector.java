package agzam4.flow.metric.collectors.consumers;

import agzam4.flow.metric.collectors.ConsumeCollector;
import agzam4.flow.metric.metrics.ItemsMetric;
import arc.Events;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.game.EventType.WorldLoadEndEvent;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.consumers.ConsumeItemFilter;

public class ConsumeItemFilterCollector<T extends ConsumeItemFilter> extends ConsumeCollector<T> {

	public ItemsMetric items;
	
	public @Nullable ConsumeItemsCache cache;

	public ConsumeItemFilterCollector() {}
	
	public ConsumeItemFilterCollector(ConsumeItemsCache cache) {
		this.cache = cache;
	}
	
	@Override
	public void setupMetric() {
		super.setupMetric();
		items = metrics.get(ItemsMetric.class);
	}
	
	@Override
	protected void collect(T cons, Building building, float scale) {
		super.collect(cons, building, scale);
		Item consumed = cons.getConsumed(building);
		if(cache != null) {
			if(consumed == null) consumed = cache.get(building);
			else cache.put(building, consumed);
		}
		if(consumed == null) return;
		items.sub(consumed, scale);
	}

	public static class ConsumeItemsCache {

		public static int[][] lastItems = null;
		
		public ConsumeItemsCache setup() {
			Events.on(WorldLoadEndEvent.class, e -> {
				lastItems = new int[Vars.world.width()][Vars.world.height()];
			});
			return this;
		}

		public @Nullable Item get(Building building) {
			int id = lastItems[building.tileX()][building.tileY()];
			if(id < 1) return null;
			return Vars.content.item(id-1);
		}
		
		public void put(Building building, Item item) {
			if(lastItems == null) lastItems = new int[Vars.world.width()][Vars.world.height()];
			lastItems[building.tileX()][building.tileY()] = item.id+1;
		}
		
	}
	
}
