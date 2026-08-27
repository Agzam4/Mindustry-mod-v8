package agzam4.flow.metric.collectors.production;

import agzam4.flow.metric.collectors.BaseCollector;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.production.Drill.DrillBuild;

public class DrillCollector<T extends Drill, B extends DrillBuild> extends BaseCollector<T, B> {
	
	@Override
	protected void produce(B building) {
		super.produce(building);
		produceItems(building);
	}
	
	protected void produceItems(B drill) {
		if(drill.dominantItem == null) return;
		items.add(drill.dominantItem, drill.lastDrillSpeed*60f*drill.timeScale());
	}
	
}
