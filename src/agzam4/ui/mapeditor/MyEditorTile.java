package agzam4.ui.mapeditor;

import arc.func.Prov;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.TileOp;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.OverlayFloor;
import mindustry.world.modules.ItemModule;
import mindustry.world.modules.LiquidModule;
import mindustry.world.modules.PowerModule;

public class MyEditorTile extends Tile {

	private MapLayer layer;
	
	boolean selected = false;
	
    public MyEditorTile(MapLayer layer, int x, int y, Block floor, Block overlay, Block wall) {
        super(x,y);
    	this.layer = layer;
        this.floor = floor.asFloor();
        this.overlay = overlay.asFloor();
        this.block = wall;

        //update entity and create it if needed
        changeBuild(Team.derelict, wall::newBuilding, 0);
        changed();
    }

    @Override
    public void setFloor(Floor type){
        if(skip()){
            super.setFloor(type);
            return;
        }

//        if(type instanceof OverlayFloor){
//            //don't place on liquids
//            if(floor.hasSurface() || !type.needsSurface){
//                setOverlayID(type.id);
//            }
//            return;
//        }
//
//        if(floor == type && overlayID() == 0) return;
//        if(overlayID() != 0) op(OpType.overlay, overlayID());
//        if(floor != type) op(OpType.floor, floor.id);

        this.floor = type;
        this.overlay = (Floor)Blocks.air;
    }

    @Override
    public boolean isEditorTile(){
        return true;
    }

    @Override
    public void setBlock(Block type, Team team, int rotation, Prov<Building> entityprov){
        if(skip()){
            super.setBlock(type, team, rotation, entityprov);
            return;
        }

        if(this.block == type && (build == null || build.rotation == rotation)){
            update();
            return;
        }

//        if(!isCenter()){
//        	MyEditorTile cen = (MyEditorTile)build.tile;
//            cen.op(OpType.rotation, (byte)build.rotation);
//            cen.op(OpType.team, (byte)build.team.id);
//            cen.op(OpType.block, block.id);
//            update();
//        }else{
//            if(build != null) op(OpType.rotation, (byte)build.rotation);
//            if(build != null) op(OpType.team, (byte)build.team.id);
//            op(OpType.block, block.id);
//
//        }

        super.setBlock(type, team, rotation, entityprov);
    }

    @Override
    public void setTeam(Team team){
        if(skip()){
            super.setTeam(team);
            return;
        }

        if(getTeamID() == team.id) return;
//        op(OpType.team, (byte)getTeamID());
        super.setTeam(team);

        getLinkedTiles(t -> layer.renderer.updatePoint(t.x, t.y));
    }

    @Override
    public void setOverlay(Block overlay){
        if(skip()){
            super.setOverlay(overlay);
            return;
        }

        if(!floor.hasSurface() && overlay.asFloor().needsSurface && (overlay instanceof OreBlock || !floor.supportsOverlay)) return;
        if(overlay() == overlay) return;
//        op(OpType.overlay, this.overlay.id);
        super.setOverlay(overlay);
    }

    @Override
    protected void fireChanged(){
        if(skip()){
            super.fireChanged();
        }else{
            update();
        }
    }

    @Override
    protected void firePreChanged(){
        if(skip()){
            super.firePreChanged();
        }else{
            update();
        }
    }

    @Override
    public void recache(){
        if(skip()){
            super.recache();
        }
    }

    @Override
    protected void changed(){
        if(Vars.state.isGame()){
            super.changed();
        }
    }

    @Override
    protected void changeBuild(Team team, Prov<Building> entityprov, int rotation){
        if(skip()){
            super.changeBuild(team, entityprov, rotation);
            return;
        }

        build = null;

        if(block == null) block = Blocks.air;
        if(floor == null) floor = (Floor)Blocks.air;

        Block block = block();

        if(block.hasBuilding()){
            build = entityprov.get().init(this, team, false, rotation);
            if(block.hasItems) build.items = new ItemModule();
            if(block.hasLiquids) build.liquids = new LiquidModule();
            if(block.hasPower) build.power = new PowerModule();
        }
    }

    private void update() {
    	layer.renderer.updatePoint(x, y);
    }

    private boolean skip() {
        return Vars.state.isGame() || layer.isLoading() || Vars.world.isGenerating();
    }

//    private void op(OpType type, short value) {
//    	layer.addTileOp(TileOp.get(x, y, (byte)type.ordinal(), value));
//    }
}
