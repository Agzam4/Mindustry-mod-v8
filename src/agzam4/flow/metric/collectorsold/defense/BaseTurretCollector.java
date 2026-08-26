package agzam4.flow.metric.collectorsold.defense;

import agzam4.flow.metric.collectors.BaseCollector;
import agzam4.flow.metric.metrics.CombatMetrics;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.defense.turrets.BaseTurret.BaseTurretBuild;

public class BaseTurretCollector<T extends BaseTurret, B extends BaseTurretBuild> extends BaseCollector<T, B> {

	public CombatMetrics combat;
	
	@Override
	public void setupMetric() {
		super.setupMetric();
		combat = metrics.get(CombatMetrics.class);
	}
	
	
	@Override
	protected void collect(B building) {
		super.collect(building);
		collectDps(building);
	}

	protected void collectDps(B building) {
		// BaseTurretBuild::estimateDps returns zero so nothing to do
	}

	public float dps(B building) {
		return 0;
	}
	
}
