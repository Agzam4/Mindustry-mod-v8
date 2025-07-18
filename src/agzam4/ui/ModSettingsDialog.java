package agzam4.ui;

import agzam4.Afk;
import agzam4.AgzamMod;
import agzam4.Awt;
import agzam4.ModWork;
import agzam4.UpdateInfo;
import agzam4.uiOverride.CustomChatFragment;
import agzam4.uiOverride.UiOverride;
import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.TextArea;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.ColorPicker;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable;
import mindustry.world.meta.BuildVisibility;

public class ModSettingsDialog extends Table {

	static Cell<TextButton> unlockContent = null;
	static Cell<TextButton> unlockBlocks = null;
	static Cell<TextButton> chatColor = null;
	
	static TextureRegion colorBox = AgzamMod.sprite("color-box");

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
			Afk.afkAvalible = true;
			if(Awt.avalible && !Vars.mobile) {
				table.field(Afk.getCustomAfk(), t -> {
					Core.settings.put("agzam4mod.afk-start", t);
				}).tooltip(ModWork.bungle("afk.automessage-start-tooltip")).width(Core.scene.getWidth()/2f).row();
				addCheck(table, "afk.afk-ping");
				addCheck(table, "afk.auto-afk-mode", b -> Afk.autoAI = b);
				table.labelWrap(() -> Strings.format(ModWork.bungle("afk.default-names"), Afk.baseName(), Afk.ruName())).growX().colspan(4).pad(10).padBottom(4).row();

				table.labelWrap(() -> Strings.format(ModWork.bungle("afk.custom-names"), Afk.baseName(), Afk.ruName())).growX().colspan(4).pad(10).padBottom(4).row();
		        table.area(Afk.names(), s -> Afk.names(s)).growX().colspan(4).pad(10).padBottom(4).minHeight(250f).row();
//				.tooltip(ModWork.bungle("afk.automessage-start-tooltip")).width(Core.scene.getWidth()/2f)
				
			} else {
				Afk.afkAvalible = false;
		        table.add(ModWork.bungle("afk-err")).color(Color.red).colspan(4).pad(10).padBottom(4).row();
			}
		} catch (Throwable e) {
			Afk.afkAvalible = false;
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

		
		createMessagesGradientPicker(table);
		table.row();

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

	private static void rebuildMessagesColors(Table table) {
		table.clearChildren();
		for (int i = 0; i < CustomChatFragment.messageColors.size; i++) {
			final int id = i;
			final Color color = CustomChatFragment.messageColors.get(id);
			Table row = table.row();
			row.table(Styles.grayPanel, t -> {
				t.table(new TextureRegionDrawable(colorBox).tint(color)).size(25, 25);
			}).size(50, 50);
			row.button("" + Iconc.pick, Styles.grayt, () -> {
				Vars.ui.picker.show(color, false, c -> {
					color.set(c);
					rebuildMessagesColors(table);
					saveMessagesColors();
				});
			}).size(50, 50);
			row.button("" + Iconc.cancel, Styles.grayt, () -> {
				CustomChatFragment.messageColors.remove(id);
				rebuildMessagesColors(table);
				saveMessagesColors();
			}).size(50, 50);
		}
	}
	

	private static void saveMessagesColors() {
		ModWork.setting("messages-gradient", CustomChatFragment.messageColors.toString(" "));
	}
	
	private static void createMessagesGradientPicker(Table mainTable) {
		mainTable.label(() -> ModWork.bungle("settings.ui.messages-gradient")).growX().colspan(4).pad(10).padBottom(10).row();
		
		Table colorsTable = mainTable.table().get();
		
		
		rebuildMessagesColors(colorsTable);
		mainTable.button(Iconc.add + "", () -> {
			Vars.ui.picker.show(CustomChatFragment.messageColors.size == 0 ? Color.sky : CustomChatFragment.messageColors.peek(), false, color -> {
				CustomChatFragment.messageColors.add(new Color(color));
				rebuildMessagesColors(colorsTable);
				saveMessagesColors();
			});
		}).growX().pad(20).padBottom(4);
		
		mainTable.row();

		mainTable.label(() -> ModWork.bungle("settings.ui.messages-gradient-trigger")).growX().colspan(4).pad(10).padBottom(10).row();
		mainTable.field(CustomChatFragment.colorTrigger, t -> {
			CustomChatFragment.colorTrigger = t;
			ModWork.setting("messages-gradient-trigger", t);
		}).row();
		
		
//		chatColor = table.button(Iconc.chat + "[white]" + ModWork.bungle("settings.ui.chat-color"), Styles.defaultt, () -> {
//			ColorPicker colorPicker = new ColorPicker();
//			colorPicker.show(CustomChatFragment.messagesColor(), false, c -> {
//				if(c.r == 1 && c.g == 1f && c.b == 1f) CustomChatFragment.messagesColor = null;
//				else CustomChatFragment.messagesColor = c;
//				ModWork.setting("ui.messages-color", c.toString());
//				
//				chatColor.get().color.set(CustomChatFragment.messagesColor());
//			});
//			
//		}).growX().pad(20).padBottom(4);
//		chatColor.get().color.set(CustomChatFragment.messagesColor());
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
