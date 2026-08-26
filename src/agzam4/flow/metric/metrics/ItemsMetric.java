package agzam4.flow.metric.metrics;

import java.util.Arrays;

import mindustry.Vars;
import mindustry.type.Item;

public class ItemsMetric extends Metric {

	public float[] balance = new float[0];
	public boolean[] overflow = new boolean[0];
	
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

	public void add(Item item, float ips) {
		balance[item.id] += ips;
	}
	
	public void sub(Item item, float ips) {
		balance[item.id] -= ips;
	}

	public void warn(Item item) {
		overflow[item.id] = true;		
	}

	public int size() {
		return balance.length;
	}

	public float get(Item item) {
		return balance[item.id];
	}

	@Override
	public Class<? extends Metric> key() {
		return ItemsMetric.class;
	}
	
}