package agzam4.utils;

import agzam4.AgzamMod;
import arc.Core;

public class Prefs {

	public static final Prefs settings = new Prefs("settings");
	
	private final String prefix;
	
	private Prefs(String prefix) {
		this.prefix = AgzamMod.name + "." + prefix + ".";
	}
	
	
	public boolean bool(String string) {
		return Core.settings.getBool(prefix + string, true);
	}

	public boolean bool(String string, boolean def) {
		return Core.settings.getBool(prefix + string, def);
	}

	public String string(String string, String def) {
		return Core.settings.getString(prefix + string, def);
	}

	public void put(String string, String value) {
		Core.settings.put(prefix + string, value);
	}
}
