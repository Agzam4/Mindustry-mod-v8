package agzam4;

import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType.PlayerChatEvent;
import mindustry.gen.Call;

public class Afk {

	public static boolean autoAI = false;
	
	private static boolean isPaused = false;
	private static long pauseStartTime = System.nanoTime();
	
	private static String pingText = "@Agzam 000"; // Random numbers system for antispam

	public static boolean afkAvalible;

	public static Seq<String> names = new Seq<String>();

	public static void init() {
		autoAI = ModWork.setting("afk.auto-afk-mode");
		names(null);
		Events.on(PlayerChatEvent.class, e -> {
			if(!afkAvalible) return;
			if(!Awt.avalible) return;
			if(!ModWork.setting("afk-ping")) return;
			if(e.message == null) return;	
			if(!isPaused) return;
			if(e.player == null) return;
			if(Vars.player == null) return;
			if(e.player.plainName().equals(Vars.player.plainName())) return;

			String stripName = baseName();
			String ruName = ruName();
			
			long afk = timeSec();
			if(afk >= 10) {
				final String msg = Strings.stripColors(e.message).toLowerCase();
				if(msg.startsWith(pingText)) {
					createPingText(stripName);
					if(Awt.message(Strings.stripColors(e.player.name()) + ": " + msg.substring(pingText.length()))) {
				        Call.sendChatMessage("[lightgray]" + ModWork.bungle("afk.message-send"));
					} else {
				        Call.sendChatMessage("[lightgray]" + ModWork.bungle("afk.message-not-send"));
					}
					return;
				}
				if(msg.contains(ruName) || msg.contains(stripName.toLowerCase()) || names.contains(s -> msg.contains(s.toLowerCase()))) {
					createPingText(stripName);
					Awt.beep();
					String time = Mathf.floor(afk/60) + " " + ModWork.bungle("afk.min");
					if(afk < 60) {
						time = afk + " " + ModWork.bungle("afk.sec");
					}
					String text = "[lightgray]" + Afk.getCustomAfk()
							.replaceAll("@name", Strings.stripColors(stripName))
							.replaceAll("@time", time + "");
					if(text.indexOf("@pingText") == -1) {
						text += ModWork.bungle("afk.automessage-end").replaceFirst("@pingText", pingText);
					} else {
						text = text.replaceAll("@pingText", pingText);
					}
					Call.sendChatMessage(text); 
				}
			}
		});		
		
		Core.app.addListener(new ApplicationListener() {

			@Override
			public void pause() {
				isPaused = true;
				pauseStartTime = Time.nanos();
			}
			
			@Override
			public void resume() {
				isPaused = false;
			}
			
		});
	}

	public static String baseName() {
		return ModWork.strip(Vars.player.name).replaceAll(" ", "_");
	}

	public static String ruName() {
		return ModWork.toRus(baseName());
	}

	private static void createPingText(String stripName) {
		pingText = ("@" + stripName + " " + Mathf.random(100, 999)).toLowerCase();
	}
	
	public static String getCustomAfk() {
		String def = ModWork.bungle("afk.automessage");
		String str = Core.settings.getString("agzam4mod.afk-start", def);
		if(str.isEmpty()) return def;
		return str;
	}

	private static long timeSec() {
		long afk = System.nanoTime()-pauseStartTime;
		return afk / Time.nanosPerMilli / 1000;
	}

	public static String names() {
		return names.toString("\n");
	}

	public static void names(@Nullable String nms) {
		if(nms == null) {
			nms = ModWork.settingDef("afk-names", "");
			Log.info("loaded: @", nms);
		} else {
			Log.info("saved: @", nms);
			ModWork.setting("afk-names", nms);
		}
		names.clear();
		for (String name : nms.split("\n")) {
			if(name.isEmpty()) continue;
			names.add(name);
		}
	}
	
	public static boolean isAfk() {
		return isPaused;
	}
}
