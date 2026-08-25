package agzam4.flow.metric;

import java.util.Arrays;

import mindustry.Vars;

public class ItemsMetric extends Metric {

	private float[] balance = new float[Vars.content.items().size];
	private boolean[] overflow = new boolean[Vars.content.items().size];
	
	@Override
	public void reset() {
		if(balance.length != Vars.content.items().size) {
			balance = new float[Vars.content.items().size];
			overflow = new boolean[Vars.content.items().size];
			return;
		}
		Arrays.fill(balance, 0f);
		Arrays.fill(overflow, false);
	}
	
	
	
}