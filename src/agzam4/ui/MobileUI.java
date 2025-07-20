package agzam4.ui;

import agzam4.AgzamMod;
import agzam4.ModWork;
import agzam4.ModWork.KeyBinds;
import agzam4.industry.IndustryCalculator;
import agzam4.ui.editor.ButtonProps;
import agzam4.ui.editor.ButtonsPropsTable;
import agzam4.utils.PlayerUtils;
import arc.Core;
import arc.func.Cons;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.scene.utils.Elem;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.ui.Styles;

import static agzam4.ui.MobileUI.tilesize;
import static mindustry.ui.Styles.*;

public class MobileUI {
	
	public static boolean collapsed = false;

	/*
	 * TODO: unlinked tiles to different panels 
	 */
	
	public static enum MobileButtons {

		remove(new ButtonProps("remove", () -> {}).icon(Iconc.trash)), // editor only
		empty(new ButtonProps("empty", () -> {}).icon(Iconc.move)), // editor only
		utils(new ButtonProps("utils", PlayerUtils::show).icon(Iconc.wrench).position(0, 0)),
		collapse(new ButtonProps("collapse", b -> MobileUI.collapsed = b).icon(Iconc.resize).position(1, 0).collapseable(false)),
		hideUnits(new ButtonProps("hide-units", b -> AgzamMod.hideUnits(b)).icon(Iconc.units).position(0, -1)),
		lockUnit(new ButtonProps("lock-unit", b -> AgzamMod.lockUnit(b)).icon(Iconc.lock).position(1, -1)),
		selection(new ButtonProps("selection", b -> KeyBinds.selection.isDown = b).icon(Iconc.book).position(0, -2)),
		selectionClear(new ButtonProps("selection-clear", IndustryCalculator::clearSelection).icon(Iconc.cancel).position(1, -2));

		public final ButtonProps prop;
		
		MobileButtons(ButtonProps prop) {
			this.prop = prop;
		}
		
		public static @Nullable MobileButtons find(String str) {
			for (var b : values()) {
				if(b.prop.name.equals(str)) return b;
			}
			return null;
		}
		
	}
	
	public static ButtonsPropsTable tiles = ButtonsPropsTable.load("mobile-ui-table", () -> new ButtonsPropsTable(
			MobileButtons.utils.prop,
			new ButtonProps().position(1, 0),
			MobileButtons.hideUnits.prop,
			MobileButtons.lockUnit.prop,
			MobileButtons.selection.prop,
			MobileButtons.selectionClear.prop
			));
	
	
	public static boolean enabled = ModWork.settingDef("mobile-ui", false) | Vars.mobile;

    public static float tilesize = ModWork.settingFloat("mobile-ui-buttons-size", 50f);

	public static void enable() {
		if(!enabled) build();
		enabled = true;
		ModWork.setting("mobile-ui", true);
	}

	public static void disable() {
		if(enabled) build();
		enabled = false;
		ModWork.setting("mobile-ui", false);
	}

	public static void init() {
		if(enabled) build();
	}
	
	static TextButtonStyle styles[] = {
			defaultt,
		    /** Flat, square, opaque. */
		    flatt,
		    /** Flat, square, opaque, gray. */
		    grayt,
		    /** Flat, square, toggleable. */
		    flatTogglet,
		    /** Flat, square, gray border.*/
		    flatBordert,
		    /** No background whatsoever, only text. */
		    nonet,
		    /** Similar to flatToggle, but slightly tweaked for logic. */
		    logicTogglet,
		    /** Similar to flatToggle, but with a transparent base background. */
		    flatToggleMenut,
		    /** Toggle variant of default style. */
		    togglet,
		    /** Partially transparent square button. */
		    cleart,
		    /** Similar to flatToggle, but without a darker border. */
		    fullTogglet,
		    /** Toggle-able version of flatBorder. */
		    squareTogglet,
		    /** Special square button for logic dialogs. */
		    logict
	};
	
	public static int styleIndex = 0;
	private static Cell<TextButton> btn;

	public static TextButtonStyle buttonsStyle = Styles.grayt,
			toggleButtonsStyle = Styles.logicTogglet;
	
	public static String style(Object object) {
		if(object instanceof TextButtonStyle) {
			btn.style((TextButtonStyle) object);
			return "ok";
		}
		return "no instance of TextButtonStyle";
	}
	
	private static Table mainTable, container;
	public static Drawable background = ((TextureRegionDrawable)Tex.whiteui).tint(.2f, .2f, .2f, 1f);


	public static void rebuild() {
		remove();
		build();
	}
	
	public static void remove() {
		if(mainTable == null) return;
		mainTable.remove();
		mainTable = null;
	}
	
	public static void build() {
		
		mainTable = new Table();
		
				
//		mainTable = new Table().margin(10);
//		mainTable.setLayoutEnabled(true);

//		btn = mainTable.button(Iconc.move + "", buttonsStyle, () -> {
////			styleIndex++;
////			styleIndex %= styles.length;
////			btn.get().setStyle(styles[styleIndex]);
//		}).uniformX().uniformY().fill().fontScale(2f);
//		applyStyle(btn);
		
		mainTable.row();


		container = mainTable.table(background).touchable(Touchable.enabled)
				.width(1 * tilesize).height(1 * tilesize).get();
		
		
//		HudFragment;
		
//		container.labelWrap("").width(100).height(33).row();
		
		for (var button : tiles) {
			var cell = container.add(button.button(true));
			
	        cell.update(t -> {
	        	int x = button.x() - MobileButtons.collapse.prop.x();
	        	int y = button.y() - MobileButtons.collapse.prop.y();
	        	t.setBounds(x * tilesize, y * tilesize, tilesize, tilesize);
	        	cell.setBounds(x * tilesize, y * tilesize, tilesize, tilesize);
	        });
	        if(button.collapseable) cell.visible(() -> !collapsed);
		}
		
//		applyStyle(container.button(Iconc.wrench + "", buttonsStyle, PlayerUtils::show)).row();
//
//		toggle(container, Iconc.units, b -> AgzamMod.hideUnits(b));
//		toggle(container, Iconc.lock, b -> AgzamMod.lockUnit(b)).row();
//		
//		toggle(container, Iconc.book, b -> KeyBinds.selection.isDown = b); // paste
//		applyStyle(container.button(Iconc.cancel+"", buttonsStyle, IndustryCalculator::clearSelection));
//		container.row();
//		
		container.setPosition(Core.scene.getWidth()/2f, Core.scene.getHeight()/2f);
		container.background(Styles.none);

		Core.scene.add(mainTable);

		new Dragg(mainTable, mainTable).setPosition(
				ModWork.settingFloat("mobile-toolbar-x", 0), 
				ModWork.settingFloat("mobile-toolbar-y", 0)
				);
		
//		new Dragg(container, container);
		
		mainTable.visible(() -> ModWork.acceptKey());
	}

	private static Cell<TextButton> applyStyle(Cell<TextButton> cell) {
		cell.width(50).height(50).margin(0).pad(0);
		return cell;
	}
	
	private static Cell<TextButton> toggle(Table table, char icon, Cons<Boolean> listener) {
        TextButton button = Elem.newButton(icon + "", toggleButtonsStyle, null);
        button.changed(() -> {
			listener.get(button.isChecked());
        });
		return applyStyle(table.add(button));
	}


	static class Dragg {
		
		Element dragger, parent;
		
		protected Vec2 draggStart = null;
		
		public Dragg(Element dragger, Element parent) {
			this.dragger = dragger;
			this.parent = parent;
			
			dragger.addListener(new InputListener() {

				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
					draggStart = new Vec2(x, y);
					return true;
				}

				@Override
				public void touchDragged(InputEvent event, float x, float y, int pointer) {
					update(x, y);
				}

				@Override
				public void touchUp(InputEvent e, float x, float y, int pointer, KeyCode button) {
					update(x, y);
					draggStart = null;

					ModWork.setting("mobile-toolbar-x", parent.x);
					ModWork.setting("mobile-toolbar-y", parent.y);
				}
			});
		}

		public void update(float x, float y) {
			setPosition(
					Mathf.clamp(parent.x-draggStart.x+x, 
							parent.getPrefWidth()/2f, 
							Core.scene.getWidth() - parent.getPrefWidth()/2f),
					
					Mathf.clamp(parent.y-draggStart.y+y,
							parent.getPrefHeight()/2f, 
							Core.scene.getHeight() - parent.getPrefHeight()/2f));
		}

		public void setPosition(float x, float y) {
			parent.setPosition(
					Mathf.clamp(x, parent.getPrefWidth()/2f, 
							Core.scene.getWidth() - parent.getPrefWidth()/2f),
					Mathf.clamp(y, parent.getPrefHeight()/2f, 
							Core.scene.getHeight() - parent.getPrefHeight()/2f));
		}
	}


	public static void tilesize(float size) {
		if(size > 100) size = 100;	
		if(size < 25) size = 25;		
		tilesize = size;
		ModWork.setting("mobile-ui-buttons-size", tilesize);
	}


}
