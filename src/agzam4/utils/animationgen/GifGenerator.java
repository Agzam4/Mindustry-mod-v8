package agzam4.utils.animationgen;

import agzam4.io.GifIO;
import agzam4.utils.code.Code;
import arc.math.Mathf;
import arc.struct.IntSeq;
import arc.struct.IntSet;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.core.World;
import mindustry.game.Schematic;
import mindustry.game.Schematic.Stile;
import mindustry.logic.LExecutor;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.LogicDisplay;
import mindustry.world.blocks.logic.MemoryBlock;
import mindustry.world.blocks.logic.LogicBlock.LogicLink;

public class GifGenerator {

	public static boolean avalible() {
		return GifIO.avalible();
	}
	
	public static Seq<DrawRect> process(Seq<int[][]> frames, int size) {
		Seq<DrawRect> totalrects = Seq.with();
		int[][] current = new int[size][size];
		int w;

		int sync = 1;
		
		for (int fid = 0; fid < frames.size; fid++) {
			Seq<DrawRect> rects = Seq.with();
			
			var frame = frames.get(fid);
			for (int y = 0; y < size; y++) {
				for (int x = 0; x < size; x++) {
					if(frame[x][y] == current[x][y]) continue;
					int rgba8888 = frame[x][y];
					for (w = 0; w+x < size && rgba8888 == frame[x+w][y]; w++);
					DrawRect rect = new DrawRect(x, y, w, 1, rgba8888, fid);
					if((rgba8888 & 0xff) != 0) rects.add(rect);
					x += w-1;
				}
			}
			rects.sort((r1,r2)->Integer.compare(r1.rgb, r2.rgb));
			
			// Setting sub-frame indexes
			int buffer = 0;
			IntSet colors = new IntSet();
			
			for (int i = 0; i < rects.size; i++) {
				var rect = rects.get(i);
				if(buffer+1 > LExecutor.maxDisplayBuffer) { // Reseting buffer and changing sub-frame id
					sync++;
					buffer = 0;
				}
				rect.sync = sync;
				if(!colors.contains(rect.rgb)) buffer++; // change color operation
			}
			if(buffer > 0) sync++; // Flush current buffer
			
			totalrects.addAll(rects);
		}
		return totalrects;
	}
	

	public static void scheme(Seq<int[][]> frames, LogicDisplay displayBlock) {
		Seq<DrawRect> rects = process(frames, displayBlock.displaySize);
		if(rects == null) {
			Vars.ui.showErrorMessage("GIF processing error");
			return;
		}
		
		Seq<Stile> tiles = Seq.with();
		Seq<DrawCode> codes = Seq.with();
		
		Log.info("rects: @", rects.size);
		for (int i = 0; i < frames.size; i++) {
			final int f = i;
			Log.info("@ frame rects: @", i, rects.select(r -> r.frame == f).size);
		}

		MemoryBlock memoryBlock = (MemoryBlock) Blocks.memoryCell;
		LogicBlock logicBlock = (LogicBlock) Blocks.microProcessor;
		
		Stile display = new Stile(displayBlock, 0, 0, null, (byte) 0);
		Stile memory = new Stile(memoryBlock, 0, displayBlock.size + displayBlock.sizeOffset, null, (byte) 0);
		Stile player = new Stile(logicBlock, 0, -displayBlock.size - displayBlock.sizeOffset, null, (byte) 0);
		
		int range = World.toTile(logicBlock.range);
		float displayRange2 = (range+display.block.size)*(range+display.block.size);
		float range2 = range*range;

		int min = displayBlock.sizeOffset;
		int max = displayBlock.sizeOffset + displayBlock.size - 1;
		Log.info("limit: [@,@]", min, max);
		
		for (int dy = -range; dy <= range; dy++) {
			for (int dx = -range; dx <= range; dx++) {
				if(dx == memory.x && dy == memory.y) continue;
				if(dx == player.x && dy == player.y) continue;
				if(min <= dx && dx <= max && min <= dy && dy <= max) continue;
				if(Mathf.dst2(dx, dy) > displayRange2) continue;
				if(Mathf.dst2(dx, dy, memory.x, memory.y) > range2) continue;
				codes.add(new DrawCode(logicBlock, dx, dy));
			}
		}
		codes.sort((c1,c2) -> Float.compare(Mathf.dst2(c1.x, c1.y), Mathf.dst2(c2.x, c2.y)));
		
		IntSeq[] syncs = new IntSeq[frames.size];
		for (int i = 0; i < syncs.length; i++) syncs[i] = new IntSeq();
		
		for (int i = 0; i < rects.size; i++) {
			var rect = rects.get(i);
			syncs[rect.frame].add(rect.sync);
			
			var batched = codes.find(c -> c.accept(rect));
			if(batched != null) {
				batched.add(rect);
				continue;
			}

			// searching minimum frame, minimum draw buffer
			codes = codes.sort((c1,c2) -> {
				if(c1.lastframe == c2.lastframe) return c1.batchSize - c2.batchSize;
				return c1.lastframe - c2.lastframe;
			});
			
			var first = codes.find(c -> c.avalible());
			if(first == null) continue; // TODO: 0 check
			first.add(rect);
		}
		
		codes.each(c -> {
			var stile = c.stile(display, memory);
			if(stile == null) return;
			tiles.add(stile);
		});
		
		Code playerCode = new Code();
		playerCode.getLink("#Display", 0);
		playerCode.jump("equal #Display null", -1);
		playerCode.getLink("#Memory", 1);
		playerCode.jump("equal #Memory null", -1);
		playerCode.sleep(1);
		playerCode.markLast("mark-begin");
		
		for (int i = 0; i < syncs.length; i++) {
			IntSet frameSyncs = new IntSet();
			final int frame = i;
			syncs[i].each(sync -> {
				if(frameSyncs.contains(sync)) return;
				frameSyncs.add(sync);
				
				if(playerCode.size() + 15 > 999) return;

				// Buffer are empty
				playerCode.write(0, "#Memory", 1); // Lock
				playerCode.sensor("#Buffer", "@bufferSize", "#Display");
				playerCode.jump("notEqual #Buffer 0", -1);
				
				// If someone unlocked -> go back
				playerCode.read("#Lock", "#Memory", 1);
				playerCode.jump("notEqual #Lock 0", -4); 

//				playerCode.read("#Sync", "#Memory", 1);

				playerCode.write(0, "#Memory", 1); // Lock
				playerCode.write(sync, "#Memory", 0);
				playerCode.write(frame, "#Memory", 2);

				playerCode.read("#Lock", "#Memory", 1);
				playerCode.jump("equal #Lock 0", -1);
				
				// Wait for someone draws current sub-frame
//				playerCode.read("#Sync", "#Memory", 1);
//				playerCode.jump("#Sync lessThan " + sync, -1);
				
			});
//			playerCode.sleep(.25f);
		}
		playerCode.jump("mark-begin");

		player.config = LogicBlock.compress(playerCode.toString(), Seq.with(
				new LogicLink(display.x-player.x, display.y-player.y, "agzamMod-display", false),
				new LogicLink(memory.x-player.x, memory.y-player.y, "agzamMod-memory", false)
		));
		
//		for (int i = 0; i < tiles.size; i++) {
//			var stile = tiles.get(i);
//			
//			Code code = new Code();
//			code.getLink("#Display", 0);
//			code.jump("equal #Display null", -1);
//			
//		}
		
		tiles.addAll(display, memory, player);

		Schematic schematic = new Schematic(tiles, new StringMap(), 0, 0);
		Vars.control.input.useSchematic(schematic);		
		
	}
	
	
	private static class DrawCode {
		
		int lastframe = 0;
		
		private Code code = new Code();
		
		private int batchSize = 0;
		
		private final int maxbatchSize = LExecutor.maxGraphicsBuffer-1;
		
		LogicBlock block;
		int x, y;
		
		public DrawCode(LogicBlock block, int x, int y) {
//			Blocks.microProcessor.maxInstructionsPerTick
			this.block = block;
			this.x = x;
			this.y = y;
			code.getLink("#Display", 0);
			code.jump("equal #Display null", -1);
			code.getLink("#Memory", 1);
			code.jump("equal #Memory null", -1);
			
		}
		
		public Stile stile(Stile display, Stile memory) {
			if(code.size() <= 4) return null; 
			if(batchSize > 0) mlogFlush();
			
			LogicLink dlink = new LogicLink(display.x-x, display.y-y, "agzamMod-display", false);
			LogicLink mlink = new LogicLink(memory.x-x, memory.y-y, "agzamMod-memory", false);
			Log.info("Code size: @", code.size());
			return new Stile(
					block, 
					x, y, 
					LogicBlock.compress(code.toString(), Seq.with(dlink, mlink)),
					(byte) 0
			);
			
		}


		public boolean accept(DrawRect rect) {
			if(lastframe != rect.sync) return false;
			return avalible() && batchSize+3 < maxbatchSize;
		}

		public boolean avalible() {
			return code.size()+10 < 999;
		}
		
		int lastRgb = 0;
		
		public void add(DrawRect drawRect) {
			if(drawRect.sync != lastframe) { // New frame, flush old
				mlogFlush();
				lastframe = drawRect.sync;
			}
			if(lastRgb != drawRect.rgb) {
				code.color(drawRect.rgb);
				batchSize++;
				lastRgb = drawRect.rgb;
			}
			code.drawRect(drawRect.x, drawRect.y, drawRect.w, drawRect.h);
			batchSize++;
			
			if(batchSize >= maxbatchSize) { // Prevents graphics buffer overflow
				Log.info("Overflow");
				code.drawflush("#Display");
				batchSize = 0;
			}
		}
		
		public void mlogFlush() {
			if(lastframe > 0) {
				code.read("#Frame", "#Memory", 0);
				code.jump("notEqual #Frame " + lastframe, -1);
				code.drawflush("#Display");
			}
			code.write(1, "#Memory", 1);
			Log.info("(@,@): sync-@", x, y, lastframe);
			batchSize = 0;
		}
		
	}
	
}
