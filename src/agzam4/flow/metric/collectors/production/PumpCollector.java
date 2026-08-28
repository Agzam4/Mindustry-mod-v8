package agzam4.flow.metric.collectors.production;

import agzam4.flow.metric.collectors.BaseCollector;
import mindustry.world.blocks.production.Pump;
import mindustry.world.blocks.production.Pump.PumpBuild;

public class PumpCollector<T extends Pump, B extends PumpBuild> extends BaseCollector<T, B> {

	@Override
	protected void produce(B building) {
		super.produce(building);
		produceLiquids(building);
	}

	protected void produceLiquids(B pump) {
		if(pump.liquidDrop == null) return;
		liquids.add(pump.liquidDrop, fraction(pump) * block(pump).pumpAmount * 60f * pump.timeScale());
	}
	
	public float fraction(B pump) {
		return pump.amount;
	}
	
}
