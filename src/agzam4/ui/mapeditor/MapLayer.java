package agzam4.ui.mapeditor;

import arc.util.Disposable;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.editor.DrawOperation;
import mindustry.editor.OperationStack;
import mindustry.gen.TileOp;
import mindustry.world.Tile;
import mindustry.world.Tiles;

public class MapLayer implements Disposable {

	public MapLayerRender renderer = new MapLayerRender(this);
	private Tiles tiles = null;
    
	public final boolean main;
	private int width, height;

    private MyOperationStack stack = new MyOperationStack(); // TODO: move to editor
	private MyDrawOperation currentOp;

    public MapLayer(int width, int height) {
		this(width, height, false);
	}

    public MapLayer(int width, int height, boolean main) {
		resize(width, height);
		this.main = main;
	}

    public void resize(int width, int height) {
    	resize(0, 0, width, height);
    }
    
    public void resize(int dx, int dy, int width, int height) {
    	var tiles = main ? Vars.world.resize(width, height) : new Tiles(width, height);
    	this.width = width;
    	this.height = height;
    	
//    	var resized = new Tiles(width, height);
//    	if(this.tiles == null) {
    		// Creating empty map
        	for (int y = 0; y < tiles.height; y++) {
        		for (int x = 0; x < tiles.width; x++) {
        			Tile t = new MyEditorTile(this, x, y, Blocks.stone, Blocks.air, Blocks.air);
        			tiles.set(x, y, t);
        		}
        	}
//    	} else {
//        	for (int y = 0; y < resized.height; y++) {
//        		for (int x = 0; x < resized.width; x++) {
//        			resized.set(x, y, tiles.get(x, y));
//    			}
//    		}
//    	}
    	
    	
    	this.tiles = tiles;
    	
    	renderer.resize(width, height);
    }
    
    public int width() {
		return width;
	}
    
    public int height() {
		return height;
	}
	
	public Tiles tiles() {
		return tiles;
	}
    
	@Override
	public void dispose() {
		renderer.dispose();
	}


	public boolean isLoading() {
		// TODO Auto-generated method stub
		return false;
	}

	public void addTileOp(long data) {
//        if(loading) return;

        if(currentOp == null) currentOp = new MyDrawOperation(this);
        currentOp.addOperation(data);

        renderer.updatePoint(TileOp.x(data), TileOp.y(data));
	}
	
	public void flushOp() {
        if(currentOp == null || currentOp.isEmpty()) return;
        stack.add(currentOp);
        currentOp = null;		
	}

	public Tile tile(short x, short y) {
		return tiles.getn(x, y);
	}

	public void load(Runnable run) {
		run.run();
		// TODO
	}

	public void undo() {
        if(stack.canUndo()) stack.undo();
	}

	public Object undosAbalible() {
		return stack.size();
	}
}
