package agzam4.flow.metric.collectors;

import agzam4.flow.metric.Metrics;
import mindustry.world.Block;

public interface Collector<B extends Block,T,P> {

	public void init(Metrics metrics);
	
	@SuppressWarnings("unchecked")
	public default void collectRaw(Block block, Object object, P payload) {
		collect((B)block, (T) object, payload);
	}
	
	public void collect(B block, T t, P payload);

	
}
