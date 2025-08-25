package agzam4;

import arc.Core;
import arc.func.Cons;
import arc.scene.event.EventListener;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Reflect;
import mindustry.game.EventType.Trigger;

public class Events {

    private static final ObjectMap<Object, Seq<Cons<?>>> events = new ObjectMap<>();
    private static final Seq<EventListener> sceneListeners = new Seq<>();
    
    public static <T> void on(Class<T> type, Cons<T> listener){
    	arc.Events.on(type, listener);
        
        events.get(type, () -> new Seq<>(Cons.class)).add(listener);
    }
    
    /**
     * Try to detect wrong class of Events.events
     * @return null if class wrong or Events.events if all OK
     * @see Issue #16
     */
    private static @Nullable ObjectMap<Object, Seq<Cons<?>>> getSafe() {
    	 try {
    	    ObjectMap<Object, Seq<Cons<?>>> superEvents = Reflect.get(arc.Events.class, null, "events");
         	Cons<?> cons = null;
         	for (var list : superEvents.values()) {
 				for (var c : list) {
 					cons = c;
 				}
 			}
         	if(cons == null) {
         		Log.err("Cant detect type of Events.events");
         		return null;
         	}
         	return superEvents;
 		} catch (Exception e) {}
  		return null;
	}

	public static void run(Trigger type, Runnable listener) {
		var superEvents = getSafe();
		if(superEvents == null) return;
		
		Cons<?> cons = e -> listener.run();
		superEvents.get(type, () -> new Seq<>(Cons.class)).add(cons);
        events.get(type, () -> new Seq<>(Cons.class)).add(cons);
	}
    
    /**
     * Clears all mod events
     */
    public static void clear() {
		var all = getSafe();
		if(all == null) return;
    	events.each((type, list) -> {
    		list.each(event -> all.get(type).remove(event));
    	});
    	sceneListeners.each(l -> Core.scene.removeListener(l));
	}

	public static void scene(EventListener listener) {
		sceneListeners.add(listener);
		Core.scene.addListener(listener);
	}

}
