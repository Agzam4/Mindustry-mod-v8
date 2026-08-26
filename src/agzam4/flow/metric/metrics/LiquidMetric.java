package agzam4.flow.metric.metrics;

import java.util.Arrays;

import mindustry.Vars;
import mindustry.type.Liquid;

public class LiquidMetric extends Metric {
	
	public float[] balance = new float[Vars.content.liquids().size];
	
	@Override
	public void reset() {
		if(balance.length != Vars.content.liquids().size) {
			balance = new float[Vars.content.liquids().size];
			return;
		}
		Arrays.fill(balance, 0f);
	}

	public void add(Liquid item, float ips) {
		balance[item.id] += ips;
	}

	public int size() {
		return balance.length;
	}

	public void sub(Liquid liquid, float lps) {
		balance[liquid.id] -= lps;
	}

	public float get(Liquid liquid) {
		return balance[liquid.id];
	}

	@Override
	public Class<? extends Metric> key() {
		return LiquidMetric.class;
	}
	
}