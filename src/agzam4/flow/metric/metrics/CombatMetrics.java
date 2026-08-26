package agzam4.flow.metric.metrics;

public class CombatMetrics extends Metric {

	public static String type = "mindustry.combat";
	
	public float groundDps, airDps;
	
	@Override
	public void reset() {
		groundDps = 0;
		airDps = 0;
	}

	@Override
	public Class<? extends Metric> key() {
		return CombatMetrics.class;
	}
}
