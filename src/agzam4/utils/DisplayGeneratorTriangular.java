package agzam4.utils;

import java.awt.Polygon;
import java.util.Comparator;

import agzam4.ModWork;
import arc.graphics.Pixmap;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Threads;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.game.Schematic.Stile;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.LogicBlock.LogicLink;

public class DisplayGeneratorTriangular {

	private static int triangles = 300;
	private static int initialCount = 50;
	private static int octaves = 5;
	
	// 80x80
	// 176x176
//	static int[][] rgbs;
	static int[][] rs,gs,bs;
	static int[][] srs,sgs,sbs;

	
	public static void show() {
		Vars.platform.showMultiFileChooser(file -> {
			BaseDialog dialog = new BaseDialog(ModWork.bungle("dialog.utils.select-display"));
			dialog.titleTable.remove();
			dialog.closeOnBack();

			dialog.cont.pane(p -> {
				p.defaults().left();
				
				Table t = new Table();
				p.add(t).row();

	            t.button(Blocks.logicDisplay.emoji() + " " + Blocks.logicDisplay.localizedName, Styles.defaultt, () -> {
	            	Threads.thread(() -> {
		                Pixmap pixmap = new Pixmap(file);
		                create(pixmap, 80);
		                pixmap.dispose();
	            	});
	            	dialog.hide();
	        		PlayerUtils.hide();
	            }).growX().pad(10).padBottom(4).wrapLabel(false).row();

	            t.button(Blocks.largeLogicDisplay.emoji() + " " + Blocks.largeLogicDisplay.localizedName, Styles.defaultt, () -> {
	            	Threads.thread(() -> {
	            		Pixmap pixmap = new Pixmap(file);
	                	create(pixmap, 176);
		                pixmap.dispose();
	            	});
	            	dialog.hide();
	        		PlayerUtils.hide();
	            }).growX().pad(10).padBottom(4).wrapLabel(false).row();
	            
	            t.button("@back", Styles.defaultt, () -> {
	            	dialog.hide();
	        		PlayerUtils.hide();
	            }).growX().pad(10).padBottom(4).wrapLabel(false).row();
			});
			dialog.show();
		}, "png", "jpg", "jpeg");
	}

	public static void create(Pixmap pixmap, int size) {
		int[][] rgbs = new int[size][size];
		srs = new int[size][size];
		sgs = new int[size][size];
		sbs = new int[size][size];
		
		Seq<Draw> ts = new Seq<>();
		
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				rgbs[x][y] = pixmap.get(x*pixmap.width/size, y*pixmap.height/size);
			}
		}
		int[] avv = new int[3];
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				int rgb = rgbs[x][y];
				avv[0] += (rgb >> 16) & 0xFF;
				avv[1] += (rgb >> 8) & 0xFF;
				avv[2] += (rgb >> 0) & 0xFF;

				srs[x][y] = (rgb >> 16) & 0xFF;
				sgs[x][y] = (rgb >> 8) & 0xFF;
				sbs[x][y] = (rgb >> 0) & 0xFF;
			}
		}
		avv[0] /= size*size;
		avv[1] /= size*size;
		avv[2] /= size*size;

		rs = new int[size][size];
		gs = new int[size][size];
		bs = new int[size][size];
		
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
//				int rgb = rgbs[x][y];
				rs[x][y] = avv[0];//(rgb >> 16) & 0xFF;
				gs[x][y] = avv[1];//(rgb >> 8) & 0xFF;
				bs[x][y] = avv[2];//(rgb >> 0) & 0xFF;
			}
		}
		
		for (int it = 0; it < triangles; it++) {
			
			Seq<ShapesSimilarity> cool = new Seq<>();
			
			Polygon max = null;
			int maxSimilarity = Integer.MAX_VALUE;
			
			for (int i = 0; i < initialCount ; i++) {
				Polygon t = new Polygon(
						new int[] {Mathf.random(size),Mathf.random(size),Mathf.random(size)}, 
						new int[] {Mathf.random(size),Mathf.random(size),Mathf.random(size)}, 3);
				
				ShapesSimilarity similarity = step(t, size);
				if(similarity == null) continue;
				cool.add(similarity);
				
				if(similarity.similarity < maxSimilarity) {
					max = similarity.shape;
					maxSimilarity = similarity.similarity;
				}
			}
			cool.sort(new Comparator<ShapesSimilarity>() {
				@Override
				public int compare(ShapesSimilarity o1, ShapesSimilarity o2) {
					return o1.similarity - o2.similarity;
				}
			});
			while (cool.size > 10) {
				cool.remove(cool.size-1);
			}
			
			// creating same
			for (int octave = 0; octave < octaves; octave++) {
				Seq<ShapesSimilarity> coolestOfCool = new Seq<>();
				for (ShapesSimilarity s : cool) {
					coolestOfCool.add(s);
					int[] xpoints = s.shape.xpoints;
					int[] ypoints = s.shape.ypoints;
					int minX = Math.min(Math.min(xpoints[0], xpoints[1]), xpoints[2]);
					int maxX = Math.max(Math.max(xpoints[0], xpoints[1]), xpoints[2]);
					int minY = Math.min(Math.min(ypoints[0], ypoints[1]), ypoints[2]);
					int maxY = Math.max(Math.max(ypoints[0], ypoints[1]), ypoints[2]);

					if(maxX <= minX) maxX = size;
					if(maxY <= minY) maxY = size;
					
					for (int i = 0; i < 10; i++) {
						Polygon t = new Polygon(
								new int[] {Mathf.random(minX, maxX),Mathf.random(minX, maxX),Mathf.random(minX, maxX)}, 
								new int[] {Mathf.random(minY, maxY),Mathf.random(minY, maxY),Mathf.random(minY, maxY)}, 3);
						ShapesSimilarity similarity = step(t, size);
						if(similarity == null) continue;

						coolestOfCool.add(similarity);
					}
				}
				coolestOfCool.sort(new Comparator<ShapesSimilarity>() {
					@Override
					public int compare(ShapesSimilarity o1, ShapesSimilarity o2) {
						return o1.similarity - o2.similarity;
					}
				});
				while (coolestOfCool.size > 10) {
					coolestOfCool.remove(coolestOfCool.size-1);
				}
				cool = coolestOfCool;
			}
			
			if(max != null) {
				Polygon t = cool.get(0).shape;//max;
				Color c = avv(t, size);

				for (int y = 0; y < size; y++) {
					for (int x = 0; x < size; x++) {
						if(outTriangle(x, y, t.xpoints, t.ypoints)) continue;
						rs[x][y] = c.r;
						gs[x][y] = c.g;
						bs[x][y] = c.b;
					}
				}
				ts.add(new Draw(t, c));
			}
			
		}
		
		
		Seq<Code> codes = new Seq<Code>();

		final int maxCount = 999-3;
		
		Code code = new Code();
		code.getLink("#Display", 0);
		code.jump("equal #Display null", -1);
		code.drawClear(avv[0], avv[1], avv[2], 255);
		int count = maxCount;
		
		for (int i = 0; i < ts.size; i++) {
			Draw draw = ts.get(i);
			Polygon t = draw.shape;

			if(count <= 3) {
				if(code != null) {
					codes.add(code);
				}
				code = new Code();
				code.getLink("#Display", 0);
				code.jump("equal #Display null", -1);
				count = maxCount;
			}
			
			code.color(draw.color.r, draw.color.g, draw.color.b, 255);
			count--;
			code.drawTriangle(t.xpoints, t.ypoints);
			count--;
			code.drawflush("#Display");
			count--;
		}
//		for (int y = 0; y < size; y++) {
//			for (int x = 0; x < size; x++) {
//			}
//		}
		if(code != null) codes.add(code);
		
		Code nCode = null;
		codes.add(nCode);
		
		Seq<Stile> seq = new Seq<Schematic.Stile>(); // LogicBlock.compress(code, links)
		
		int bsize = size == 80 ? 2 : 4;
		int d = size == 80 ? 0 : -1;
		int wh = bsize;
		
		int index = 0;
		
		for (int bs = bsize; bs < 100; bs++) {
			for (int y = -bs-d; y <= bs; y++) {
				for (int x = -bs-d; x <= bs; x++) {
					if(x > -bs-d && y > -bs-d && x < bs && y < bs) continue;
					if(index >= codes.size) {
						wh = bs;
						break;
					}
					Code c = codes.get(index++);
					if(c == null) {
						seq.add(new Stile(Blocks.message, x, y, "[gold]Auto generated images processor\n[lightgray]Agzam's mod [lime]test-drive-build", (byte) 0));
					} else {
						LogicLink link = new LogicLink(-x, -y, "agzamMod-delivery-autolink-" + index, false);
						seq.add(new Stile(Blocks.microProcessor, x, y, 
								LogicBlock.compress(c.toString(), new Seq<>(new LogicLink[] {link})), (byte) 0));
					}
				}
				if(index >= codes.size) break;
			}
			if(index >= codes.size) break;
		}
		wh += bsize;
		seq.add(new Stile(size == 80 ? Blocks.logicDisplay : Blocks.largeLogicDisplay, 0, 0, null, (byte) 0));
		
		Schematic schematic = new Schematic(seq, new StringMap(), wh+1, wh+1);
		
		Vars.control.input.useSchematic(schematic);
	}
	
    private static Color avv(Polygon polygon, int size) {
		int[] avv = new int[3];
		int count = 0;
		int minX = Math.min(Math.min(polygon.xpoints[0], polygon.xpoints[1]), polygon.xpoints[2]);
		int maxX = Math.max(Math.max(polygon.xpoints[0], polygon.xpoints[1]), polygon.xpoints[2]);
		int minY = Math.min(Math.min(polygon.ypoints[0], polygon.ypoints[1]), polygon.ypoints[2]);
		int maxY = Math.max(Math.max(polygon.ypoints[0], polygon.ypoints[1]), polygon.ypoints[2]);
		if(maxX >= size) maxX = maxX-1;
		if(maxY >= size) maxY = maxY-1;
		for (int y = minY; y < maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				if(outTriangle(x, y, polygon.xpoints, polygon.ypoints)) continue;
				avv[0] += rs[x][y];
				avv[1] += gs[x][y];
				avv[2] += bs[x][y];
				count++;
			}
		}
		if(count == 0) count = 1;
		avv[0] /= count;
		avv[1] /= count;
		avv[2] /= count;
		return new Color(avv[0], avv[1], avv[2]);
	}
	
	private static ShapesSimilarity step(Polygon polygon, int size) {
		int[] avv = new int[3];
		int count = 0;
		int l = Math.min(Math.min(polygon.xpoints[0], polygon.xpoints[1]), polygon.xpoints[2]);
		int r = Math.max(Math.max(polygon.xpoints[0], polygon.xpoints[1]), polygon.xpoints[2]);
		int t = Math.min(Math.min(polygon.ypoints[0], polygon.ypoints[1]), polygon.ypoints[2]);
		int b = Math.max(Math.max(polygon.ypoints[0], polygon.ypoints[1]), polygon.ypoints[2]);
		if(r >= size) r = size-1;
		if(b >= size) b = size-1;
		for (int y = t; y <= b; y++) {
			for (int x = l; x <= r; x++) {
				if(outTriangle(x, y, polygon.xpoints, polygon.ypoints)) continue;
				avv[0] += srs[x][y];
				avv[1] += sgs[x][y];
				avv[2] += sbs[x][y];
				count++;
			}
		}
		if(count == 0) return null;
		avv[0] /= count;
		avv[1] /= count;
		avv[2] /= count;
		
		int diffsum = 0;
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				if(outTriangle(x, y, polygon.xpoints, polygon.ypoints)) {
					int diff = 0;
					diff += Math.abs((srs[x][y] - rs[x][y]));
					diff += Math.abs((sgs[x][y] - gs[x][y]));
					diff += Math.abs((sbs[x][y] - bs[x][y]));
					diffsum += diff;
					continue;
				}
				int diff = 0;
				diff += Math.abs(srs[x][y] - avv[0]);
				diff += Math.abs(sgs[x][y] - avv[1]);
				diff += Math.abs(sbs[x][y] - avv[2]);
				diffsum += diff;
			}
		}
		return new ShapesSimilarity(polygon, diffsum);
	}

	static class Draw {
		Polygon shape;
		Color color;
		
		public Draw(Polygon shape, Color color) {
			this.shape = shape;
			this.color = color;
		}
	}

	static class ShapesSimilarity {
		Polygon shape;
		int similarity;
		
		public ShapesSimilarity(Polygon shape, int similarity) {
			this.shape = shape;
			this.similarity = similarity;
		}
	}

	static class Color {
		int r, g, b;
		
		public Color(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}
	}

	static boolean outTriangle(int x, int y, int[] xs, int[] ys) {
		int d1 = sign(x,y, xs[0],ys[0], xs[1],ys[1]);
		int d2 = sign(x,y, xs[1],ys[1], xs[2],ys[2]);
		int d3 = sign(x,y, xs[2],ys[2], xs[0],ys[0]);
		return (((d1 < 0) || (d2 < 0) || (d3 < 0)) && ((d1 > 0) || (d2 > 0) || (d3 > 0)));
	}
	
	static int sign(int x0, int y0, int x1, int y1, int x2, int y2) {
	    return (x0 - x2) * (y1 - y2) - (x1 - x2) * (y0 - y2);
	}

}
