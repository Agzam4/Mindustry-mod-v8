package agzam4.flow.metric.collectors.consumers.items;

import mindustry.gen.Building;
import mindustry.world.consumers.ConsumeItemExplode;

public class ConsumeItemExplodeCollector<T extends ConsumeItemExplode> extends ConsumeItemFilterCollector<T> {

	public ConsumeItemExplodeCollector() {}
	
	public ConsumeItemExplodeCollector(ConsumeItemsCache cache) {
		this.cache = cache;
	}

	@Override
	protected void collect(T cons, Building building, float scale) {}

	protected String debug(T cons, Building building, float scale) { return "<no>"; }
	
}
