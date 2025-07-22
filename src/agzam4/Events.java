package agzam4;

import arc.Core;
import arc.func.Cons;
import arc.scene.event.EventListener;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.game.EventType.Trigger;

public class Events {

    private static final ObjectMap<Object, Seq<Cons<?>>> events = new ObjectMap<>();
    private static final Seq<EventListener> sceneListeners = new Seq<>();

    public static <T> void on(Class<T> type, Cons<T> listener){
    	arc.Events.on(type, listener);
        events.get(type, () -> new Seq<>(Cons.class)).add(listener);
    }

	public static void run(Trigger type, Runnable listener) {
		Log.info("added: @ (@)", type, type.hashCode());

        ObjectMap<Object, Seq<Cons<?>>> superEvents = Reflect.get(arc.Events.class, null, "events");

		Cons<?> cons = e -> listener.run();
		
		superEvents.get(type, () -> new Seq<>(Cons.class)).add(cons);
        events.get(type, () -> new Seq<>(Cons.class)).add(cons);
	}
    
    /**
     * Clears all mod events
     */
    public static void clear() {
        ObjectMap<Object, Seq<Cons<?>>> all = Reflect.get(arc.Events.class, null, "events");
//    	events.each((type, list) -> list.each(event -> all.get(type).remove(event)));
    	events.each((type, list) -> {
    		Log.info("Removing: @ (@)", type, type.hashCode());
    		list.each(event -> all.get(type).remove(event));
    	});
    	sceneListeners.each(l -> Core.scene.removeListener(l));
	}

	public static void scene(EventListener listener) {
		sceneListeners.add(listener);
		Core.scene.addListener(listener);
	}

}
