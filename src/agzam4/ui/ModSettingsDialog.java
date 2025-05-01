package agzam4.ui;

import static arc.Core.bundle;
import static arc.Core.settings;
import static mindustry.Vars.content;

import agzam4.AgzamMod;
import agzam4.Awt;
import agzam4.ClientPathfinder;
import agzam4.ModWork;
import agzam4.UpdateInfo;
import agzam4.ModWork.KeyBinds;
import agzam4.uiOverride.CustomChatFragment;
import agzam4.uiOverride.UiOverride;
import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.ui.Button;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.content.Planets;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Planet;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.CustomRulesDialog;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.CheckSetting;
import mindustry.world.meta.BuildVisibility;

public class ModSettingsDialog extends Table {

	static Cell<TextButton> unlockContent = null;
	static Cell<TextButton> unlockBlocks = null;
	
	public static Cons<SettingsTable> builder = settingsTable -> {
		boolean needUpdate = UpdateInfo.needUpdate();
		
		settingsTable.defaults().left();
		Table table = new Table();
		if(needUpdate) {
			table.add(ModWork.bungle("need-update")).color(Color.red).colspan(4).pad(10).padBottom(4).row();
			table.button("@mods.browser.reinstall", Icon.download, () -> UpdateInfo.githubImportMod(AgzamMod.mod.getRepo(), null))
			.fillX().pad(6).colspan(4).padTop(0).padBottom(10).row();
		}
		
		addCategory(table, "unlock");

		unlockContent = table.button(ModWork.bungle("settings.unlock-content"), Icon.lockOpen, Styles.defaultt, () -> {
			unlockDatabaseContent();
			if(unlockContent != null) unlockContent.disabled(true);
		}).growX().pad(10).padBottom(4);
		table.row();
		
		unlockBlocks = table.button(ModWork.bungle("settings.unlock-blocks"), Icon.lockOpen, Styles.defaultt, () -> {
			unlockBlocksContent();
			if(unlockBlocks != null) unlockBlocks.disabled(true);
		}).growX().pad(10).padBottom(4);
		table.row();
		
//		unlockContent = table.button(ModWork.bungle("settings.unlock-content"), Icon.lockOpen, Styles.defaultt, () -> {
//			unlockDatabaseContent();
//			unlockContent.disabled(true);
//		}).growX().pad(10).padBottom(4);
//		
//		unlockBlocks = table.button(ModWork.bungle("settings.unlock-blocks"), Icon.lockOpen, Styles.defaultt, () -> {
//			unlockBlocksContent();
//			unlockBlocks.disabled(true);
//		}).growX().pad(10).padBottom(4);
//		table.row();

		addCategory(table, "cursors");
        addCheck(table, "cursors-tracking");

		addCategory(table, "units-and-buildings");
        addCheck(table, "show-turrets-range");
        addCheck(table, "show-build-health");
        addCheck(table, "show-units-health");
        addCheck(table, "wave-viewer");
//		addCheck(table, "enemies-paths", false, b -> ClientPathfinder.enabled = b);
//		addKeyBind(table, KeyBinds.hideUnits);
//		addKeyBind(table, KeyBinds.slowMovement);

		addCategory(table, "calculations");
		addCheck(table, "show-blocks-tooltip");
		addCheck(table, "selection-calculations");
		addCheck(table, "buildplans-calculations");
		
//		addKeyBind(table, KeyBinds.selection);
//		addKeyBind(table, KeyBinds.clearSelection);
		
		addCategory(table, "afk");
		
		try {
			AgzamMod.afkAvalible = true;
			if(Awt.avalible && !Vars.mobile) {
				table.field(AgzamMod.getCustomAfk(), t -> {
					Core.settings.put("agzam4mod.afk-start", t);
				}).tooltip(ModWork.bungle("afk.automessage-start-tooltip")).width(Core.scene.getWidth()/2f).row();
			} else {
				AgzamMod.afkAvalible = false;
		        table.add(ModWork.bungle("afk-err")).color(Color.red).colspan(4).pad(10).padBottom(4).row();
			}
		} catch (Throwable e) {
			AgzamMod.afkAvalible = false;
	        table.add(ModWork.bungle("afk-err")).color(Color.red).colspan(4).pad(10).padBottom(4).row();
		}
		
        settingsTable.add(table);
        settingsTable.row();
//
		settingsTable.name = ModWork.bungle("settings.name");
		settingsTable.visible = true;
		
		addCategory(table, "utils");
//		addKeyBind(table, KeyBinds.openUtils);
		
		addCategory(table, "custom-ui");
		addCheck(table, "custom-chat-fragment", b -> UiOverride.set());
		addCheck(table, "outline-chat", b -> CustomChatFragment.font = b ? Fonts.outline : Fonts.def);
		

		addCategory(table, "report-bugs");
		table.button(Iconc.github + " Github", Styles.defaultt, () -> {
            if(!Core.app.openURI("https://github.com/Agzam4")){
                Vars.ui.showErrorMessage("@linkfail");
                Core.app.setClipboardText("https://github.com/Agzam4");
            }
		}).growX().pad(20).padBottom(4);
		table.row();	
		table.button(Iconc.play + " YouTube", Styles.defaultt, () -> {
            if(!Core.app.openURI("https://www.youtube.com/@agzam4/")){
            	Vars.ui.showErrorMessage("@linkfail");
                Core.app.setClipboardText("https://www.youtube.com/@agzam4/");
            }
		}).growX().pad(20).padBottom(4);
		table.row();
		
//		table.table(Tex.button, t -> {
//            t.margin(10f);
//            ButtonGroup<Button> group = new ButtonGroup<>();
//
////            t.defaults().size(140f, 50f);
//
//    		t.button(Iconc.github + " Github", Styles.defaultt, () -> {
//                if(!Core.app.openURI("https://github.com/Agzam4")){
//                    Vars.ui.showErrorMessage("@linkfail");
//                    Core.app.setClipboardText("https://github.com/Agzam4");
//                }
//    		})
//    		.fill(.5f, 1f).pad(0).padRight(10f)
//    		.group(group);
//    		
//    		t.button(Iconc.play + " YouTube", Styles.defaultt, () -> {
//                if(!Core.app.openURI("https://www.youtube.com/@agzam4/")){
//                	Vars.ui.showErrorMessage("@linkfail");
//                    Core.app.setClipboardText("https://www.youtube.com/@agzam4/");
//                }
//    		})
//    		.fill(.5f, 1f).pad(0)
//    		.group(group);
//    		t.row();
//
//        }).left().fillX().expand(false, false).row();
		
	};

	private static void addCheck(Table table, String settings) {
		addCheck(table, settings, null);
	}

	private static void addCheck(Table table, String settings, Cons<Boolean> listener) {
		addCheck(table, settings, true, listener);
	}
	
	private static void addCheck(Table table, String settings, boolean def, Cons<Boolean> listener) {
		table.check(ModWork.bungle("settings." + settings), ModWork.settingDef(settings, def), b -> {
			ModWork.setting(settings, b);
			if(listener != null) listener.get(b);
		}).colspan(4).pad(10).padBottom(4).tooltip(ModWork.bungle("settings-tooltip." + settings)).left().row();
	}
	
	private static void addCategory(Table table, String category) {
//        table.add(ModWork.bungle("category." + category)).color(Color.gray).colspan(4).pad(10).padBottom(4).row();
//		table.image().color(Pal.accent).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).row();		
		table.add(ModWork.bungle("category." + category)).color(Pal.accent).colspan(4).pad(10).padBottom(4).row();
	    table.image().color(Pal.accent).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).row();
	}
	
	private static void unlockDatabaseContent() {
		Vars.content.units().each(u -> u.hidden = false);
		Vars.content.items().each(i -> i.hidden = false);
		Vars.content.liquids().each(l -> l.hidden = false);		
	}
	
	private static void unlockBlocksContent() {
		Vars.content.blocks().each(b -> {
			b.buildVisibility = BuildVisibility.shown;
		});
	}
	
}
