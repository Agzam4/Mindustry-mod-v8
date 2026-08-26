package agzam4.flow.metric.metrics;

public class PowerMetrics extends Metric {

	public float power;
	
	@Override
	public void reset() {
		power = 0;
	}

	public void add(float power) {
		this.power += power;
	}

	public void sub(float power) {
		this.power -= power;
	}

	@Override
	public Class<? extends Metric> key() {
		return PowerMetrics.class;
	}
	
}
