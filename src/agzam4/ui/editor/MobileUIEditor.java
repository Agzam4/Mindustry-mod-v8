package agzam4.ui.editor;

import agzam4.ui.MobileUI;
import agzam4.ui.ModStyles;
import agzam4.ui.MobileUI.MobileButtons;
import agzam4.utils.Bungle;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.*;
import arc.scene.Element;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.Log;
import arc.util.Time;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static agzam4.ui.MobileUI.*;

public class MobileUIEditor extends BaseDialog {

	public static final MobileUIEditor instance = new MobileUIEditor();
	private static final Rect rect = new Rect();
	
	private static Point2 selected = new Point2();
	
	private static float bottomTable = 75f;
    
    private MobileUIEditorView view = new MobileUIEditorView();
	
    private MobileUIEditor() {
		super("");
		rebuld();
		closeOnBack(() -> {
			MobileUI.rebuild();
		});
	}
	
	Cell<TextButton> hover = null;
	
	private void rebuld() {
		clearChildren();
        setFillParent(true);
        margin(0);
        table(cont -> {
        	cont.margin(0);
            cont.left();

            cont.table(t -> t.add(view).grow().margin(0)).margin(0).grow().row();
            
            cont.table(bottom -> {
				Table container = new Table();
				
				ScrollPane pane = new ScrollPane(container);
		        for (var button : MobileButtons.values()) {
		        	var b = button.prop.button(false);
		        	b.changed(() -> {
		        		tiles.set(selected, button);
		        		view.rebuld();
		        		tiles.save("mobile-ui-table");
		        	});
		        	b.setStyle(Styles.defaultt);
		        	b.setText(button.prop.text + " " + Bungle.mobile("button." + button.prop.name));
		        	container.add(b).height(bottomTable).wrapLabel(false).pad(3f);
		        	b.row();
				}
		        
		        bottom.add(pane).margin(0).growX().fillX().row();
		        final float resizeSize = 40f;

            	
		        bottom.table(center -> {
		        	center.add(Bungle.mobile("ui-editor.size")).left().padRight(10f);
		        	center.table(t -> {
		        		t.button("-", () -> {
		        			MobileUI.tilesize(tilesize-1);
		        		}).disabled(b -> tilesize <= 25).size(resizeSize);
		        		t.label(() -> Integer.toString((int) tilesize)).pad(0, 10f, 0, 10f);
		        		t.button("+", () -> {
		        			MobileUI.tilesize(tilesize+1);
		        		}).disabled(b -> tilesize >= 100).size(resizeSize);

		        	});
		        	center.row();
		        	center.add(Bungle.mobile("ui-editor.opacity")).left().padRight(10f);
		        	center.table(t -> {
		        		t.button("-", () -> {
		        			MobileUI.opacity(opacity-10);
		        		}).disabled(b -> opacity <= 0).size(resizeSize);
		        		t.label(() -> Integer.toString((int) opacity)).pad(0, 10f, 0, 10f);
		        		t.button("+", () -> {
		        			MobileUI.opacity(opacity+10);
		        		}).disabled(b -> opacity >= 100).size(resizeSize);
		        	});
		        }).grow().fill().pad(0).margin(0);
		        
		        row();
		        bottom.background(Styles.grayPanel);
		        
            }).margin(0).bottom().growX().row();
        }).grow().margin(0).pad(0);		
	}
	
	private static class MobileUIEditorView extends Table {
		
		public MobileUIEditorView() {
			rebuld();
	        this.touchable = Touchable.enabled;
			addListener(new InputListener() {
	        	
	        	public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
	        		select(x,y);
					return true;
	        	};
	        	

				public void enter(InputEvent event, float x, float y, int pointer, Element fromActor) {
					requestScroll();
	        	};
	        	
	        	public boolean mouseMoved(InputEvent event, float x, float y) {
					requestScroll();
					return true;
	        	};
	        	
	        	public void touchDragged(InputEvent event, float x, float y, int pointer) {
	        		select(x,y);
	        	};
	        });
		}

		
		private void select(float x, float y) {
			if(x < this.x || x > this.x + width) return;
			if(y < this.y || y > this.y + height) return;
			
//			if(y-this.y <= bottomTable) return;
	        float shiftX = shiftX();
	        float shiftY = shiftY();
//	        shiftX %= tilesize;
//	        shiftY %= tilesize;
	        
			selected.x = Mathf.floor((x-shiftX) / tilesize);
			selected.y = Mathf.floor((y-shiftY) / tilesize);
			
		}
		
		private void rebuld() {
			clearChildren();
			tiles.update();
        	Log.info("tiles: @", tiles.buttons.size);
			for (var button : tiles) {
				var cell = add(button.button(false));
				cell.get().touchable = Touchable.disabled;
		        cell.update(t -> {
					float x = this.x + (button.x() - tiles.width/2f) * tilesize + width/2f;
					float y = this.y + (button.y() - tiles.height/2f) * tilesize + height/2f;
		        	t.setBounds(x, y, tilesize, tilesize);
		        	Log.info("tilesize: @", tilesize);
		        	cell.setBounds(x, y, tilesize, tilesize);
		        });
			}
		}

		private float shiftX() {
			return x + ( - tiles.width/2f) * tilesize + width/2f;
		}

		private float shiftY() {
			return y + ( - tiles.height/2f) * tilesize + height/2f;
		}
		
		@Override
		public void act(float delta) {
			ModStyles.mobileAlpha(opacity);		
			ModStyles.mobileFontAlpha(1f);	
			super.act(delta);
		}
		
		@Override
		public void draw() {
	        validate();

	        // Background
	        Draw.color(Color.black, .9f);
	        Fill.crect(x, y, width, height);

	        float shiftX = shiftX();
	        float shiftY = shiftY();

	        // Grid
	        Draw.color(Pal.darkestGray, 1f);
	        for(float x = shiftX%tilesize; x <= this.x+this.width; x += tilesize) Lines.line(x, y, x, y+height);
	        for(float y = shiftY%tilesize; y <= this.y+this.height; y += tilesize) Lines.line(x, y, x+width, y);
	        
	        // Buttons border
	        Draw.color(Pal.gray);
	        final float border = 2f;
	        for (var button : tiles) {
	        	float x = this.x + (button.x() - tiles.width/2f) * tilesize + width/2f + tilesize/2f;
	        	float y = this.y + (button.y() - tiles.height/2f) * tilesize + height/2f + tilesize/2f;
		        Fill.rect(x, y, tilesize + border*2f, tilesize + border*2f);
			}
	        Draw.reset();
	        Draw.flush();
	        
	        // Buttons
	        drawChildren();


	        float sx = selected.x * tilesize + shiftX;
	        float sy = selected.y * tilesize + shiftY;

	        if(!ScissorStack.push(rect.set(sx, sy, tilesize, tilesize))) return;

	        // Selection
	        
	        int count = 4;
	        float lstroke = tilesize*Mathf.sqrt2/4f/count;

	        Draw.color(Pal.accent, .5f);
	        Lines.stroke(lstroke);
	        for (int i = 0; i < count; i++) {
				float t = (i + Time.globalTime/Time.toSeconds)%count*tilesize/count;

				Lines.line(sx, sy+t, sx+t, sy);
				
				Lines.line(sx+t, sy+tilesize, sx+tilesize, sy+t);
	        	
			}
	        ScissorStack.pop();

	        if(!ScissorStack.push(rect.set(x, y, width, height))) return;
	      
	        Draw.color(Pal.accent);
	        Lines.stroke(border);
	        Lines.rect(sx-border, sy-border, tilesize+border*2f, tilesize+border*2f);
	        ScissorStack.pop();
		
		}
		
	}
	
}
