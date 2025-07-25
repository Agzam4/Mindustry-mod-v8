package agzam4.ui;

import agzam4.AgzamMod;
import agzam4.ModWork;
import agzam4.ModWork.KeyBinds;
import agzam4.gameutils.UnitsVisibility;
import agzam4.industry.IndustryCalculator;
import agzam4.ui.editor.ButtonProps;
import agzam4.ui.editor.ButtonsPropsTable;
import agzam4.ui.editor.MobileUIEditor;
import agzam4.utils.PlayerUtils;
import agzam4.utils.Prefs;
import arc.Core;
import arc.math.Mathf;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.scene.ui.layout.*;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.ui.Styles;

public class MobileUI {
	
	public static boolean collapsed = false;

	/*
	 * TODO: unlinked tiles to different panels 
	 */
	

	public static enum MobileButtons {

		remove(new ButtonProps("remove", () -> {}).icon(Iconc.trash)), // editor only
		empty(new ButtonProps("empty", () -> {}).icon(Iconc.move)), // editor only
		
		utils(new ButtonProps("utils", PlayerUtils::show).icon(Iconc.wrench).position(0, 0)),
		collapse(new ButtonProps("collapse", b -> {
			collapsed = b;
			
		}).icon(Iconc.resize).position(1, 0).collapseable(false)),
		hideUnits(new ButtonProps("hide-units", b -> UnitsVisibility.visibility(b)).icon(Iconc.units).position(0, -1)),
		lockUnit(new ButtonProps("lock-unit", b -> AgzamMod.lockUnit(b)).icon(Iconc.lock).position(1, -1)),
		selection(new ButtonProps("selection", b -> KeyBinds.selection.isDown = b).icon(Iconc.book).position(0, -2)),
		selectionClear(new ButtonProps("selection-clear", IndustryCalculator::clearSelection).icon(Iconc.cancel).position(1, -2)),
		
		// Extra
		blockTooltips(new ButtonProps("show-blocks-tooltip", b -> Prefs.settings.put("show-blocks-tooltip", b)).toggled(() -> Prefs.settings.bool("show-blocks-tooltip")).icon(Iconc.infoCircle));

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
    public static int opacity = ModWork.settingInt("mobile-ui-buttons-opacity", 100);
    
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

	private static float onHideScaleDelta = 0;
	
	public static void build() {
		
		mainTable = new Table() {
			
			@Override
			public void act(float delta) {
				super.act(delta);
				
				if(ModWork.acceptKey()) {
					onHideScaleDelta = Mathf.clamp(onHideScaleDelta + delta*7.5f);
				} else {
					onHideScaleDelta = Mathf.clamp(onHideScaleDelta - delta*7.5f);
				}
				float visualScale = onHideScaleDelta;
				
				if(MobileUIEditor.instance.isShown()) return;
				
				ModStyles.mobileAlpha(opacity/100f*visualScale);		
				ModStyles.mobileFontAlpha(visualScale);	
			}
			
		};

//		mainTable.visible(() -> !MobileUIEditor.instance.isShown()); 
//		mainTable.touchable(() -> ModWork.acceptKey() ? Touchable.enabled : Touchable.disabled);
		
				
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

		tiles.update();
		
		
		for (var button : tiles) {
			var btn = button.button(true);
			if(button.toggle()) btn.toggle();
			var cell = container.add(btn);
			
	        cell.update(t -> {
	        	int x = button.x();//MobileButtons.collapse.prop.x();
	        	int y = button.y();//MobileButtons.collapse.prop.y();
	        	t.setBounds(x * tilesize, y * tilesize, tilesize, tilesize);
	        	cell.setBounds(x * tilesize, y * tilesize, tilesize, tilesize);
	        });
	        if(button.collapseable) cell.visible(() -> !isCollapsed());
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

		ElementDragg dragg = new ElementDragg(mainTable, "mobile-toolbar");
//		.setPosition(
//				ModWork.settingFloat("mobile-toolbar-x", 0), 
//				ModWork.settingFloat("mobile-toolbar-y", 0)
//				);
		
		dragg.box = box -> {
			if(isCollapsed()) {
				box.x = MobileButtons.collapse.prop.x()*tilesize;
				box.y = MobileButtons.collapse.prop.y()*tilesize;
				box.width = tilesize;
				box.height = tilesize;
				return;
			}
			box.x = 0;
			box.y = 0;
			box.width = tiles.width * tilesize;
			box.height = tiles.height * tilesize;
		};
		
//		new Dragg(container, container);
		
		
//		mainTable.visible(() -> ModWork.acceptKey());
	}

	public static boolean isCollapsed() {
		return collapsed && tiles.buttons.contains(MobileButtons.collapse.prop);
	}
	
	public static void tilesize(float size) {
		if(size > 100) size = 100;	
		if(size < 25) size = 25;		
		tilesize = size;
		ModWork.setting("mobile-ui-buttons-size", tilesize);
	}

	public static void opacity(int o) {
		if(o > 100) o = 100;	
		if(o < 0) o = 0;		
		opacity = o;
		ModStyles.mobileAlpha(o/100f);		
		ModWork.setting("mobile-ui-buttons-opacity", opacity);
	}


}
