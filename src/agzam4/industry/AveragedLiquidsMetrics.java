package agzam4.industry;

import agzam4.flow.metric.metrics.LiquidMetric;
import mindustry.Vars;
import mindustry.type.Liquid;

public class AveragedLiquidsMetrics extends LiquidMetric {

	public float[] averages = new float[0];
	public int interval = 60;
	
	private int ticks = 0;
	private boolean ready = false;
	
	public AveragedLiquidsMetrics(int interval) {
		this.interval = interval;
	}
	
	@Override
	public void reset() {
		super.reset();
		if(averages.length != Vars.content.liquids().size) {
			averages = new float[Vars.content.liquids().size];
			ready = false;
			ticks = 0;
			return;
		}
		
		if(ticks >= interval) {
			for (int i = 0; i < balance.length; i++) averages[i] = balance[i]/ticks;
			ticks = 0;
			ready = true;
			return;
		}
		ticks++;
	}
	

	public float getReal(Liquid liquid) {
		return super.get(liquid);
	}
	
	@Override
	public float get(Liquid liquid) {
		if(ready) return super.get(liquid);
		return averages[liquid.id];
	}
	
}
