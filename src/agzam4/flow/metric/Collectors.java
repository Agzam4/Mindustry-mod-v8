package agzam4.flow.metric;

import agzam4.debug.Debug;
import agzam4.flow.metric.collectors.BlockCollector;
import agzam4.flow.metric.collectors.consumers.*;
import agzam4.flow.metric.collectors.consumers.items.ConsumeItemDynamicCollector;
import agzam4.flow.metric.collectors.consumers.items.ConsumeItemFilterCollector;
import agzam4.flow.metric.collectors.consumers.items.ConsumeItemsCollector;
import agzam4.flow.metric.collectors.consumers.liquids.ConsumeLiquidCollector;
import agzam4.flow.metric.collectors.consumers.liquids.ConsumeLiquidsCollector;
import agzam4.flow.metric.collectors.consumers.payload.ConsumePayloadDynamicCollector;
import agzam4.flow.metric.collectors.consumers.payload.ConsumePayloadFilterCollector;
import agzam4.flow.metric.collectors.consumers.payload.ConsumePayloadsCollector;
import agzam4.flow.metric.collectors.defense.*;
import agzam4.flow.metric.collectors.heat.*;
import agzam4.flow.metric.collectors.power.*;
import agzam4.flow.metric.collectors.production.*;
import agzam4.flow.metric.metrics.*;
import arc.struct.ObjectSet;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.BaseTurret.BaseTurretBuild;
import mindustry.world.blocks.defense.turrets.ReloadTurret.ReloadTurretBuild;
import mindustry.world.blocks.heat.HeatProducer;
import mindustry.world.blocks.heat.HeatProducer.HeatProducerBuild;
import mindustry.world.blocks.power.VariableReactor;
import mindustry.world.blocks.power.VariableReactor.VariableReactorBuild;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.production.AttributeCrafter.AttributeCrafterBuild;
import mindustry.world.blocks.production.Drill.DrillBuild;
import mindustry.world.blocks.production.GenericCrafter.GenericCrafterBuild;
import mindustry.world.blocks.production.HeatCrafter.HeatCrafterBuild;
import mindustry.world.blocks.production.Pump.PumpBuild;
import mindustry.world.blocks.production.SolidPump.SolidPumpBuild;
import mindustry.world.consumers.*;

public class Collectors {
	
	public static MetricCollector collectors = new MetricCollector();

	public static ItemsMetric items = new ItemsMetric();
	public static LiquidMetric liquids = new LiquidMetric();
	public static PowerMetrics power = new PowerMetrics();
	public static CombatMetrics combat = new CombatMetrics();
	public static HeatMetric heat = new HeatMetric();
	public static PayloadMetric payload = new PayloadMetric();
	
	public static void init() {

		collectors.metric(items);
		collectors.metric(liquids);
		collectors.metric(power);
		collectors.metric(combat);
		collectors.metric(heat);
		collectors.metric(payload);
		
		collectors.register(Block.class, new BlockCollector<Block, Building>());
		
		// defense
		collectors.register(BaseTurret.class, new BaseTurretCollector<BaseTurret, BaseTurretBuild>());
		collectors.register(ReloadTurret.class, new ReloadTurretCollector<ReloadTurret, ReloadTurretBuild>());
		collectors.register(BaseTurret.class, new BaseTurretCollector<BaseTurret, BaseTurretBuild>());

		// power
		collectors.register(VariableReactor.class, new VariableReactorCollector<VariableReactor, VariableReactorBuild>());
		
		// production
		collectors.register(GenericCrafter.class, new GenericCrafterCollector<GenericCrafter, GenericCrafterBuild>());
		collectors.register(Drill.class, new DrillCollector<Drill, DrillBuild>());
		collectors.register(HeatCrafter.class, new HeatCrafterCollector<HeatCrafter, HeatCrafterBuild>());
		collectors.register(AttributeCrafter.class, new AttributeCrafterCollector<AttributeCrafter, AttributeCrafterBuild>());
		collectors.register(HeatCrafter.class, new HeatCrafterCollector<HeatCrafter, HeatCrafterBuild>());
		// production liquids
		collectors.register(Pump.class, new PumpCollector<Pump, PumpBuild>());
		collectors.register(SolidPump.class, new SolidPumpCollector<SolidPump, SolidPumpBuild>());

		// heat
		collectors.register(HeatProducer.class, new HeatProducerCollector<HeatProducer, HeatProducerBuild>());
		
		
		// Consumers
		collectors.register(ConsumeItems.class, new ConsumeItemsCollector<ConsumeItems>());
		collectors.register(ConsumeItemDynamic.class, new ConsumeItemDynamicCollector<ConsumeItemDynamic>());
		
		collectors.register(ConsumeItemFilter.class, new ConsumeItemFilterCollector<ConsumeItemFilter>(
				new ConsumeItemFilterCollector.ConsumeItemsCache().setup())
				);

		collectors.register(ConsumePower.class, new ConsumePowerCollector<ConsumePower>());
		
		collectors.register(ConsumeLiquid.class, new ConsumeLiquidCollector<ConsumeLiquid>());
		collectors.register(ConsumeLiquids.class, new ConsumeLiquidsCollector<ConsumeLiquids>());

//		TODO:
//		collectors.register(ConsumePayloads.class, new ConsumePayloadsCollector<ConsumePayloads>());
//		collectors.register(ConsumePayloadDynamic.class, new ConsumePayloadDynamicCollector<ConsumePayloadDynamic>());
//		collectors.register(ConsumePayloadFilter.class, new ConsumePayloadFilterCollector<ConsumePayloadFilter>());
		
		collectors.build();
		
		if(Debug.debug) {
			ObjectSet<Class<?>> classes = ObjectSet.with();
			ObjectSet<Class<?>> interfaces = ObjectSet.with();
			
			for (Block b : Vars.content.blocks()) {
				if(!b.destructible && !b.update) continue;
//				Blocks
				Class<?> cls = b.getClass();
				if(collectors.registrated(cls)) continue;
				while (true) {
					if(!cls.toString().contains("$")) classes.add(cls);
					interfaces.addAll(cls.getInterfaces());
					cls = cls.getSuperclass();
					if(cls == Object.class) break;
				}
			}

			Log.info("=== classes ===\n@", classes.toSeq().sort((s1,s2)->s1.toString().compareTo(s2.toString())).toString("\n"));
			Log.info("=== interfaces ===\n@", interfaces.toSeq().toString("\n"));
		}
	}
	
	
}
