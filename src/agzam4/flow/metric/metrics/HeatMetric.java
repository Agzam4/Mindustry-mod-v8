package agzam4.flow.metric.metrics;

public class HeatMetric extends Metric {

	public static String type = "mindustry.heat";
	
	public float heat;
	
	@Override
	public void reset() {
		heat = 0;
	}

	public void add(float heat) {
		this.heat += heat;
	}

	public void sub(float heat) {
		this.heat -= heat;
	}

	@Override
	public Class<? extends Metric> key() {
		return HeatMetric.class;
	}
	
	
}