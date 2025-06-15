package agzam4;

import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.Dialog;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType.ClientServerConnectEvent;
import mindustry.game.EventType.PlayerChatEvent;
import mindustry.game.EventType.TapEvent;
import mindustry.game.EventType.Trigger;
import mindustry.game.EventType.UnitDamageEvent;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.mod.Mods.LoadedMod;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.KeybindDialog;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.CheckSetting;
import mindustry.world.meta.BuildVisibility;

import agzam4.ModWork.KeyBinds;
import agzam4.debug.Debug;
import agzam4.industry.IndustryCalculator;
import agzam4.ui.ModSettingsDialog;
import agzam4.uiOverride.UiOverride;
import agzam4.utils.PlayerAI;
import agzam4.utils.PlayerUtils;
import agzam4.utils.ProcessorGenerator;
import agzam4.utils.UnitSpawner;

public class AgzamMod extends Mod {
	
	
	/**
	 * TODO:
	 * [V] Range only for enemies team fix
	 * [V] Hide unit spawn on servers
	 * [V] Words list for ping
	 * [V] Auto-enable AFK mode
	 * [V] Colored text
	 * Pixelisation fix
	 */

	public static boolean hideUnits;
	private static UnitTextures[] unitTextures;
	private static TextureRegion minelaser, minelaserEnd;
	private Cell<TextButton> unlockContent = null, unlockBlocks = null;
	
	int pauseRandomNum = 0;
	
	private boolean debug = false; // FIXME
	
	public static long updates = 0;
	
	public static LoadedMod mod;
	
	@Override
	public void init() {
//		Vars.content.unitsclone()
//		Vars.content.getByName(ContentType.unit, "anthicus-missile").
//		Vars.content.getByName(ContentType.unit, "anthicus-missile").playerControllable = true
//		Vars.state.rules.
//		Team.
//		Vars.netServer.admins.kickedIPs
//		Vars.player.unit().cap();
		mod = Vars.mods.getMod("agzam4mod");
		Afk.init();
		ClientPathfinder.init();
		MyFonts.load();
		MyIndexer.init();
		
		try {
			UiOverride.init();
			Debug.init();
			CursorTracker.init();
			
			Vars.content.items().each(b -> {
				if(!b.hasEmoji()) {
					MyFonts.createEmoji(b.uiIcon, b.name);
				}
			});
			Vars.content.blocks().each(b -> {
				if(!b.hasEmoji()) {
					MyFonts.createEmoji(b.uiIcon, b.name);
//					Vars.ui.hudfrag;
				}
			});
			IndustryCalculator.init();
			WaveViewer.init();
			PlayerUtils.build();
		try {
			try {
				Awt.avalible = Awt.avalible();
			} catch (Error e) {

			} 
		} catch (Throwable e) {
		}
		
		minelaser = Core.atlas.find("minelaser");
		minelaserEnd = Core.atlas.find("minelaser-end");
		unitTextures = new UnitTextures[Vars.content.units().size];
		for (int i = 0; i < unitTextures.length; i++) {
			unitTextures[i] = new UnitTextures(Vars.content.unit(i));
		}
		
		Core.scene.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, KeyCode keyCode) {
                if (ModWork.acceptKey()) {
                	if(keyCode.equals(KeyBinds.hideUnits.key)) {
                		hideUnits(!hideUnits);
                	}
                	if(keyCode.equals(KeyBinds.openUtils.key)) {
                		PlayerUtils.show();
                	}
                }
                return super.keyDown(event, keyCode);
            }
        });

		boolean needUpdate = UpdateInfo.needUpdate();
		
//		Cons<SettingsTable> builder = settingsTable -> {};
		
		if(needUpdate) {
			Vars.ui.settings.addCategory(ModWork.bungle("settings.name") + " [red]" + Iconc.warning, Icon.wrench, ModSettingsDialog.builder);
		} else {
			Vars.ui.settings.addCategory(ModWork.bungle("settings.name"), Icon.wrench, ModSettingsDialog.builder);
		}

		Events.on(TapEvent.class, e -> {
			if(e == null) return;
			if(e.player == null) return;
			if(e.tile == null) return;
//			EnemiesPaths.tap(e);
		});
		
		Events.run(Trigger.update, () -> {
			updates++;
			IndustryCalculator.update();
			PlayerAI.updatePlayer();
			UnitSpawner.update();
//			EnemiesPaths.update();
//			DamageNumbers.update();
			if(Vars.player.unit() != null) {
				if(Core.input.keyDown(KeyBinds.slowMovement.key)) {
					Vars.player.unit().vel.scl(.5f);
				}
				if(lockUnit) {
					Vars.player.unit().vel.scl(0);
				}
			}
		});
		Events.run(Trigger.preDraw, () -> {
			PlayerAI.preDraw();
		});
		
		Events.run(Trigger.drawOver, () -> {
			CursorTracker.draw();
			DamageNumbers.draw();
			FireRange.draw();
			IndustryCalculator.draw();
			ProcessorGenerator.draw();
			UnitSpawner.draw();
			WaveViewer.draw();
//			EnemiesPaths.draw();
			Draw.reset();
		});
		
		Events.on(UnitDamageEvent.class, e -> {
			DamageNumbers.unitDamageEvent(e);
		});
		
		
		
		// Check if player in net game to save traffic and don't get err
		Events.on(ClientServerConnectEvent.class, e -> { 
			if(!UpdateInfo.isCurrentSessionChecked) {
				UpdateInfo.check();
			}
		});

		// mobile OK
		
		if(debug) {
			MobileUI.build();
		}
		
		if(Vars.mobile) {
			MobileUI.build();
		} else {
				
		}
			

		if(true) return;
			
		} catch (Throwable e) {
			Log.err(e);
			if(true) return;
		}
	}
	

	@Deprecated
	private void addKeyBind(Table table, final KeyBinds keybind) {
//        Table hotkeyTable = new Table();
//        hotkeyTable.add().height(10);
//        hotkeyTable.row();
//        hotkeyTable.add(ModWork.bungle("settings.keybinds." + keybind.keybind), Color.white).left().padRight(40).padLeft(8);
//        hotkeyTable.label(() -> keybind.key.toString()).color(Pal.accent).left().minWidth(90).padRight(20);
//        hotkeyTable.button("@settings.rebind", Styles.defaultt, () -> {
//        	if(ModWork.hasKeyBoard()) {
//            	openDialog(keybind);
//        	}
//        }).width(130f);
//        hotkeyTable.button("@settings.resetKey", Styles.defaultt, () -> {
//        	keybind.key = keybind.def;
//        	keybind.put();
//        }).width(130f).pad(2f).padLeft(4f);
//        hotkeyTable.row();
//        table.add(hotkeyTable);
//        table.row();		
	}

	private void addCategory(Table table, String category) {
        table.add(ModWork.bungle("category." + category)).color(Pal.accent).colspan(4).pad(10).padBottom(4).row();
		table.image().color(Pal.accent).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).row();		
	}

//	private void addCheck(Table table, String settings) {
//		addCheck(table, settings, null);
//	}
//
//	private void addCheck(Table table, String settings, Cons<Boolean> listener) {
//		addCheck(table, settings, true, listener);
//	}
	

//	private float megaAccel, megaDragg, megaSpeed;

//	private void comfortMega(boolean b) {
//		comfortMega = b;
//		Core.settings.put("agzam4mod-units.settings.comfortMega", b);
//		Core.settings.saveValues();
//		if(comfortMega) {
//			mega.accel = emanate.accel;
//			mega.drag = emanate.drag;
////			mega.speed = 3;
//		} else {
//			mega.accel = megaAccel;
//			mega.drag = megaDragg;
////			mega.speed = megaSpeed;
//		}
//	}
	
	public static void hideUnits(boolean b) {
		hideUnits = b;
		if(b) {
			for (int i = 0; i < unitTextures.length; i++) {
				unitTextures[i].hideTextures();
				unitTextures[i].hideEngines();
			}
			Core.atlas.addRegion("minelaser", UnitTextures.none);
			Core.atlas.addRegion("minelaser-end", UnitTextures.none);
		} else {
			for (int i = 0; i < unitTextures.length; i++) {
				unitTextures[i].returnTextures();
				unitTextures[i].returnEngines();
			}
			Core.atlas.addRegion("minelaser", minelaser);
			Core.atlas.addRegion("minelaser-end", minelaserEnd);
		}
	}

//	Section section = Core.keybinds.getSections()[0];
//	private void openDialog(final KeyBinds keybind) {
//		Dialog keybindDialog = new Dialog(Core.bundle.get("keybind.press"));
//
//		keybindDialog.titleTable.getCells().first().pad(4);
//			
//        if(section.device.type() == DeviceType.keyboard){
//
//        	keybindDialog.addListener(new InputListener(){
//                @Override
//                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
//                    if(Core.app.isAndroid()) return false;
//                    rebind(keybindDialog, keybind, button);
//                    return false;
//                }
//
//                @Override
//                public boolean keyDown(InputEvent event, KeyCode button){
//                	keybindDialog.hide();
//                    if(button == KeyCode.escape) return false;
//                    rebind(keybindDialog, keybind, button);
//                    return false;
//                }
//
//                @Override
//                public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
//                    keybindDialog.hide();
//                    rebind(keybindDialog, keybind, KeyCode.scroll);
//                    return false;
//                }
//            });
//        }
//
//        keybindDialog.show();
//        Time.runTask(1f, () -> keybindDialog.getScene().setScrollFocus(keybindDialog));
//    }
//	
	void rebind(Dialog rebindDialog, KeyBinds keyBinds, KeyCode newKey){
        rebindDialog.hide();
        keyBinds.key = newKey;
        keyBinds.put();
    }

	//  Core.settings.put("agzam4mod-units.settings.hideUnitsHotkey", new java.lang.Integer(75))
	// Core.settings.getInt("agzam4mod-units.settings.hideUnitsHotkey", KeyCode.h.ordinal())

	static boolean lockUnit = false;
	
	public static void lockUnit(boolean b) {
		lockUnit = b;
	}

	public static TextureRegion sprite(String name) {
		return Core.atlas.find("agzam4mod-" + name);
	}
	
	public static TextureRegion sprite(String name, int scale) {
		AtlasRegion a = Core.atlas.find("agzam4mod-" + name);
		a.texture.setFilter(TextureFilter.mipMapLinearLinear);
		return a;
	}
}
