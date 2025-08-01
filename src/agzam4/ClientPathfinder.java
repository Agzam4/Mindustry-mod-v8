package agzam4;

@Deprecated
public class ClientPathfinder implements Runnable {

	@Override
	public void run() {}

//	public static ClientPathfinder instance;
//	public static boolean enabled;
//	
//	public static void init() {
//		instance = new ClientPathfinder();
//	}
//
//    private static final long maxUpdate = Time.millisToNanos(8);
//    private static final int updateFPS = 60;
//    private static final int updateInterval = 1000 / updateFPS;
//
//    /** cached world size */
//    static int wwidth, wheight;
//
//    static final int impassable = -1;
//
//    public static final int
//        fieldCore = 0;
//
//    @SuppressWarnings("unchecked")
//	public static final Seq<Prov<Flowfield>> fieldTypes = Seq.with(
//        EnemyCoreField::new
//    );
//
//    public static final int
//        costGround = 0,
//        costLegs = 1,
//        costNaval = 2;
//
//    public static final Seq<PathCost> costTypes = Seq.with(
//        //ground
//        (team, tile) ->
//            (PathTile.allDeep(tile) || ((PathTile.team(tile) == team && !PathTile.teamPassable(tile)) || PathTile.team(tile) == 0) && PathTile.solid(tile)) ? impassable : 1 +
//            PathTile.health(tile) * 5 +
//            (PathTile.nearSolid(tile) ? 2 : 0) +
//            (PathTile.nearLiquid(tile) ? 6 : 0) +
//            (PathTile.deep(tile) ? 6000 : 0) +
//            (PathTile.damages(tile) ? 30 : 0),
//
//        //legs
//        (team, tile) ->
//            PathTile.legSolid(tile) ? impassable : 1 +
//            (PathTile.deep(tile) ? 6000 : 0) + //leg units can now drown
//            (PathTile.solid(tile) ? 5 : 0),
//
//        //water
//        (team, tile) ->
//            (!PathTile.liquid(tile) ? 6000 : 1) +
//            PathTile.health(tile) * 5 +
//            (PathTile.nearGround(tile) || PathTile.nearSolid(tile) ? 14 : 0) +
//            (PathTile.deep(tile) ? 0 : 1) +
//            (PathTile.damages(tile) ? 35 : 0)
//    );
//
//    /** tile data, see PathTileStruct - kept as a separate array for threading reasons */
//    int[] tiles = new int[0];
//
//    /** maps team, cost, type to flow field*/
//    Flowfield[][][] cache;
//    /** unordered array of path data for iteration only. DO NOT iterate or access this in the main thread. */
//    Seq<Flowfield> threadList = new Seq<>(), mainList = new Seq<>();
//    /** handles task scheduling on the update thread. */
//    TaskQueue queue = new TaskQueue();
//    /** Current pathfinding thread */
//    @Nullable Thread thread;
//    IntSeq tmpArray = new IntSeq();
//
//    public ClientPathfinder(){
//        clearCache();
//
//        Events.on(WorldLoadEvent.class, event -> {
//            stop();
//
//            //reset and update internal tile array
//            tiles = new int[world.width() * world.height()];
//            wwidth = world.width();
//            wheight = world.height();
//            threadList = new Seq<>();
//            mainList = new Seq<>();
//            clearCache();
//
//            for(int i = 0; i < tiles.length; i++){
//                Tile tile = world.tiles.geti(i);
//                tiles[i] = packTile(tile);
//            }
//
//            //don't bother setting up paths unless necessary
//            if(state.rules.waveTeam.needsFlowField() && net.client() && enabled){
//                preloadPath(getField(state.rules.waveTeam, costGround, fieldCore));
//
//                //preload water on naval maps
//                if(spawner.getSpawns().contains(t -> t.floor().isLiquid)){
//                    preloadPath(getField(state.rules.waveTeam, costNaval, fieldCore));
//                }
//
//            }
//
//            start();
//        });
//
//        Events.on(ResetEvent.class, event -> stop());
//
//        Events.on(TileChangeEvent.class, event -> updateTile(event.tile));
//
//        //remove nearSolid flag for tiles
//        Events.on(TilePreChangeEvent.class, event -> {
//            Tile tile = event.tile;
//
//            if(tile.solid()){
//                for(int i = 0; i < 4; i++){
//                    Tile other = tile.nearby(i);
//                    if(other != null){
//                        //other tile needs to update its nearSolid to be false if it's not solid and this tile just got un-solidified
//                        if(!other.solid()){
//                            boolean otherNearSolid = false;
//                            for(int j = 0; j < 4; j++){
//                                Tile othernear = other.nearby(i);
//                                if(othernear != null && othernear.solid()){
//                                    otherNearSolid = true;
//                                    break;
//                                }
//                            }
//                            int arr = other.array();
//                            //the other tile is no longer near solid, remove the solid bit
//                            if(!otherNearSolid && tiles.length > arr){
//                                tiles[arr] &= ~(PathTile.bitMaskNearSolid);
//                            }
//                        }
//                    }
//                }
//            }
//        });
//    }
//
//    private void clearCache(){
//        cache = new Flowfield[256][5][5];
//    }
//
//    /** Packs a tile into its internal representation. */
//    public int packTile(Tile tile){
//        boolean nearLiquid = false, nearSolid = false, nearLegSolid = false, nearGround = false, solid = tile.solid(), allDeep = tile.floor().isDeep();
//
//        for(int i = 0; i < 4; i++){
//            Tile other = tile.nearby(i);
//            if(other != null){
//                Floor floor = other.floor();
//                boolean osolid = other.solid();
//                if(floor.isLiquid) nearLiquid = true;
//                //TODO potentially strange behavior when teamPassable is false for other teams?
//                if(osolid && !other.block().teamPassable) nearSolid = true;
//                if(!floor.isLiquid) nearGround = true;
//                if(!floor.isDeep()) allDeep = false;
//                if(other.legSolid()) nearLegSolid = true;
//
//                //other tile is now near solid
//                if(solid && !tile.block().teamPassable){
//                    tiles[other.array()] |= PathTile.bitMaskNearSolid;
//                }
//            }
//        }
//
//        int tid = tile.getTeamID();
//
//        return PathTile.get(
//            tile.build == null || !solid || tile.block() instanceof CoreBlock ? 0 : Math.min((int)(tile.build.health / 40), 80),
//            tid == 0 && tile.build != null && state.rules.coreCapture ? 255 : tid, //use teamid = 255 when core capture is enabled to mark out derelict structures
//            solid,
//            tile.floor().isLiquid,
//            tile.legSolid(),
//            nearLiquid,
//            nearGround,
//            nearSolid,
//            nearLegSolid,
//            tile.floor().isDeep(),
//            tile.floor().damageTaken > 0.00001f,
//            allDeep,
//            tile.block().teamPassable
//        );
//    }
//
//    public int get(int x, int y){
//        return tiles[x + y * wwidth];
//    }
//
//    /** Starts or restarts the pathfinding thread. */
//    private void start(){
//        stop();
//        if(!enabled) return;
//        if(!net.client()) return;
//
//        thread = new Thread(this, "Pathfinder");
//        thread.setPriority(Thread.MIN_PRIORITY);
//        thread.setDaemon(true);
//        thread.start();
//    }
//
//    /** Stops the pathfinding thread. */
//    private void stop(){
//        if(thread != null){
//            thread.interrupt();
//            thread = null;
//        }
//        queue.clear();
//    }
//
//    /** Update a tile in the internal pathfinding grid.
//     * Causes a complete pathfinding recalculation. Main thread only. */
//    public void updateTile(Tile tile){
//        if(!enabled) return;
//        if(!net.client()) return;
//
//        tile.getLinkedTiles(t -> {
//            int pos = t.array();
//            if(pos < tiles.length){
//                tiles[pos] = packTile(t);
//            }
//        });
//
//        //can't iterate through array so use the map, which should not lead to problems
//        for(Flowfield path : mainList){
//            if(path != null){
//                synchronized(path.targets){
//                    path.updateTargetPositions();
//                }
//            }
//        }
//
//        //mark every flow field as dirty, so it updates when it's done
//        queue.post(() -> {
//            for(Flowfield data : threadList){
//                data.dirty = true;
//            }
//        });
//    }
//
//    /** Thread implementation. */
//    @Override
//    public void run(){
//        while(true){
//            if(!net.client()) return;
//            if(!enabled) return;
//            try{
//
//                if(state.isPlaying()){
//                    queue.run();
//
//                    //each update time (not total!) no longer than maxUpdate
//                    for(Flowfield data : threadList){
//
//                        //if it's dirty and there is nothing to update, begin updating once more
//                        if(data.dirty && data.frontier.size == 0){
//                            updateTargets(data);
//                            data.dirty = false;
//                        }
//
//                        updateFrontier(data, maxUpdate);
//                    }
//                }
//
//                try{
//                    Thread.sleep(updateInterval);
//                }catch(InterruptedException e){
//                    //stop looping when interrupted externally
//                    return;
//                }
//            }catch(Throwable e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public Flowfield getField(Team team, int costType, int fieldType){
//        if(cache[team.id][costType][fieldType] == null){
//            Flowfield field = fieldTypes.get(fieldType).get();
//            field.team = team;
//            field.cost = costTypes.get(costType);
//            field.targets.clear();
//            field.getPositions(field.targets);
//
//            cache[team.id][costType][fieldType] = field;
//            queue.post(() -> registerPath(field));
//        }
//        return cache[team.id][costType][fieldType];
//    }
//
//    /** Gets next tile to travel to. Main thread only. */
//    public @Nullable Tile getTargetTile(Tile tile, Flowfield path){
//        if(tile == null) return null;
//
//        //uninitialized flowfields are not applicable
//        if(!path.initialized){
//            return tile;
//        }
//
//        //if refresh rate is positive, queue a refresh
//        if(path.refreshRate > 0 && Time.timeSinceMillis(path.lastUpdateTime) > path.refreshRate){
//            path.lastUpdateTime = Time.millis();
//
//            tmpArray.clear();
//            path.getPositions(tmpArray);
//
//            synchronized(path.targets){
//                //make sure the position actually changed
//                if(!(path.targets.size == 1 && tmpArray.size == 1 && path.targets.first() == tmpArray.first())){
//                    path.updateTargetPositions();
//
//                    //queue an update
//                    queue.post(() -> updateTargets(path));
//                }
//            }
//        }
//
//        //use complete weights if possible; these contain a complete flow field that is not being updated
//        int[] values = path.hasComplete ? path.completeWeights : path.weights;
//        int apos = tile.array();
//        int value = values[apos];
//
//        Tile current = null;
//        int tl = 0;
//        for(Point2 point : Geometry.d8){
//            int dx = tile.x + point.x, dy = tile.y + point.y;
//
//            Tile other = world.tile(dx, dy);
//            if(other == null) continue;
//
//            int packed = world.packArray(dx, dy);
//
//            if(values[packed] < value && (current == null || values[packed] < tl) && path.passable(packed) &&
//            !(point.x != 0 && point.y != 0 && (!path.passable(world.packArray(tile.x + point.x, tile.y)) || !path.passable(world.packArray(tile.x, tile.y + point.y))))){ //diagonal corner trap
//                current = other;
//                tl = values[packed];
//            }
//        }
//
//        if(current == null || tl == impassable || (path.cost == costTypes.items[costGround] && current.dangerous() && !tile.dangerous())) return tile;
//
//        return current;
//    }
//
//    /** Increments the search and sets up flow sources. Does not change the frontier. */
//    private void updateTargets(Flowfield path){
//
//        //increment search, but do not clear the frontier
//        path.search++;
//
//        synchronized(path.targets){
//            //add targets
//            for(int i = 0; i < path.targets.size; i++){
//                int pos = path.targets.get(i);
//
//                path.weights[pos] = 0;
//                path.searches[pos] = path.search;
//                path.frontier.addFirst(pos);
//            }
//        }
//    }
//
//    private void preloadPath(Flowfield path){
//        path.updateTargetPositions();
//        registerPath(path);
//        updateFrontier(path, -1);
//    }
//
//    /**
//     * TODO wrong docs
//     * Created a new flowfield that aims to get to a certain target for a certain team.
//     * Pathfinding thread only.
//     */
//    private void registerPath(Flowfield path){
//        path.lastUpdateTime = Time.millis();
//        path.setup(tiles.length);
//
//        threadList.add(path);
//
//        //add to main thread's list of paths
//        Core.app.post(() -> mainList.add(path));
//
//        //fill with impassables by default
//        for(int i = 0; i < tiles.length; i++){
//            path.weights[i] = impassable;
//        }
//
//        //add targets
//        for(int i = 0; i < path.targets.size; i++){
//            int pos = path.targets.get(i);
//            path.weights[pos] = 0;
//            path.frontier.addFirst(pos);
//        }
//    }
//
//    /** Update the frontier for a path. Pathfinding thread only. */
//    private void updateFrontier(Flowfield path, long nsToRun){
//        boolean hadAny = path.frontier.size > 0;
//        long start = Time.nanos();
//
//        int counter = 0;
//
//        while(path.frontier.size > 0){
//            int tile = path.frontier.removeLast();
//            if(path.weights == null) return; //something went horribly wrong, bail
//            int cost = path.weights[tile];
//
//            //pathfinding overflowed for some reason, time to bail. the next block update will handle this, hopefully
//            if(path.frontier.size >= world.width() * world.height()){
//                path.frontier.clear();
//                return;
//            }
//
//            if(cost != impassable){
//                for(Point2 point : Geometry.d4){
//
//                    int dx = (tile % wwidth) + point.x, dy = (tile / wwidth) + point.y;
//
//                    if(dx < 0 || dy < 0 || dx >= wwidth || dy >= wheight) continue;
//
//                    int newPos = tile + point.x + point.y * wwidth;
//                    int otherCost = path.cost.getCost(path.team.id, tiles[newPos]);
//
//                    if((path.weights[newPos] > cost + otherCost || path.searches[newPos] < path.search) && otherCost != impassable){
//                        path.frontier.addFirst(newPos);
//                        path.weights[newPos] = cost + otherCost;
//                        path.searches[newPos] = (short)path.search;
//                    }
//                }
//            }
//
//            //every N iterations, check the time spent - this prevents extra calls to nano time, which itself is slow
//            if(nsToRun >= 0 && (counter++) >= 200){
//                counter = 0;
//                if(Time.timeSinceNanos(start) >= nsToRun){
//                    return;
//                }
//            }
//        }
//
//        //there WERE some things in the frontier, but now they are gone, so the path is done; copy over latest data
//        if(hadAny && path.frontier.size == 0){
//            System.arraycopy(path.weights, 0, path.completeWeights, 0, path.weights.length);
//            path.hasComplete = true;
//        }
//    }
//
//    public static class EnemyCoreField extends Flowfield{
//        @Override
//        protected void getPositions(IntSeq out){
//            for(Building other : indexer.getEnemy(team, BlockFlag.core)){
//                out.add(other.tile.array());
//            }
//
//            //spawn points are also enemies.
//            if(state.rules.waves && team == state.rules.defaultTeam){
//                for(Tile other : spawner.getSpawns()){
//                    out.add(other.array());
//                }
//            }
//        }
//    }
//
//    public static class PositionTarget extends Flowfield{
//        public final Position position;
//
//        public PositionTarget(Position position){
//            this.position = position;
//            this.refreshRate = 900;
//        }
//
//        @Override
//        public void getPositions(IntSeq out){
//            out.add(world.packArray(World.toTile(position.getX()), World.toTile(position.getY())));
//        }
//    }
//
//    /**
//     * Data for a flow field to some set of destinations.
//     * Concrete subclasses must specify a way to fetch costs and destinations.
//     */
//    public static abstract class Flowfield{
//        /** Refresh rate in milliseconds. Return any number <= 0 to disable. */
//        protected int refreshRate;
//        /** Team this path is for. Set before using. */
//        protected Team team = Team.derelict;
//        /** Function for calculating path cost. Set before using. */
//        protected PathCost cost = costTypes.get(costGround);
//        /** Whether there are valid weights in the complete array. */
//        protected volatile boolean hasComplete;
//        /** If true, this flow field needs updating. This flag is only set to false once the flow field finishes and the weights are copied over. */
//        protected boolean dirty = false;
//
//        /** costs of getting to a specific tile */
//        public int[] weights;
//        /** search IDs of each position - the highest, most recent search is prioritized and overwritten */
//        public int[] searches;
//        /** the last "complete" weights of this tilemap. */
//        public int[] completeWeights;
//
//        /** search frontier, these are Pos objects */
//        IntQueue frontier = new IntQueue();
//        /** all target positions; these positions have a cost of 0, and must be synchronized on! */
//        final IntSeq targets = new IntSeq();
//        /** current search ID */
//        int search = 1;
//        /** last updated time */
//        long lastUpdateTime;
//        /** whether this flow field is ready to be used */
//        boolean initialized;
//
//        void setup(int length){
//            this.weights = new int[length];
//            this.searches = new int[length];
//            this.completeWeights = new int[length];
//            this.frontier.ensureCapacity((length) / 4);
//            this.initialized = true;
//        }
//
//        public boolean hasCompleteWeights(){
//            return hasComplete && completeWeights != null;
//        }
//
//        public void updateTargetPositions(){
//            targets.clear();
//            getPositions(targets);
//        }
//
//        protected boolean passable(int pos){
//            int amount = cost.getCost(team.id, instance.tiles[pos]);
//            //edge case: naval reports costs of 6000+ for non-liquids, even though they are not technically passable
//            return amount != impassable && !(cost == costTypes.get(costNaval) && amount >= 6000);
//        }
//
//        /** Gets targets to pathfind towards. This must run on the main thread. */
//        protected abstract void getPositions(IntSeq out);
//    }
//
//    public interface PathCost{
//        int getCost(int team, int tile);
//    }
    
}
