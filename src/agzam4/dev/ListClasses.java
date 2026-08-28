package agzam4.dev;

import arc.struct.*;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.core.ContentLoader;
import mindustry.world.Block;

public class ListClasses {

	
	public static void main(String[] args) {
		Vars.content = new ContentLoader();
		Liquids.load();
		Items.load();
		Blocks.load();

		ObjectSet<Class<?>> classes = ObjectSet.with();
		ObjectSet<Class<?>> interfaces = ObjectSet.with();
		
		for (Block b : Vars.content.blocks()) {
			Class<?> cls = b.getClass();
			while (true) {
				if(!cls.toString().contains("$")) classes.add(cls);
				interfaces.addAll(cls.getInterfaces());
				cls = cls.getSuperclass();
				if(cls == Object.class) break;
			}
		}

		Log.info("=== classes ===\n@", classes.toSeq().sort((s1,s2)->s1.toString().compareTo(s2.toString())).toString("\n"));
		Log.info("=== interfaces ===\n@", interfaces.toSeq().toString("\n"));
		
	}
	
}