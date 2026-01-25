package agzam4.utils.animationgen;

import agzam4.utils.code.Code;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.core.World;
import mindustry.game.Schematic;
import mindustry.game.Schematic.Stile;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.LogicDisplay;
import mindustry.world.blocks.logic.MemoryBlock;
import mindustry.world.blocks.logic.LogicBlock.LogicLink;

public class GifGenerator {

	
	public static Seq<DrawRect> process(Seq<int[][]> frames, int size) {
		int[][] current = new int[size][size];
		int w;

		Seq<DrawRect> rects = Seq.with();
		
		for (int fid = 0; fid < frames.size; fid++) {
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
			
		}
		return rects;
	}
	

	public static void scheme(Seq<int[][]> frames, LogicDisplay displayBlock) {
		Seq<DrawRect> rects = process(frames, displayBlock.displaySize);
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
		
		int range = World.toTile(logicBlock.range);
		float displayRange2 = (range+display.block.size)*(range+display.block.size);
		float range2 = range*range;

		int min = displayBlock.sizeOffset;
		int max = displayBlock.sizeOffset + displayBlock.size;
		
		for (int dy = -range; dy <= range; dy++) {
			for (int dx = -range; dx <= range; dx++) {
				if(dx == memory.x && dy == memory.y) continue;
				if(min < dx && dx < max && dy < min && dy > max) continue;
				if(Mathf.dst2(dx, dy) > displayRange2) continue;
				if(Mathf.dst2(dx, dy, memory.x, memory.y) > range2) continue;
				codes.add(new DrawCode(logicBlock, dx, dy));
			}
		}
		
		for (int i = 0; i < rects.size; i++) {
			var rect = rects.get(i);
			
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
		
//		for (int i = 0; i < tiles.size; i++) {
//			var stile = tiles.get(i);
//			
//			Code code = new Code();
//			code.getLink("#Display", 0);
//			code.jump("equal #Display null", -1);
//			
//		}
		
		tiles.addAll(display, memory);

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
			LogicLink mlink = new LogicLink(memory.x-x, memory.y-y, "agzamMod-display", false);
			Log.info("Code size: @", code.size());
			return new Stile(
					block, 
					x, y, 
					LogicBlock.compress(code.toString(), Seq.with(dlink, mlink)),
					(byte) 0
			);
			
		}


		public boolean accept(DrawRect rect) {
			return avalible() && batchSize+3 < maxbatchSize;
		}

		public boolean avalible() {
			return code.size()+6 < 999;
		}
		
		int lastRgb = 0;
		
		public void add(DrawRect drawRect) {
			if(drawRect.frame != lastframe) { // New frame, flush old
				mlogFlush();
				lastframe = drawRect.frame;
			}
			if(lastRgb != drawRect.rgb) {
				code.color(drawRect.rgb);
				batchSize++;
				lastRgb = drawRect.rgb;
			}
			code.drawRect(drawRect.x, drawRect.y, drawRect.w, drawRect.h);
			batchSize++;
			
			if(batchSize >= maxbatchSize) { // Prevents graphics buffer overflow
				code.drawflush("#Display");
				batchSize = 0;
			}
		}
		
		public void mlogFlush() {
			code.read("#Frame", "#Memory", 0);
			code.jump("notEqual #Frame " + lastframe, -1);
			code.drawflush("#Display");
			batchSize = 0;
		}
		
	}
	
}
