package agzam4.ui.mapeditor;

import agzam4.MyDraw;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.ScissorStack;
import arc.input.KeyCode;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Scl;
import arc.util.Align;
import arc.util.Log;
import arc.util.Strings;
import mindustry.content.Blocks;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;

public class MyMapView extends Element {
	
//	MapView
//	private Label statusbar;

	private Font font = Fonts.def;
    private GlyphLayout layout = new GlyphLayout();
	
	private MyMapEditor editor = MyMapEditor.instance;

    public final Vec2 position = new Vec2(); // 1 unit = 1 tile
    public float zoom = 1f; // 1 means fit in component

    public final Vec2 mousePx = new Vec2();
    public final Vec2 mouse = new Vec2();
    public final Vec2 lastMousePx = new Vec2();
    public final Vec2 lastMouse = new Vec2();
    public final Vec2 mouseDelta = new Vec2();
    private float scroll = 0;

	private Rect rect = new Rect();
    
    MyEditorTool tool = MyEditorTool.pen;


    
    public MyMapView() {
        this.touchable = Touchable.enabled;
        scrolled(delta -> scroll += upx(delta*30f));

        addListener(new InputListener() {
        	
        	public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
        		mousePx.set(x, y);
                requestScroll();
				return true;
        	};
        	
        	public void enter(InputEvent event, float x, float y, int pointer, Element fromActor) {
        		mousePx.set(x, y);
                requestScroll();
        	};
        	
        	public boolean mouseMoved(InputEvent event, float x, float y) {
        		mousePx.set(x, y);
                requestScroll();
				return true;
        	};
        	
        	public void touchDragged(InputEvent event, float x, float y, int pointer) {
        		mousePx.set(x, y);
        	};
        });
        
        
//        statusbar = new Label(() -> Strings.format("@, @", (int)mouse.x, (int)mouse.y));
    }

    @Override
    public void layout() {
//    	statusbar.layout();
    }
    
    boolean hasOperation = false;

    @Override
    public void act(float delta) {
    	mouseDelta.set(Core.input.mouse()).sub(lastMousePx).scl(upx());
    	updateMouse();

    	if(Core.input.keyTap(KeyCode.d)) position.add(+1,0);
    	if(Core.input.keyTap(KeyCode.a)) position.add(-1,0);

    	if(Core.input.keyTap(KeyCode.w)) position.add(0, +1);
    	if(Core.input.keyTap(KeyCode.s)) position.add(0, -1);
    	
    	if(Core.input.keyDown(KeyCode.mouseMiddle)) {
    		Core.input.mouse();
    		position.sub(mouseDelta);
    	}
    	if(Core.input.ctrl()) {
    		zoom(-scroll);
    	} else {
        	if(Core.input.shift()) {
        		position.add(scroll, 0);
        	} else {
        		position.add(0, -scroll);
        	}
    	}
    	
    	if(Core.input.keyTap(KeyCode.space)) {
    		var t = editor.tile(mouse);
    		if(t != null) {
    			t.setBlock(Blocks.yellowStoneWall);
        		editor.rerender(t);
    		}
    		Log.info("tile: @", t);
    	}
//    	if(Core.input.keyDown(KeyCode.scroll))

        
		scroll = 0;
		lastMousePx.set(Core.input.mouse());
    	
    	updateMouse();
    	
    	boolean hasOperation = tool.line(editor, lastMouse.x, lastMouse.y, mouse.x, mouse.y);
    	

    	if(this.hasOperation && !hasOperation) {
    		editor.flushOp();
    	}
    	
    	this.hasOperation = hasOperation;
    	
    	if(Core.input.keyTap(KeyCode.z)) {
    		editor.undo();
    	}
    	
    	lastMouse.set(mouse);
    }

    private void updateMouse() {
        mouse.set(mousePx).sub(x + width/2f, y + height/2f).scl(upx()).add(position);
	}


	private void zoom(float z) {
		if(z == 0) return;
    	updateMouse();
    	
    	float x = mouse.x;
    	float y = mouse.y;
    	if(z > 0) {
    		if(zoom > 100f) zoom = 100f;
    		zoom *= 1.2f;
    	} else {
    		zoom /= 1.2f;
    		if(zoom < .1f) zoom = .1f;
    	}
//		if(zoom + z*zoom/25f <= 0) return; // negative zoom is fun but...
//		zoom += z*zoom/25f;	
    	updateMouse();

    	float nx = mouse.x;
    	float ny = mouse.y;
		
    	position.sub(nx, ny).add(x, y);
    	updateMouse();
	}
    
    
	private float px(float tiles) {
    	float size = Math.min(width / editor.width(), height / editor.height());
		return tiles * zoom * size;
	}

    private float upx(float px) {
    	float size = Math.min(width / editor.width(), height / editor.height());
		return px / zoom / size;
	}

    private float upx() {
		return 1f / zoom / Math.min(width / editor.width(), height / editor.height());
	}

    @Override
    public void draw() {
        float ratio = 1f / ((float)editor.width() / editor.height());
        float size = Math.min(width, height);
        float sclwidth = size * zoom;
        float sclheight = size * zoom * ratio;
        float centerx = (x + width/2f)  - px(position.x - editor.width()/2f);
        float centery = (y + height/2f) - px(position.y - editor.height()/2f);

//        image.setImageSize(editor.width(), editor.height());

        if(!ScissorStack.push(rect.set(x + Core.scene.marginLeft, y + Core.scene.marginBottom, width, height))){
            return;
        }

        Draw.color(Pal.remove);
        Lines.stroke(2f);
        Lines.rect((centerx) - sclwidth / 2 - 1, (centery) - sclheight / 2 - 1, sclwidth + 2, sclheight + 2);
        editor.draw(centerx - sclwidth / 2, centery - sclheight / 2, sclwidth, sclheight);

        Draw.color(Pal.accent);
		var t = editor.tile(mouse);
		if(t == null) {
	        Draw.color(Pal.remove);
		}
        Fill.circle(centerx + px(mouse.x) - px(editor.width()/2f), centery + px(mouse.y) - px(editor.height()/2f), 5f);

//        MyDraw.textColor(Strings.format("@, @", (int)mouse.x, (int)mouse.y), 
//        		(x + width - 15), (y + height - 15), 1f,1f,1f, 5f, Align.left);
//
//        MyDraw.textColor(Strings.format("@, @", (int)mouse.x, (int)mouse.y), 
//        		(x + width/2), (y + height/2), 1f,1f,1f, 5f, Align.center);
        
        Draw.reset();
        

//        if(grid){
//            Draw.color(Color.gray);
//            image.setBounds(centerx - sclwidth / 2, centery - sclheight / 2, sclwidth, sclheight);
//            image.draw();
//
//            Lines.stroke(2f);
//            Draw.color(Pal.bulletYellowBack);
//            Lines.line(centerx - sclwidth/2f, centery - sclheight/4f, centerx + sclwidth/2f, centery - sclheight/4f);
//            Lines.line(centerx - sclwidth/4f, centery - sclheight/2f, centerx - sclwidth/4f, centery + sclheight/2f);
//            Lines.line(centerx - sclwidth/2f, centery + sclheight/4f, centerx + sclwidth/2f, centery + sclheight/4f);
//            Lines.line(centerx + sclwidth/4f, centery - sclheight/2f, centerx + sclwidth/4f, centery + sclheight/2f);
//
//            Lines.stroke(3f);
//            Draw.color(Pal.accent);
//            Lines.line(centerx - sclwidth/2f, centery, centerx + sclwidth/2f, centery);
//            Lines.line(centerx, centery - sclheight/2f, centerx, centery + sclheight/2f);
//
//            Draw.reset();
//        }

//        int index = 0;
//        for(int i = 0; i < MapEditor.brushSizes.length; i++){
//            if(editor.brushSize == MapEditor.brushSizes[i]){
//                index = i;
//                break;
//            }
//        }

//        float scaling = zoom * Math.min(width, height) / editor.width();

        Draw.color(Pal.accent);
        Lines.stroke(Scl.scl(2f));

//        if((!editor.drawBlock.isMultiblock() || tool == MyEditorTool.eraser) && tool != MyEditorTool.fill){
//            if(tool == MyEditorTool.line && drawing){
//                Vec2 v1 = unproject(startx, starty).add(x, y);
//                float sx = v1.x, sy = v1.y;
//                Vec2 v2 = unproject(lastx, lasty).add(x, y);
//
//                Lines.poly(brushPolygons[index], sx, sy, scaling);
//                Lines.poly(brushPolygons[index], v2.x, v2.y, scaling);
//            }
//
//            if((tool.edit || (tool == MyEditorTool.line && !drawing)) && (!mobile || drawing)){
//                Point2 p = project(mousex, mousey);
//                Vec2 v = unproject(p.x, p.y).add(x, y);
//
//                //pencil square outline
//                if(tool == MyEditorTool.pencil && tool.mode == 1){
//                    Lines.square(v.x + scaling/2f, v.y + scaling/2f, scaling * ((editor.brushSize == 1.5f ? 1f : editor.brushSize) + 0.5f));
//                }else{
//                    Lines.poly(brushPolygons[index], v.x, v.y, scaling);
//                }
//            }
//        }else{
//            if((tool.edit || tool == MyEditorTool.line) && (!mobile || drawing)){
//                Point2 p = project(mousex, mousey);
//                Vec2 v = unproject(p.x, p.y).add(x, y);
//                float offset = (editor.drawBlock.size % 2 == 0 ? scaling / 2f : 0f);
//                Lines.square(
//                v.x + scaling / 2f + offset,
//                v.y + scaling / 2f + offset,
//                scaling * editor.drawBlock.size / 2f);
//            }
//        }

        final float statusbarHeight = 20f;
        
        Draw.color(Pal.accent);
        Lines.stroke(Scl.scl(3f));
        Lines.rect(x, y+statusbarHeight, width, height-statusbarHeight);

        Draw.color(0f, 0f, 0f, .8f);
        Fill.rect(x+width/2f, y+statusbarHeight/2f, width, statusbarHeight);

        Draw.color(Color.white);

//        MyDraw.textOld(Strings.format("@, @", (int)mouse.x, (int)mouse.y), 
//        		x+width - statusbarHeight/2f, y+statusbarHeight/2f, 15f, Align.right, Fonts.def);
        
        MyDraw.text(Strings.format("@, @ | @", (int)mouse.x, (int)mouse.y, editor().undosAbalible()), 
        		x+width - statusbarHeight/2f, y+statusbarHeight/2f, 15f, Align.right, Fonts.def);

        Draw.reset();

        ScissorStack.pop();
    }
    
	public MyMapEditor editor() {
		return editor;
	}
}
