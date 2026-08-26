package agzam4.industry;

import agzam4.flow.metric.metrics.ItemsMetric;
import mindustry.Vars;
import mindustry.type.Item;

public class AveragedItemsMetrics extends ItemsMetric {

	public float[] averages = new float[0];
	public int interval = 60;
	
	private int ticks = 0;
	private boolean ready = false;
	
	public AveragedItemsMetrics(int interval) {
		this.interval = interval;
	}
	
	@Override
	public void reset() {
		super.reset();
		if(averages.length != Vars.content.items().size) {
			averages = new float[Vars.content.items().size];
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
	

	public float getReal(Item item) {
		return super.get(item);
	}
	
	@Override
	public float get(Item item) {
		if(ready) return super.get(item);
		return averages[item.id];
	}
	
}
