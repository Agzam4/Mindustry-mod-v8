package agzam4.flow.metric.collectorsold.defense;

import mindustry.Vars;
import mindustry.type.Liquid;
import mindustry.world.blocks.defense.turrets.ReloadTurret;
import mindustry.world.blocks.defense.turrets.ReloadTurret.ReloadTurretBuild;

public class ReloadTurretCollector<T extends ReloadTurret, B extends ReloadTurretBuild> extends BaseTurretCollector<T, B> {

	@Override
	public float dps(B building) {
		float dps = building.estimateDps() * building.team().rules().blockDamageMultiplier*Vars.state.rules.blockDamageMultiplier;
		if(block(building).coolant != null && building.liquids != null) {
			Liquid liquid = building.liquids.current();
			if(building.liquids.get(liquid) > 0.01f) {
				float reload = block(building).reload;
				float maxUsed = block(building).coolant.amount;
				float multiplier = block(building).coolantMultiplier;
				
				// reload, coolant.amount, coolantMultiplier
                float reloadRate = 1f + maxUsed * multiplier * liquid.heatCapacity;
                float standardReload = reload;
                float result = standardReload / (reload / reloadRate);
				dps *= result;//efficiency(building);
			}
		}
		return dps;
	}
	
}
