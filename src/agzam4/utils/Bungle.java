package agzam4.utils;

import arc.Core;

public class Bungle {

	public static String core(String string) {
		return Core.bundle.get(string, "[red]??<core>." + string + "??[]");
	}
	
	public static String get(String string) {
		return Core.bundle.get("agzam4mod." + string, "[red]??" + string + "??[]");
	}

	public static String afk(String string) {
		return Core.bundle.get("agzam4mod.afk." + string, "[red]??afk." + string + "??[]");
	}

	public static String category(String string) {
		return Core.bundle.get("agzam4mod.category." + string, "[red]??category." + string + "??[]");
	}
	
	public static String settings(String string) {
		return Core.bundle.get("agzam4mod.settings." + string, "[red]??settings." + string + "??[]");
	}

	public static String settingsTooltip(String string) {
		return Core.bundle.get("agzam4mod.settings-tooltip." + string, "[red]??settings-tooltip." + string + "??[]");
	}

	public static String mobile(String string) {
		return Core.bundle.get("agzam4mod.mobile." + string, "[red]??mobile." + string + "??[]");
	}

	public static String dialog(String string) {
		return Core.bundle.get("agzam4mod.dialog." + string, "[red]??dialog." + string + "??[]");
	}

	public static String calculator(String string) {
		return Core.bundle.get("agzam4mod.calculator." + string, "[red]??calculator." + string + "??[]");
	}
	
	
}
