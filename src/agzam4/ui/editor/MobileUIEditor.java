package agzam4.ui.editor;


import agzam4.ModWork;
import agzam4.ui.MobileUI;
import agzam4.ui.MobileUI.MobileButtons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.ScissorStack;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static agzam4.ui.MobileUI.tilesize;
import static mindustry.Vars.mobile;
import static agzam4.ui.MobileUI.tiles;

public class MobileUIEditor extends BaseDialog {

	public static final MobileUIEditor instance = new MobileUIEditor();
	private static final Rect rect = new Rect();
	
	private static Point2 selected = new Point2();
	
	private static float bottomTable = 75f;
	
	private static TextButtonStyle styleGray = new TextButtonStyle(){{
        over = Styles.flatOver;
        font = Fonts.def;
        fontColor = Color.white;
        disabledFontColor = Color.gray;
        disabled = Styles.flatOver;
        down = Styles.flatOver;
        up = Styles.flatOver;
    }};
    
    private MobileUIEditorView view = new MobileUIEditorView();
	
    private MobileUIEditor() {
		super("");
		closeOnBack();
		
//		MapObjectivesCanvas
//		MapObjectivesDialog
		
		rebuld();
//		setColor(new Color(0, 0, 0, 0));
//		background(Styles.none);
//		cont.background(Styles.none);
		
//		setStyle(new DialogStyle(){{
//            stageBackground = Styles.none;
//            titleFont = Fonts.def;
//            background = windowEmpty;
//            titleFontColor = Pal.accent;
//        }});
		
	}
	
	Cell<TextButton> hover = null;
	
	private void rebuld() {
		clearChildren();

        float size = mobile ? 50f : 58f;

        clearChildren();
        setFillParent(true);
        margin(0);
        table(cont -> {
        	cont.margin(0);
            cont.left();

            cont.table(t -> t.add(view).grow().margin(0)).margin(0).grow().row();
            
            cont.table(bottom -> {
//			bottom.table(container -> {
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
		        	b.setText(button.prop.text + " " + ModWork.bungle("mobile-ui.button." + button.prop.name));
		        	container.add(b).height(bottomTable).wrapLabel(false).pad(3f);
		        	b.row();
				}
		        
		        bottom.add(pane).margin(0).growX().fillX().row();
		        final float resizeSize = 40f;
		        bottom.table(t -> {
		        	t.button("-", () -> {
		        		MobileUI.tilesize(tilesize-1);
		        	}).disabled(b -> tilesize <= 25).size(resizeSize);
		        	t.label(() -> Integer.toString((int) tilesize)).pad(0, 10f, 0, 10f);
		        	t.button("+", () -> {
		        		MobileUI.tilesize(tilesize+1);
		        	}).disabled(b -> tilesize >= 100).size(resizeSize);
		        	
		        }).margin(0).growX().fillX().height(resizeSize).row();
		        
		        bottom.background(Styles.grayPanel);
		        
            }).margin(0).bottom().growX().row();
            
//            cont.table(mid -> {
//                mid.top();
//
//                Table tools = new Table().top();
//                tools.defaults().size(size, size);
//
//
////                mid.table(Tex.underline, t -> {
////                    Slider slider = new Slider(0, MapEditor.brushSizes.length - 1, 1, false);
////                    slider.moved(f -> editor.brushSize = MapEditor.brushSizes[(int)f]);
////                    for(int j = 0; j < MapEditor.brushSizes.length; j++){
////                        if(MapEditor.brushSizes[j] == editor.brushSize){
////                            slider.setValue(j);
////                        }
////                    }
////
////                    var label = new Label("@editor.brush");
////                    label.setAlignment(Align.center);
////                    label.touchable = Touchable.disabled;
////
////                    t.top().stack(slider, label).width(size * 3f - 20).padTop(4f);
////                    t.row();
////                }).padTop(5).growX().top();
//
//                mid.row();
//
//                mid.row();
//
//                mid.table(t -> {
//                    t.button("@editor.cliffs", Icon.terrain, Styles.flatt, editor::addCliffs).growX().margin(9f);
//                }).growX().top();
//            }).margin(0).top().growY().row();



        }).grow().margin(0).pad(0);		
		
        MobileUI.rebuild();
		
//		cont.table(bottom -> {
////			bottom.table(container -> {
//				Table container = new Table();
//				
//				ScrollPane pane = new ScrollPane(container);
//
//		        for (var button : MobileButtons.values()) {
//		        	var b = button.prop.button();
//		        	b.changed(() -> {
//		        		view.rebuld();
//		        	});
//		        	b.setStyle(Styles.defaultt);
//		        	b.setText(ModWork.bungle("mobile-ui.button." + button.prop.name));
//		        	container.add(b).size(bottomTable, bottomTable);
//		        	b.row();
//				}
//		        container.background(Styles.accentDrawable);
////			}).growX().fillX();
//		        
//		        bottom.add(pane).growX().fillX();    
//			
//			
//			
//			bottom.background(Styles.grayPanel);
//			
//		}).grow();
////		.update(bottom -> {
////			bottom.setBounds(x, y, width, bottomTable);
////		});
//
//        cont.table(t -> t.add(view).grow()).grow();
	}

	private float shiftX() {
		return (-tiles.minX - tiles.width/2f) * tilesize + width/2f;
	}

	private float shiftY() {
		return (-tiles.minY - tiles.height/2f) * tilesize + height/2f;
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
			for (var button : tiles) {
				var cell = add(button.button(false));
				cell.get().touchable = Touchable.disabled;
		        cell.update(t -> {
					float x = this.x + (button.x()-tiles.minX - tiles.width/2f) * tilesize + width/2f;
					float y = this.y + (button.y()-tiles.minY - tiles.height/2f) * tilesize + height/2f;
		        	t.setBounds(x, y, tilesize, tilesize);
		        	cell.setBounds(x, y, tilesize, tilesize);
		        });
			}
		}

		private float shiftX() {
			return x + (-tiles.minX - tiles.width/2f) * tilesize + width/2f;
		}

		private float shiftY() {
			return y + (-tiles.minY - tiles.height/2f) * tilesize + height/2f;
		}
		
		@Override
		public void draw() {
	        validate();

	        Draw.color(Color.black, .9f);
	        Fill.crect(x, y, width, height);

	        float shiftX = shiftX();
	        float shiftY = shiftY();

	        Draw.color(Pal.darkestGray, 1f);
	        
	        for(float x = shiftX%tilesize; x <= this.x+this.width; x += tilesize) Lines.line(x, y, x, y+height);
	        for(float y = shiftY%tilesize; y <= this.y+this.height; y += tilesize) Lines.line(x, y, x+width, y);
	        
	        Draw.color(Pal.gray);

	        final float border = 2f;

	        for (var button : tiles) {
	        	float x = this.x + (button.x()-tiles.minX - tiles.width/2f) * tilesize + width/2f + tilesize/2f;
	        	float y = this.y + (button.y()-tiles.minY - tiles.height/2f) * tilesize + height/2f + tilesize/2f;
		        Fill.rect(x, y, tilesize + border*2f, tilesize + border*2f);
			}
	        
	        
	        Draw.reset();
	        Draw.flush();
	        
//			super.draw();
	        drawChildren();

//	        if(!ScissorStack.push(rect.set(x, y+bottomTable, width, height-bottomTable))) return;
	        
	        Draw.color(Pal.accent, .5f);
//	        Fill.rect(selected.x * tilesize + tilesize/2f + shiftX, selected.y * tilesize + tilesize/2f + shiftY, tilesize + border*2f, tilesize + border*2f);

	        float sx = selected.x * tilesize + shiftX;
	        float sy = selected.y * tilesize + shiftY;

	        if(!ScissorStack.push(rect.set(sx, sy, tilesize, tilesize))) return;
	        
	        int count = 4;
	        float lstroke = tilesize*Mathf.sqrt2/4f/count;
	        

	        
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

//	        Lines.rect(x,y,width,height);

//	        ScissorStack.pop();
//	        for(int y = minY; y <= maxY; y++) Lines.line(minX * unitSize, progY + y * unitSize, maxX * unitSize, progY + y * unitSize);
		
		}
		
	}
	
}
