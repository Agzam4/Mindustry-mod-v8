package agzam4;

import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Reflect;
import mindustry.game.EventType.Trigger;

public class Events {

    private static final ObjectMap<Object, Seq<Cons<?>>> events = new ObjectMap<>();

    public static <T> void on(Class<T> type, Cons<T> listener){
    	arc.Events.on(type, listener);
        events.get(type, () -> new Seq<>(Cons.class)).add(listener);
    }

	public static void run(Object type, Runnable listener) {
		arc.Events.run(type, listener);
        events.get(type, () -> new Seq<>(Cons.class)).add(e -> listener.run());
	}
    
    /**
     * Clears all mod events
     */
    public static void clear() {
        ObjectMap<Object, Seq<Cons<?>>> all = Reflect.get(arc.Events.class, null, "events");
    	events.each((type, list) -> list.each(event -> all.get(type).remove(event)));
	}

}
