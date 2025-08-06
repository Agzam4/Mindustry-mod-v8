package agzam4;

import arc.*;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.event.*;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.input.Binding;
import mindustry.mod.Mod;
import mindustry.mod.Mods.LoadedMod;
import mindustry.ui.Fonts;
import mindustry.world.Tile;
import agzam4.ModWork.KeyBinds;
import agzam4.debug.Debug;
import agzam4.events.SceneTileTap;
import agzam4.gameutils.Afk;
import agzam4.gameutils.CursorTracker;
import agzam4.gameutils.DamageNumbers;
import agzam4.gameutils.FireRange;
import agzam4.gameutils.UnitsVisibility;
import agzam4.gameutils.WaveViewer;
import agzam4.industry.IndustryCalculator;
import agzam4.render.Text;
import agzam4.render.light.LightRenderer;
import agzam4.ui.MobileUI;
import agzam4.ui.ModSettingsDialog;
import agzam4.ui.ModStyles;
import agzam4.ui.mapeditor.MapEditorDialog;
import agzam4.uiOverride.UiOverride;
import agzam4.utils.*;

public class AgzamMod extends Mod {
	
	/** Safe URL is mod.getRepo() is null **/
	public static final String repo = "Agzam4/Mindustry-mod-v8";
	public static final String name = "agzam4mod";
	
	/**
	 * TODO:
	 * [V] Pixelisation fix
	 * Messge limit
	 */

	
	int pauseRandomNum = 0;
	
	public static final int modRandom = Mathf.random(100, 999);
	
	public static LoadedMod mod;
	
	@Override
	public void init() {
		Log.info("Mod loaded rid: @", modRandom);
		mod = Vars.mods.getMod("agzam4mod");
		Afk.init();
		MyFonts.load();
		MyIndexer.init();
		
		ModStyles.init();
		
		DamageNumbers.init();
		UnitsVisibility.init();
		
		LightRenderer.init();
		
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
		
		Events.scene(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, KeyCode keyCode) {
                if (ModWork.acceptKey()) {
                	if(keyCode.equals(KeyBinds.hideUnits.key)) {
                		UnitsVisibility.toggle();
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
			if(Vars.world != null && Core.input.keyTap(Binding.select) && !Core.scene.hasMouse()){
				Tile selected = Vars.world.tileWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());
				if(selected != null){
					arc.Events.fire(new SceneTileTap(selected));
				}
			}
			
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
			
			
//			Log.info("update: @", modRandom);
		});
		Events.run(Trigger.preDraw, () -> {
			PlayerAI.preDraw();
		});

		Events.run(Trigger.uiDrawBegin, () -> {
			DamageNumbers.drawUi();
			CursorTracker.draw();
			WaveViewer.drawUi();
			IndustryCalculator.drawUi();
			Text.font(Fonts.outline);
			Text.size();
			Text.font(Fonts.def);
			Text.size();
		});
		
		Events.run(Trigger.drawOver, () -> {
//			Log.info("rand: @", modRandom);
			FireRange.draw();
			ProcessorGenerator.draw();
			UnitSpawner.draw();
			IndustryCalculator.draw();
			UnitsVisibility.draw();
			
			WaveViewer.draw(); // FIXME
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
		CursorTracker.dispose();
		DamageNumbers.dispose();
		UnitsVisibility.dispose();
		Events.clear();
		LightRenderer.unapply();
		KeyBinds.dispose();
	}

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
