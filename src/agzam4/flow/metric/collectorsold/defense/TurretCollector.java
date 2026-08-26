package agzam4.flow.metric.collectorsold.defense;

import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;

public class TurretCollector<T extends Turret, B extends TurretBuild> extends ReloadTurretCollector<T, B> {

	@Override
	protected void collectDps(B building) {
		float dps = dps(building);
		if(block(building).targetAir) combat.airDps += dps;
		if(block(building).targetGround) combat.groundDps += dps;
	}
	
}
