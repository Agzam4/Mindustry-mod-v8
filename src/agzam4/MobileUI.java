package agzam4;

import agzam4.ModWork.KeyBinds;
import agzam4.industry.IndustryCalculator;
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
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.ui.Styles;

import static mindustry.ui.Styles.*;

public class MobileUI {
	
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
	public static Cell<TextButton> btn;

	public static TextButtonStyle buttonsStyle = Styles.grayt,
			toggleButtonsStyle = Styles.logicTogglet;
	
	public static String style(Object object) {
		if(object instanceof TextButtonStyle) {
			btn.style((TextButtonStyle) object);
			return "ok";
		}
		return "no instance of TextButtonStyle";
	}
	
	public static Table mainTable, container;
	private static Drawable background = ((TextureRegionDrawable)Tex.whiteui).tint(.2f, .2f, .2f, 1f);
	
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
				.width(100).height(150)
				.get();
		
//		HudFragment;
		
//		container.labelWrap("").width(100).height(33).row();
		
		applyStyle(container.button(Iconc.wrench + "", buttonsStyle, PlayerUtils::show)).row();

		toggle(container, Iconc.units, b -> AgzamMod.hideUnits(b));
		toggle(container, Iconc.lock, b -> AgzamMod.lockUnit(b)).row();
		
		toggle(container, Iconc.book, b -> KeyBinds.selection.isDown = b); // paste
		applyStyle(container.button(Iconc.cancel+"", buttonsStyle, IndustryCalculator::clearSelection));
		container.row();
		
		container.setPosition(Core.scene.getWidth()/2f, Core.scene.getHeight()/2f);
		
		Core.scene.add(mainTable);

//		new Dragg(btn.get(), mainTable);

//		new Dragg(mainTable, mainTable);
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
}
