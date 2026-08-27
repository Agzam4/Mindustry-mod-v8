package agzam4.flow.metric.collectors;

import agzam4.ModWork;
import agzam4.flow.metric.metrics.HeatMetric;
import agzam4.flow.metric.metrics.ItemsMetric;
import agzam4.flow.metric.metrics.LiquidMetric;
import agzam4.flow.metric.metrics.Metrics;
import agzam4.flow.metric.metrics.PowerMetrics;
import arc.util.Log;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.Block;

public class BaseCollector<T extends Block, B extends Building> {

	public Metrics metrics;

	protected PowerMetrics power;
	protected ItemsMetric items;
	protected LiquidMetric liquids;
	protected HeatMetric heat; // TODO: remove
	
	public BaseCollector() {
		
	}
	
	public void setupMetric() {
		items = metrics.get(ItemsMetric.class);
		liquids = metrics.get(LiquidMetric.class);
		power = metrics.get(PowerMetrics.class);
		heat = metrics.get(HeatMetric.class);
		Log.info("@, @, @, @", items, liquids, power, heat);
	}

	@SuppressWarnings("unchecked")
	public final void from(Building building) {
		collect((B) building);
	}

	@SuppressWarnings("unchecked")
	public final void from(BuildPlan plan) {
		collect((T) plan.block, plan.x, plan.y, plan.rotation, plan.config);
	}
	
	@SuppressWarnings("unchecked")
	public final void from(Block block, int x, int y, int rotation, Object config) {
		collect((T) block, x, y, rotation, config);
	}

	protected void collect(T block, int x, int y, int rotation, Object config) {
		float craftSpeed = ModWork.getCraftSpeed(block, x, y, config);
		ModWork.consumeBlock(block, x, y, config, craftSpeed, items::sub, liquids::sub, power::sub, heat::sub);
		ModWork.produceBlock(block, x, y, config, craftSpeed, items::add, liquids::sub, power::add, heat::add);
	}

	protected void collect(B building) {
		// TODO: remove:
		ModWork.getCraftSpeed(building, (craftSpeed, craftSpeedMultiplier) -> {
//			ModWork.produceItems(building, craftSpeed, (item, ips) -> {
//				items.add(item, ips);
//				if(building.items != null) {
//					if(building.items.get(item) >= building.getMaximumAccepted(item)) {
//						items.warn(item);
//					}
//				}
//			});
			ModWork.produceLiquids(building, craftSpeed, (liquid, lps) -> liquids.add(liquid, lps));
			ModWork.produceHeat(building, craftSpeed, h -> heat.heat += h);

			//			
			for (int c = 0; c < building.block.consumers.length; c++) {
				var consume = building.block.consumers[c];
				ModWork.consumeItems(consume, building, craftSpeed, (item, ips) -> items.sub(item, ips));
				ModWork.consumeLiquids(consume, building, craftSpeedMultiplier, (liquid, lps) -> liquids.sub(liquid, lps));
				ModWork.consumePower(consume, building, p -> power.power -= p);
			}
			heat.heat -= ModWork.consumeHeat(building, craftSpeed);
		});
		
		produce(building);
	}

	protected void produce(B building) {
		producePower(building);
	}
	
	protected void producePower(B building) {
		power.power += building.getPowerProduction() * 60 * building.timeScale();
	}
	
	@SuppressWarnings("unchecked")
	public final T block(B buiding) {
		return (T) buiding.block;
	}
	
}
