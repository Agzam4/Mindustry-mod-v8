package agzam4.flow.metric.collectors.production;

import mindustry.world.blocks.production.SolidPump;
import mindustry.world.blocks.production.SolidPump.SolidPumpBuild;

public class SolidPumpCollector<T extends SolidPump, B extends SolidPumpBuild> extends PumpCollector<T, B> {

	@Override
	public float fraction(B sp) {
		return Math.max(sp.validTiles + sp.boost + (block(sp).attribute == null ? 0 : block(sp).attribute.env()), 0);
	}
	
}
