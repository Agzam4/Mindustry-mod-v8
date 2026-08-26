package agzam4.flow.metric.metrics;

public abstract class Metric {

	public abstract Class<? extends Metric> key();
	
	public abstract void reset();
	
}
