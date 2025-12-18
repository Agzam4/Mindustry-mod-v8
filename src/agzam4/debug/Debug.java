package agzam4.debug;

import agzam4.ModWork;
import mindustry.Vars;

public class Debug {

	public static boolean debug = ModWork.settingDef("debug", false);

	public static void enable() {
		debug = true;
		ModWork.setting("debug", true);
	}

	public static void disable() {
		debug = false;
		ModWork.setting("debug", false);
	}
	
	
	public static void info(Object object) {
		new ObjectInspector(object).show();
	}
	
	public static void init() {
		try {
			Vars.mods.getScripts().context.evaluateString(Vars.mods.getScripts().scope,
					"var mod = Vars.mods.getMod(\"agzam4mod\");\n"
					+ "var get = (pkg) => mod.loader.loadClass(pkg).newInstance();\n"
					+ "var AgzamDebug = get(\"agzam4.debug.Debug\")\n"
					+ "var AgzamUI = get(\"agzam4.ui.MobileUI\")", "main.js", 0);
		} catch (Exception e) {
			Vars.ui.showErrorMessage(e.getMessage());
		}

//		Team.sharded.rules().unitBuildSpeedMultiplier = .2f;
//		Team.sharded.rules().unitCostMultiplier = 5f;
//		Team.sharded.rules().unitDamageMultiplier = 5f;
//		Team.sharded.rules().unitHealthMultiplier = 5f;
//		Team.sharded.rules().unitCrashDamageMultiplier = 5f;
//		Groups.unit.each(u -> {if(u.id%5 != 0)u.kill();});
		
		
//		Team.crux.rules().infiniteResources = true;
//		Team.crux.rules().unitCrashDamageMultiplier = .1f;
//		Team.crux.rules().unitHealthMultiplier = .25f;
		
		
//		Vars.state.rules.spawns.clear()

//		Call.setRules(Vars.state.rules);
//		Team.crux.cores().each(c -> c.items.add(Items.fissileMatter, 99999));
//		Vars.state.rules.bannedBlocks.add(Blocks.scorch, Blocks.lancer, Blocks.arc, Blocks.parallax, Blocks.fuse, Blocks.meltdown)
//		Events.on(mindustry.game.EventType.ModContentLoadEvent.class, e -> {
//			exportColors();
//		});
	}
	
	public void testname() throws Exception {
		
//		Call.adminRequest(Groups.player.find(p -> p.name.equals("NiathWalker")), Packets.AdminAction.trace, null);
//		PlayerListFragment
//		Admin sDialog
	}
	// AgzamDebug.exportColors()
/*
	public static void exportColors() {
		int res = 3;
		int size = 0;
		
		for (Block b : Vars.content.blocks()) {
			size += b.size*b.size;
		}
//		BufferedImage pal = new BufferedImage(size, res*res, BufferedImage.TYPE_INT_ARGB);
		Pixmap pal = new Pixmap(size, res*res);
		int index = 0;
//		Log.info("pal size: @", pal);
		ObjectMap<Pixmap, Pixmap> maps = new ObjectMap<Pixmap, Pixmap>();

		for (Block b : Vars.content.blocks()) {
			TextureRegion region = b.uiIcon;
			if(region == null) {
				Log.info("region is null: @", b);
				return;
			}
			PixmapRegion t = Core.atlas.getPixmap(region);
			if(!maps.containsKey(t.pixmap)) {
				PixmapIO.writePng(Fi.get("debug/#pixmap_" + maps.size + ".png"), t.pixmap);
				maps.put(t.pixmap, t.pixmap);
			}
//			region.texture.p
			t.get(index, index);
			int rx = t.x;
			int ry = t.y;
			int rw = t.width;
			int rh = t.height;
			Log.info("@ region @;@ @x@", b.name, rx,ry,rw,rh);
			
			int rsize = res*b.size;
			Pixmap debug = new Pixmap(rsize, rsize);
			
			for (int dy = 0; dy < rsize; dy++) {
				int y1 = dy*t.height/rsize;
				int y2 = (dy+1)*t.height/rsize;
				for (int dx = 0; dx < rsize; dx++) {
					int x1 = dx*t.width/rsize;
					int x2 = (dx+1)*t.width/rsize;
					
					ObjectMap<Integer, Counter> colors = new ObjectMap<Integer, Debug.Counter>();
//					Log.info("@ sub: @->@ @->@", b.name, x1,x2,y1,y2);
					
					float cr = 0, cg = 0, cb = 0, ca = 0;
					float count = 0;

					for (int py = y1; py < y2; py++) {
						for (int px = x1; px < x2; px++) {
							Color c = new Color(t.getRaw(px, py));
							cr += c.r;
							cg += c.g;
							cb += c.b;
							ca += c.a;
							count++;
						}
					}

					cr /= count;
					cg /= count;
					cb /= count;
					ca /= count;
					
					int rgba = Color.rgba8888(cr, cg, cb, ca);
					int ind = index++;
					
					debug.setRaw(dx, dy, rgba); // t.getRaw(dx*t.width/rsize, dy*t.height/rsize)
					pal.setRaw(ind/9, ind%9, rgba);
				}
			}
			PixmapIO.writePng(Fi.get("debug/" + b.name + ".png"), debug);
			debug.dispose();
			
		}
		
//		Log.info("saving");
		PixmapIO.writePng(Fi.get("pal.png"), pal);
		
	}
//	
	private static class Counter {
		
		int count = 0;
		
		public void inc() {
			count++;
		}
		
	}
	//*/
}
