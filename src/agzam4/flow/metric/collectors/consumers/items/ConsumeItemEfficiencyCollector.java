package agzam4.flow.metric.collectors.consumers.items;

import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.consumers.ConsumeItemEfficiency;

public class ConsumeItemEfficiencyCollector<T extends ConsumeItemEfficiency> extends ConsumeItemFilterCollector<T> {

	public ConsumeItemEfficiencyCollector() {}
	
	public ConsumeItemEfficiencyCollector(ConsumeItemsCache cache) {
		this.cache = cache;
	}

	@Override
	protected void collect(T cons, Building building, float scale) {
		Item consumed = consumed(cons, building);
		if(consumed == null) return;
		items.sub(consumed, cons.itemDurationMultipliers == null ? scale : scale / cons.itemDurationMultipliers.get(consumed, 1f));
	}

	@Override
	protected String debug(T cons, Building building, float scale) {
		Item consumed = cons.getConsumed(building);
		if(consumed == null) return "";
		float s = cons.itemDurationMultipliers == null ? scale : scale / cons.itemDurationMultipliers.get(consumed, 1f);
		return s + " " + consumed.emoji();
	}

	
}
