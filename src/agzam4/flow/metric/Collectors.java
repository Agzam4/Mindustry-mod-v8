package agzam4.flow.metric;

import agzam4.debug.Debug;
import agzam4.flow.metric.collectors.BaseCollector;
import agzam4.flow.metric.collectorsold.defense.BaseTurretCollector;
import agzam4.flow.metric.collectorsold.defense.ReloadTurretCollector;
import agzam4.flow.metric.metrics.CombatMetrics;
import agzam4.flow.metric.metrics.HeatMetric;
import agzam4.flow.metric.metrics.ItemsMetric;
import agzam4.flow.metric.metrics.LiquidMetric;
import agzam4.flow.metric.metrics.PowerMetrics;
import arc.struct.ObjectSet;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.defense.turrets.BaseTurret.BaseTurretBuild;
import mindustry.world.blocks.defense.turrets.ReloadTurret;
import mindustry.world.blocks.defense.turrets.ReloadTurret.ReloadTurretBuild;

public class Collectors {
	
	public static MetricCollector collectors = new MetricCollector();

	public static ItemsMetric items = new ItemsMetric();
	public static LiquidMetric liquids = new LiquidMetric();
	public static PowerMetrics power = new PowerMetrics();
	public static CombatMetrics combat = new CombatMetrics();
	public static HeatMetric heat = new HeatMetric();
	
	public static void init() {

		collectors.metric(items);
		collectors.metric(liquids);
		collectors.metric(power);
		collectors.metric(combat);
		collectors.metric(heat);
		
		collectors.register(Block.class, new BaseCollector<Block, Building>());
		collectors.register(BaseTurret.class, new BaseTurretCollector<BaseTurret, BaseTurretBuild>());
		collectors.register(ReloadTurret.class, new ReloadTurretCollector<ReloadTurret, ReloadTurretBuild>());
		collectors.register(BaseTurret.class, new BaseTurretCollector<BaseTurret, BaseTurretBuild>());
		
		collectors.build();
		
		if(Debug.debug) {
			ObjectSet<Class<?>> classes = ObjectSet.with();
			ObjectSet<Class<?>> interfaces = ObjectSet.with();
			
			Vars.content.blocks().forEach(b -> {
				if(!b.destructible && !b.update) return;
//				Blocks
				Class<?> cls = b.getClass();
				if(collectors.registrated(cls)) return;
				while (true) {
					if(!cls.toString().contains("$")) classes.add(cls);
					interfaces.addAll(cls.getInterfaces());
					cls = cls.getSuperclass();
					if(cls == Object.class) return;
				}
			});

			Log.info("=== classes ===\n@", classes.toSeq().sort((s1,s2)->s1.toString().compareTo(s2.toString())).toString("\n"));
			Log.info("=== interfaces ===\n@", interfaces.toSeq().toString("\n"));
		}
	}
	
	
}
