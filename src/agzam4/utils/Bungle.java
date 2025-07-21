package agzam4.utils;

import arc.Core;

public class Bungle {

	public static String get(String string) {
		return Core.bundle.get("agzam4mod." + string, "[red]??" + string + "??[]");
	}

	public static String afk(String string) {
		return Core.bundle.get("agzam4mod.afk" + string, "[red]??afk." + string + "??[]");
	}

	public static String core(String string) {
		return Core.bundle.get(string, "[red]??<core>." + string + "??[]");
	}
	
}
