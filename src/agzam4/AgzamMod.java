package agzam4;

import arc.*;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.Mod;
import mindustry.mod.Mods.LoadedMod;
import agzam4.ModWork.KeyBinds;
import agzam4.debug.Debug;
import agzam4.industry.IndustryCalculator;
import agzam4.ui.MobileUI;
import agzam4.ui.ModSettingsDialog;
import agzam4.ui.mapeditor.MapEditorDialog;
import agzam4.uiOverride.UiOverride;
import agzam4.utils.*;

public class AgzamMod extends Mod {
	
	/** Safe URL is mod.getRepo() is null **/
	public static final String repo = "Agzam4/Mindustry-mod-v8";
	
	/**
	 * TODO:
	 * Pixelisation fix
	 * Messge limit
	 */

	public static boolean hideUnits;
	private static UnitTextures[] unitTextures;
	private static TextureRegion minelaser, minelaserEnd;
	
	int pauseRandomNum = 0;
	
	
	public static LoadedMod mod;
	
	@Override
	public void init() {
		mod = Vars.mods.getMod("agzam4mod");
		Afk.init();
		MyFonts.load();
		MyIndexer.init();
		
		try {
			UiOverride.init();
			Debug.init();
			CursorTracker.init();
			
			Vars.content.items().each(b -> {
				if(b.hasEmoji()) return;
				MyFonts.createEmoji(b.uiIcon, b.name);
			});
			Vars.content.blocks().each(b -> {
				if(b.hasEmoji()) return;
				MyFonts.createEmoji(b.uiIcon, b.name);
			});
			IndustryCalculator.init();
			WaveViewer.init();
			PlayerUtils.build();
		try {
			try {
				Awt.avalible = Awt.avalible();
			} catch (Error e) {} 
		} catch (Throwable e) {}
		
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

		ModSettingsDialog.updateCategory();
		
		if(Debug.debug) {
			Vars.ui.settings.addCategory("TEST EDITOR", Icon.wrench, t -> {
				t.button("TEST BUTTOM", () -> {
//					MapEditor.instance.beginEdit(100,100);
//					Vars.ui.editor.beginEditMap(getConfig());
					MapEditorDialog.instance.beginEdit(100, 100);
				});
			});
		}

		Events.on(TapEvent.class, e -> {
			if(e == null) return;
			if(e.player == null) return;
			if(e.tile == null) return;
//			EnemiesPaths.tap(e);
		});
		
		Events.run(Trigger.update, () -> {
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
				UpdateInfo.check((old, now) -> {});
			}
		});

		// mobile OK
		
		MobileUI.init();
		
		if(true) return;
			
		} catch (Throwable e) {
			Log.err(e);
			if(true) return;
		}
	}
	
	public void dispose() {
		ModSettingsDialog.clearCategory();
		Vars.ui.settings.getCategories().removeAll(c -> c.name.equals("TEST EDITOR"));
		MobileUI.remove();
		Events.clear();
		CursorTracker.dispose();
	}

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

	public static String getRepo() {
		String r = mod.getRepo();
		if(r == null) return repo;
		return r;
	}
	
}
