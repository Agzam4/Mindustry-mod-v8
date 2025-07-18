package agzam4.ui.mapeditor;

import arc.Core;
import arc.math.geom.Vec2;
import arc.util.Disposable;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.Tile;

public class MyMapEditor implements Disposable {

//	MapEditor
	public static MyMapEditor instance = new MyMapEditor();

	private @Nullable MapLayer mainLayer = null;
	
	private Block pen = Blocks.stoneWall;
	
	public MyMapEditor() {
	}
	
	public void beginEdit(int width, int height) {
		dispose(); // dispose old map resources 
		mainLayer = new MapLayer(width, height, true);
	}
	
	
	public void resize(int dy, int dx, int width, int height) {
		mainLayer.resize(dx, dy, width, height);
	}

	public void draw(float tx, float ty, float tw, float th) {
		tx += Core.scene.marginLeft;
		ty += Core.scene.marginBottom;
		mainLayer.renderer.draw(tx, ty, tw, th);
	}


	public int width() {
		if(mainLayer == null) return 0;
		return mainLayer.width();
	}

	public int height() {
		if(mainLayer == null) return 0;
		return mainLayer.height();
	}

	@Override
	public void dispose() {
		if(mainLayer != null) mainLayer.dispose();
	}

	public @Nullable Tile tile(Vec2 position) {
		return mainLayer.tiles().get((int)position.x, (int)position.y);
	}

	public @Nullable Tile tile(int x, int y) {
		return mainLayer.tiles().get(x, y);
	}

	public void drawAt(int x, int y) {
		Tile t = tile(x, y);
		if(t == null) return;
		t.setBlock(Blocks.copperWall);
	}

	public void clearAt(int x, int y) {
		Tile t = tile(x, y);
		if(t == null) return;
		t.setBlock(Blocks.air);
	}

	public void rerender(Tile t) {
		mainLayer.renderer.updatePoint(t.x, t.y);
	}

	public void flushOp() {
		mainLayer.flushOp();
	}

	public void undo() {
		mainLayer.undo();
	}

	public Object undosAbalible() {
		return mainLayer.undosAbalible();
	}

}
