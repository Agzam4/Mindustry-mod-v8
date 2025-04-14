package agzam4;

import static mindustry.Vars.pathfinder;
import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.QuadTree;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Reflect;
import mindustry.Vars;
import mindustry.ai.Pathfinder;
import mindustry.ai.types.DefenderAI;
import mindustry.ai.types.FlyingAI;
import mindustry.ai.types.GroundAI;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.entities.units.AIController;
import mindustry.entities.units.UnitController;
import mindustry.game.EventType.TapEvent;
import mindustry.game.EventType.TileChangeEvent;
import mindustry.gen.Call;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Tile;

@Deprecated
public class EnemiesPaths {

//	private int todo;
//
//    public static final int
//        fieldCore = 0;
//
//    public static final int
//        costGround = 0,
//        costLegs = 1,
//        costNaval = 2;
//    
//    static Thread thread;
//    
//    static boolean needupdated = false;
//	
//	static {
////        Events.on(WorldLoadEvent.class, event -> {
////            //don't bother setting up paths unless necessary
////            if(state.rules.waveTeam.needsFlowField() && net.client()){
////            	thread = Reflect.get(Vars.pathfinder, "thread");
////            	Reflect.invoke(Vars.pathfinder, "preloadPath", new Object[] {Vars.pathfinder.getField(state.rules.waveTeam, costGround, fieldCore)}, Flowfield.class);
////            	Reflect.invoke(Vars.pathfinder, "preloadPath", new Object[] {Vars.pathfinder.getField(state.rules.waveTeam, costLegs, fieldCore)}, Flowfield.class);
////                
////            	Log.debug("Preloading ground enemy flowfield.");
////
////                //preload water on naval maps
////                if(spawner.getSpawns().contains(t -> t.floor().isLiquid)){
////                	Reflect.invoke(Vars.pathfinder, "preloadPath", new Object[] {Vars.pathfinder.getField(state.rules.waveTeam, costNaval, fieldCore)}, Flowfield.class);
////                    Log.debug("Preloading naval enemy flowfield.");
////                }
////
////            }
////
////            start();
////        });
//        Events.on(TileChangeEvent.class, event -> needupdated = true);
//	}
//	
//
////    /** Starts or restarts the pathfinding thread. */
////    private static void start(){
////        stop();
////
////        thread = new Thread(Vars.pathfinder, "Pathfinder");
////        thread.setPriority(Thread.MIN_PRIORITY);
////        thread.setDaemon(true);
////        thread.start();
////    }
////
////    /** Stops the pathfinding thread. */
////    private static void stop(){
////        if(thread != null){
////            thread.interrupt();
////            thread = null;
////        }
////        TaskQueue queue = Reflect.get(Vars.pathfinder, "queue");
////        queue.clear();
////    }
//	
//	public static void draw() {
////		if(!ModWork.setting("show-enemies-path")) return; TODO
//		if(target == null) return;
//		if(!ClientPathfinder.enabled) return;
//		Draw.z(Layer.blockAdditive);
//		Draw.color(target.team.color);
//		
//		Position pos = target;
//		Lines.stroke(2);
//        for (int i = 0; i < path.size; i++) {
//			Lines.line(pos.getX(), pos.getY(), path.get(i).getX(), path.get(i).getY(), true);
//			pos = path.get(i);
//		}
//		
//	}
//    public static @Nullable Unit target;
//	static @Nullable Tile lastTile = null;
//	
//	static Seq<Position> path = new Seq<>();
//	static int updates = 0;
//	
//	public static void update() {
//		if(!ClientPathfinder.enabled) return;
//		if(target == null) {
//			needupdated = false;
//			return;
//		}
//		updates++;
//		if(lastTile == target.tileOn() && updates < 60*2 || !needupdated) return;
//		needupdated = false;
//		path.clear();
//		updates = 0;
//		
//		lastTile = target.tileOn();
//		if(lastTile == null) return;
//		
//		if(!target.isCommandable()) {
//			if(target.controller() == null) return;
//			UnitController controller = target.controller();
//			if(controller instanceof GroundAI) {
//				path.add(lastTile);
//		        for (int i = 0; i < 1000; i++) {
//		        	lastTile = Vars.net.client() ? 
//		        			ClientPathfinder.instance.getTargetTile(lastTile, ClientPathfinder.instance.getField(target.team, target.pathType(), Pathfinder.fieldCore))
//		        			: pathfinder.getTargetTile(lastTile, pathfinder.getField(target.team, target.pathType(), Pathfinder.fieldCore));
//		    		if(lastTile == null) return;
//		        	path.add(lastTile);
//				}
//				lastTile = target.tileOn();
//			} else if(controller instanceof FlyingAI || controller instanceof DefenderAI) {
//				try {
//					path.add(target);
//					Teamc t = Reflect.get(AIController.class, controller, "target");
//					if(t != null) {
//						path.add(t);
//					}
//				} catch (Exception e) {
//					Log.info("error: @", e.getMessage());
//				}
//			}
//			
//		}
////		AIController
//
//	}
//
//	private static Seq<Unit> tmpUnits = new Seq<Unit>(false);
//	
//    public static Seq<Unit> selectedCommandUnits(float x, float y){
//        QuadTree<Unit> tree = Vars.state.rules.waveTeam.data().tree();
//        tmpUnits.clear();
//        float rad = 8f;
//        tree.intersect(x - rad/2f, y - rad/2f, rad*2f, rad*2f, tmpUnits);
//        return tmpUnits;
//    }
//    
//
//	public static void tap(TapEvent e) {
//		if(!ClientPathfinder.enabled) return;
//		float x = Core.input.mouseWorldX();
//		float y = Core.input.mouseWorldY();
//		selectedCommandUnits(x, y);
//		if(tmpUnits.size <= 0) {
////			target = null;
//			return;
//		}
//		needupdated = target == tmpUnits.get(0);
//		target = tmpUnits.get(0);
//		lastTile = null;
//	}

}
