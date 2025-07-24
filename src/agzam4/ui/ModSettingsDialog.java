package agzam4.ui;

import agzam4.*;
import agzam4.UpdateInfo.CheckUpdatesInterval;
import agzam4.debug.Debug;
import agzam4.gameutils.Afk;
import agzam4.ui.editor.MobileUIEditor;
import agzam4.uiOverride.*;
import agzam4.utils.Bungle;
import agzam4.utils.Prefs;
import arc.Core;
import arc.files.Fi;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.mod.Mods.LoadedMod;
import mindustry.ui.*;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;

public class ModSettingsDialog extends Table {

	static Cell<TextButton> unlockContent = null;
	static Cell<TextButton> unlockBlocks = null;
	static Cell<TextButton> chatColor = null;
	
	static TextureRegion colorBox = AgzamMod.sprite("color-box");
	
	static String versionInfo = "";

	static Cell<Table> updateTable = null;
	static Cons<Table> updateBuilder;

	static Cell<Table> unlockTable = null;
	
	public static void builder(SettingsTable settingsTable) {
		settingsTable.defaults().left();
		Table table = new Table();
		
        settingsTable.add(table);
        settingsTable.row();
        
		settingsTable.name = Bungle.settings("name");
		settingsTable.visible = true;

		addCategory(table, "updates");
		
		updateBuilder = t -> {
			t.label(() -> UpdateInfo.currentName() + " v" + UpdateInfo.currentVersion() + " " + versionInfo).row();
			t.label(() -> Strings.format(Bungle.settings("updates.latest-info"), UpdateInfo.latestVersion)).visible(() -> UpdateInfo.latestVersion != null).row();
			
			t.button(Bungle.settings("checkupdates.checkupdates"), Icon.refresh, Styles.defaultt, () -> {
				versionInfo = Core.bundle.get("loading");
				UpdateInfo.check((old, now) -> {
					versionInfo = (now ? "[red]" : "[lime]") + Bungle.settings(now ? "updates.outdated" : "updates.latest");
					if(old != now) {
						updateTable.get().clearChildren();
						updateBuilder.get(updateTable.get());
						updateTable.fillX().pad(6).colspan(4).padTop(0).padBottom(10);
						updateTable.row();
						updateCategory();
					}
				});
			}).wrapLabel(false).growX().pad(10).padBottom(4);
			t.row();
			
			if(UpdateInfo.needUpdate()) {
				t.add(Bungle.settings("need-update")).color(Color.red).colspan(4).pad(10).padBottom(4).row();
				t.button("@mods.browser.reinstall", Icon.download, () -> UpdateInfo.githubImportMod(AgzamMod.getRepo(), null))
				.fillX().pad(6).colspan(4).padTop(0).padBottom(10).row();
			}
			// Vars.mods.getMod("agzam4mod").meta.version = 'as'
            t.table(Tex.button, tg -> {
                tg.margin(10f);
                var group = new ButtonGroup<>();
                var style = Styles.flatTogglet;
                for (var cui : CheckUpdatesInterval.values()) {
                    tg.button(Bungle.settings("check-updates." + cui.kebab()), style, () -> {
                    	UpdateInfo.checkUpdatesInterval(cui);
                    }).growX().fillX().group(group).height(35f);
				}
            }).fillX().pad(6).colspan(4).padTop(0).padBottom(10);

			if(Debug.debug) {
				t.row();
				t.button("Reload mod", Icon.refreshSmall, () -> {
//					try {
//						ClassLoaderCloser.close(AgzamMod.mod.loader);
//					} catch(Exception e){
//						Log.err(e);
//						Vars.ui.showErrorMessage(e.getMessage());
//					}

			        Vars.mods.list().remove(AgzamMod.mod);
//			        Vars.mods.loadMod(AgzamMod.mod);
//			        AgzamMod.mod.dispose();
			        
			        AgzamMod.mod.dispose();
			        if(AgzamMod.mod.main instanceof AgzamMod mod) mod.dispose();
			        
			        LoadedMod mod = Reflect.invoke(Vars.mods, "loadMod", new Object[] {
							AgzamMod.mod.file,
							false, true,
					}, Fi.class, Boolean.TYPE, Boolean.TYPE);
			        Vars.mods.list().add(mod);
			        mod.main.init();
					Vars.ui.showInfo("Reloaded");
					settingsTable.visible = false;
				}).fillX().pad(6).colspan(4).padTop(0).padBottom(10).row();
			}
		};
		
		updateTable = table.table(updateBuilder);
		updateTable.fillX().pad(6).colspan(4).padTop(0).padBottom(10);
		updateTable.row();
		
		addCategory(table, "unlock");
		

		unlockTable = table.table(t -> {
//			t.label(() -> UpdateInfo.currentName() + " v" + UpdateInfo.currentVersion() + " " + versionInfo).row();
//			if(Debug.debug) t.label(() -> "random: " + random).row();
//			EditorMapsDialog

//	        t.button("@editor.openin", Icon.export, () -> {
//	        }).growX().fillX().height(54f).marginLeft(10).padRight(5f);
//
//	        t.button("@delete", Icon.trash, () -> {
//	        }).growX().fillX().height(54f).marginLeft(10).padLeft(5f);

			table.check(Bungle.settings("unlock-content"), false, b -> {
				if(b) showHiddenContent();
				else hideHiddenContent();
			}).colspan(4).pad(10).padBottom(4).left().row();

			table.check(Bungle.settings("unlock-blocks"), false, b -> {
				if(b) unlockBlocksContent();
				else lockBlocksContent();
			}).colspan(4).pad(10).padBottom(4).left().row();

//			unlockContent = t.button(ModWork.bungle("settings.unlock-content"), Icon.lockOpen, Styles.defaultt, () -> {
//				unlockDatabaseContent();
//				
//				if(unlockContent != null) unlockContent.disabled(true);
//			}).growX().fillX().height(54f).marginLeft(10).padRight(5f);
//			unlockBlocks = t.button(ModWork.bungle("settings.unlock-blocks"), Icon.lockOpen, Styles.defaultt, () -> {
//				unlockBlocksContent();
//				if(unlockBlocks != null) unlockBlocks.disabled(true);
//			}).growX().fillX().height(54f).marginLeft(10).padLeft(5f);
//			
		});
//		unlockTable.expand().fill();
		unlockTable.growX().fillX().pad(6).colspan(4).padTop(0).padBottom(10);
		unlockTable.row();
//		unlockTable = table.table(t -> {
//			t.label(() -> UpdateInfo.currentName() + " v" + UpdateInfo.currentVersion() + " " + versionInfo).row();
//
//			t.button("weh", Icon.download, () -> {})
//			.fillX().pad(6).colspan(4).padTop(0).padBottom(10).row();
//			
//			unlockContent = t.button(ModWork.bungle("settings.unlock-content"), Icon.lockOpen, Styles.defaultt, () -> {
//				unlockDatabaseContent();
//				if(unlockContent != null) unlockContent.disabled(true);
//			}).fillX().pad(6).colspan(4).padTop(0).padBottom(10);
//			t.row();
//			
//			unlockBlocks = t.button(ModWork.bungle("settings.unlock-blocks"), Icon.lockOpen, Styles.defaultt, () -> {
//				unlockBlocksContent();
//				if(unlockBlocks != null) unlockBlocks.disabled(true);
//			}).fillX().pad(6).colspan(4).padTop(0).padBottom(10);
//			t.background(Styles.grayPanel);
//			t.row();
//
//			addCategory(t, "unlock");
//		});
//		unlockTable.fillX().pad(6).colspan(4).padTop(0).padBottom(10);
//		unlockTable.row();
		
		
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
				}).tooltip(Bungle.afk("automessage-start-tooltip")).width(Core.scene.getWidth()/2f).row();
				addCheck(table, "afk.afk-ping");
				addCheck(table, "afk.auto-afk-mode", b -> Afk.autoAI = b);
				table.labelWrap(() -> Strings.format(Bungle.afk("default-names"), Afk.baseName(), Afk.ruName())).growX().colspan(4).pad(10).padBottom(4).row();

				table.labelWrap(() -> Strings.format(Bungle.afk("custom-names"), Afk.baseName(), Afk.ruName())).growX().colspan(4).pad(10).padBottom(4).row();
		        table.area(Afk.names(), s -> Afk.names(s)).growX().colspan(4).pad(10).padBottom(4).minHeight(250f).row();
//				.tooltip(ModWork.bungle("afk.automessage-start-tooltip")).width(Core.scene.getWidth()/2f)
				
			} else {
				Afk.afkAvalible = false;
		        table.add(Bungle.afk("err")).color(Color.red).colspan(4).pad(10).padBottom(4).row();
			}
		} catch (Throwable e) {
			Afk.afkAvalible = false;
	        table.add(Bungle.afk("err")).color(Color.red).colspan(4).pad(10).padBottom(4).row();
		}
		
		/*
		 * addCategory(table, "utils");
		 * addKeyBind(table, KeyBinds.openUtils);
		 */
		
		addCategory(table, "custom-ui");

		if(MobileUI.enabled) {
			table.button(Bungle.settings("edit-mobile-ui"), () -> {
				MobileUIEditor.instance.show();
			}).growX().fillX().pad(6).colspan(4).padTop(10).padBottom(10).row();
		}
		
		addCheck(table, "custom-chat-fragment", b -> UiOverride.set());
		addCheck(table, "outline-chat", b -> CustomChatFragment.font = b ? Fonts.outline : Fonts.def);

		
		
		createMessagesGradientPicker(table);
		table.row();

		addCategory(table, "report-bugs");
		table.table(t -> {
			t.button(Iconc.github + " Github", Styles.defaultt, () -> {
	            if(!Core.app.openURI("https://github.com/Agzam4")){
	                Vars.ui.showErrorMessage("@linkfail");
	                Core.app.setClipboardText("https://github.com/Agzam4");
	            }
			}).growX().fillX().height(54f).marginLeft(10).padRight(5f);
			t.button(Iconc.play + " YouTube", Styles.defaultt, () -> {
	            if(!Core.app.openURI("https://www.youtube.com/@agzam4/")){
	            	Vars.ui.showErrorMessage("@linkfail");
	                Core.app.setClipboardText("https://www.youtube.com/@agzam4/");
	            }
			}).growX().fillX().height(54f).marginLeft(10).padLeft(5f);
		}).growX().fillX().pad(6).colspan(4).padTop(0).padBottom(10);
		
		table.row();
//		addCategory(table, "mobile-ui");
		
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
		mainTable.label(() -> Bungle.settings("ui.messages-gradient")).growX().colspan(4).pad(10).padBottom(10).row();
		
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

		mainTable.label(() -> Prefs.settings.string("ui.messages-gradient-trigger", "")).growX().colspan(4).pad(10).padBottom(10).row();
		mainTable.field(CustomChatFragment.colorTrigger, t -> {
			CustomChatFragment.colorTrigger = t;
			Prefs.settings.put("messages-gradient-trigger", t);
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
		String tooltip = Bungle.settingsTooltip(settings);
		var cell = table.check(Bungle.settings(settings), ModWork.settingDef(settings, def), b -> {
			ModWork.setting(settings, b);
			if(listener != null) listener.get(b);
		}).colspan(4).pad(10).padBottom(4).left();
		cell.row();
		if(Vars.mobile) {
	        Vars.ui.addDescTooltip(cell.get(), tooltip);
		} else {
			cell.tooltip(tooltip);
		}
	}
	
	private static void addCategory(Table table, String category) {
		table.add(Bungle.category(category)).color(Pal.accent).colspan(4).pad(10).padBottom(4).row();
	    table.image().color(Pal.accent).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).row();
	}
	
	private static ObjectSet<UnlockableContent> hiddenContent = new ObjectSet<UnlockableContent>();

	private static void hideHiddenContent() {
		Vars.content.units().each(c -> c.hidden = hiddenContent.remove(c));
		Vars.content.items().each(c -> c.hidden = hiddenContent.remove(c));
		Vars.content.liquids().each(c -> c.hidden = hiddenContent.remove(c));
	}
	
	private static void showHiddenContent() {
		Vars.content.units().each(u -> {
			if(u.hidden) hiddenContent.add(u);
			u.hidden = false;
		});
		Vars.content.items().each(i -> {
			if(i.hidden) hiddenContent.add(i);
			i.hidden = false;
		});
		Vars.content.liquids().each(l -> {
			if(l.hidden) hiddenContent.add(l);
			l.hidden = false;
		});		
	}
	

	private static ObjectMap<Block, BuildVisibility> blocksBuildVisibility = new ObjectMap<>();
	
	private static void lockBlocksContent() {
		Vars.content.blocks().each(b -> {
			var v = blocksBuildVisibility.remove(b);
			if(v == null) return;
			b.buildVisibility = v;
		});
	}
	
	private static void unlockBlocksContent() {
		Vars.content.blocks().each(b -> {
			blocksBuildVisibility.put(b, b.buildVisibility);
			b.buildVisibility = BuildVisibility.shown;
		});
	}

	private static String lastCategoryName = null;
	
	public static void setCategory(String name) {
		clearCategory();
		Vars.ui.settings.addCategory(name, Icon.wrench, ModSettingsDialog::builder);		
		lastCategoryName = name;
	}

	public static void clearCategory() {
		if(lastCategoryName == null) return;
		Vars.ui.settings.getCategories().remove(c -> c.name.equals(lastCategoryName));
	}

	public static void updateCategory() {
		if(UpdateInfo.needUpdate()) {
			ModSettingsDialog.setCategory(Bungle.settings("name") + " [red]" + Iconc.warning);
		} else {
			ModSettingsDialog.setCategory(Bungle.settings("name"));
		}		
	}
	
}
