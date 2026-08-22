package agzam4.procgen.display;

import arc.graphics.Pixmap;
import arc.math.Mathf;
import arc.struct.Seq;

public class DisplayGeneratorShapes {

	private static final int poolSize = 24;
	private static final int minArea = 4;
	private static final int minSides = 3, maxSides = 8;
	private static final int spotSamples = 4;
	private static final int cleanupThreshold = 60;
	private static final int maxCleanupRun = 16;
	private static final int cleanupRunColorTol = 120;

	public static Seq<Draw> decompose(Pixmap pixmap, int size) {
		return decompose(pixmap, size, 350);
	}

	public static Seq<Draw> decompose(Pixmap pixmap, int size, int iterations) {
		int[][] sr = new int[size][size], sg = new int[size][size], sb = new int[size][size];
		long avgR = 0, avgG = 0, avgB = 0;

		for(int y = 0; y < size; y++){
			for(int x = 0; x < size; x++){
				int rgba = pixmap.get(x*pixmap.width/size, y*pixmap.height/size);

		        int r = ((rgba & 0xff000000) >>> 24);
		        int g = ((rgba & 0x00ff0000) >>> 16);
		        int b = ((rgba & 0x0000ff00) >>> 8);
		        int a = ((rgba & 0x000000ff));
		        
				sr[x][y] = r;
				sg[x][y] = g;
				sb[x][y] = b;
				avgR += sr[x][y];
				avgG += sg[x][y];
				avgB += sb[x][y];
			}
		}

		int pixels = size*size;
		int bgR = (int)(avgR/pixels), bgG = (int)(avgG/pixels), bgB = (int)(avgB/pixels);

		int[][] cr = new int[size][size], cg = new int[size][size], cb = new int[size][size];
		for(int y = 0; y < size; y++){
			for(int x = 0; x < size; x++){
				cr[x][y] = bgR;
				cg[x][y] = bgG;
				cb[x][y] = bgB;
			}
		}

		Seq<Draw> drawn = new Seq<>();

		Rect base = new Rect();
		base.x = 0;
		base.y = 0;
		base.w = size;
		base.h = size;
		base.r = bgR;
		base.g = bgG;
		base.b = bgB;
		drawn.add(base);

		float startRadius = size*0.45f;
		for(int it = 0; it < iterations; it++){
			float progress = iterations <= 1 ? 0 : (float)it/(iterations - 1);
			float maxRadius = startRadius*(1f - progress) + 2f;

			Candidate best = null;
			for(int i = 0; i < poolSize; i++){
				Candidate c;
				if(i%2 == 0){
					c = randomCandidate(maxRadius, size, Mathf.random(size - 1), Mathf.random(size - 1));
				}else{
					int[] spot = hotSpot(sr, sg, sb, cr, cg, cb, size);
					c = randomCandidate(maxRadius, size, spot[0], spot[1]);
				}
				evaluate(c, sr, sg, sb, cr, cg, cb);
				if(c.improvement <= 0) continue;
				if(best == null || c.improvement > best.improvement) best = c;
			}

			if(best == null) continue;
			paint(best, cr, cg, cb);
			best.draw.r = best.fr;
			best.draw.g = best.fg;
			best.draw.b = best.fb;
			drawn.add(best.draw);
		}

//		cleanup(drawn, sr, sg, sb, cr, cg, cb, size);

		for(Draw d : drawn){
			d.flipY(size);
		}
		return drawn;
	}

	private static Candidate randomCandidate(float maxRadius, int size, int cx, int cy){
		Candidate c = new Candidate();
		int type = Mathf.random(2);

		if(type == 0){
			Rect r = new Rect();
			r.w = Mathf.random(2, Math.max(2, (int)maxRadius));
			r.h = Mathf.random(2, Math.max(2, (int)maxRadius));
			r.x = Mathf.clamp(cx - r.w/2, 0, size - r.w);
			r.y = Mathf.clamp(cy - r.h/2, 0, size - r.h);
			c.draw = r;
			c.n = 4;
			c.vx[0] = r.x;        c.vy[0] = r.y;
			c.vx[1] = r.x + r.w;  c.vy[1] = r.y;
			c.vx[2] = r.x + r.w;  c.vy[2] = r.y + r.h;
			c.vx[3] = r.x;        c.vy[3] = r.y + r.h;
		}else if(type == 1){
			Tri t = new Tri();
			t.xs = new int[3];
			t.ys = new int[3];
			float base = Mathf.random(360f);
			float rad = Mathf.random(2f, maxRadius);
			for(int k = 0; k < 3; k++){
				float ang = (base + k*120f + Mathf.random(-70f, 70f))*Mathf.degRad;
				float d = rad*Mathf.random(0.45f, 1.25f);
				t.xs[k] = (int)(cx + Mathf.cos(ang)*d);
				t.ys[k] = (int)(cy + Mathf.sin(ang)*d);
				c.vx[k] = t.xs[k];
				c.vy[k] = t.ys[k];
			}
			c.draw = t;
			c.n = 3;
		}else{
			Poly p = new Poly();
			p.x = cx;
			p.y = cy;
			p.radius = Mathf.random(2f, maxRadius);
			p.sides = Mathf.random(minSides, maxSides);
			p.rotation = Mathf.random(360f);
			c.draw = p;
			c.n = p.sides;
			for(int k = 0; k < p.sides; k++){
				float ang = (p.rotation + k*360f/p.sides)*Mathf.degRad;
				c.vx[k] = p.x + Mathf.cos(ang)*p.radius;
				c.vy[k] = p.y + Mathf.sin(ang)*p.radius;
			}
		}

		c.minX = size;
		c.maxX = 0;
		c.minY = size;
		c.maxY = 0;
		for(int i = 0; i < c.n; i++){
			c.minX = Math.min(c.minX, (int)c.vx[i]);
			c.maxX = Math.max(c.maxX, (int)c.vx[i]);
			c.minY = Math.min(c.minY, (int)c.vy[i]);
			c.maxY = Math.max(c.maxY, (int)c.vy[i]);
		}
		c.minX = Math.max(0, c.minX);
		c.maxX = Math.min(size - 1, c.maxX);
		c.minY = Math.max(0, c.minY);
		c.maxY = Math.min(size - 1, c.maxY);

		return c;
	}

	private static void evaluate(Candidate c, int[][] sr, int[][] sg, int[][] sb,
			int[][] cr, int[][] cg, int[][] cb){

		long sumR = 0, sumG = 0, sumB = 0;
		for(int y = c.minY; y <= c.maxY; y++){
			for(int x = c.minX; x <= c.maxX; x++){
				if(!contains(c, x, y)) continue;
				sumR += sr[x][y];
				sumG += sg[x][y];
				sumB += sb[x][y];
				c.area++;
			}
		}
		if(c.area < minArea) return;

		c.fr = (int)(sumR/c.area);
		c.fg = (int)(sumG/c.area);
		c.fb = (int)(sumB/c.area);

		long improve = 0;
		for(int y = c.minY; y <= c.maxY; y++){
			for(int x = c.minX; x <= c.maxX; x++){
				if(!contains(c, x, y)) continue;
				int before = Math.abs(cr[x][y] - sr[x][y])
					+ Math.abs(cg[x][y] - sg[x][y])
					+ Math.abs(cb[x][y] - sb[x][y]);
				int after = Math.abs(c.fr - sr[x][y])
					+ Math.abs(c.fg - sg[x][y])
					+ Math.abs(c.fb - sb[x][y]);
				improve += before - after;
			}
		}
		c.improvement = (int)Math.min(Integer.MAX_VALUE, improve);
	}

	private static void paint(Candidate c, int[][] cr, int[][] cg, int[][] cb){
		for(int y = c.minY; y <= c.maxY; y++){
			for(int x = c.minX; x <= c.maxX; x++){
				if(!contains(c, x, y)) continue;
				cr[x][y] = c.fr;
				cg[x][y] = c.fg;
				cb[x][y] = c.fb;
			}
		}
	}

	private static boolean contains(Candidate c, int px, int py){
		int sign = 0;
		for(int i = 0; i < c.n; i++){
			int j = (i + 1)%c.n;
			float cross = (px - c.vx[j])*(c.vy[i] - c.vy[j]) - (c.vx[i] - c.vx[j])*(py - c.vy[j]);
			if(cross == 0) continue;
			int s = cross > 0 ? 1 : -1;
			if(sign == 0) sign = s;
			else if(s != sign) return false;
		}
		return true;
	}

	private static int errAt(int x, int y, int[][] sr, int[][] sg, int[][] sb,
			int[][] cr, int[][] cg, int[][] cb){
		return Math.abs(cr[x][y] - sr[x][y])
			+ Math.abs(cg[x][y] - sg[x][y])
			+ Math.abs(cb[x][y] - sb[x][y]);
	}

	private static int[] hotSpot(int[][] sr, int[][] sg, int[][] sb,
			int[][] cr, int[][] cg, int[][] cb, int size){
		int bx = Mathf.random(size - 1), by = Mathf.random(size - 1);
		int best = errAt(bx, by, sr, sg, sb, cr, cg, cb);
		for(int i = 1; i < spotSamples; i++){
			int x = Mathf.random(size - 1), y = Mathf.random(size - 1);
			int e = errAt(x, y, sr, sg, sb, cr, cg, cb);
			if(e > best){
				best = e;
				bx = x;
				by = y;
			}
		}
		return new int[]{bx, by};
	}

	private static void cleanup(Seq<Draw> drawn, int[][] sr, int[][] sg, int[][] sb,
			int[][] cr, int[][] cg, int[][] cb, int size){
		for(int y = 0; y < size; y++){
			for(int x = 0; x < size; ){
				if(errAt(x, y, sr, sg, sb, cr, cg, cb) <= cleanupThreshold){
					x++;
					continue;
				}
				Rect r = new Rect();
				r.x = x;
				r.y = y;
				r.w = 1;
				r.h = 1;
				while(r.w < maxCleanupRun && x + r.w < size
						&& errAt(x + r.w, y, sr, sg, sb, cr, cg, cb) > cleanupThreshold
						&& Math.abs(sr[x + r.w][y] - sr[r.x][y]) + Math.abs(sg[x + r.w][y] - sg[r.x][y])
							+ Math.abs(sb[x + r.w][y] - sb[r.x][y]) <= cleanupRunColorTol){
					r.w++;
				}
				long sumR = 0, sumG = 0, sumB = 0;
				for(int i = 0; i < r.w; i++){
					sumR += sr[x + i][y];
					sumG += sg[x + i][y];
					sumB += sb[x + i][y];
				}
				r.r = (int)(sumR/r.w);
				r.g = (int)(sumG/r.w);
				r.b = (int)(sumB/r.w);
				for(int i = 0; i < r.w; i++){
					cr[x + i][y] = r.r;
					cg[x + i][y] = r.g;
					cb[x + i][y] = r.b;
				}
				drawn.add(r);
				x += r.w;
			}
		}
	}

	public static abstract class Draw {
		public int r, g, b;

		abstract void flipY(int size);
	}

	public static class Rect extends Draw {
		public int x, y, w, h;

		@Override
		void flipY(int size){
			y = size - y - h;
		}
	}

	public static class Tri extends Draw {
		public int[] xs, ys;

		@Override
		void flipY(int size){
			for(int k = 0; k < 3; k++){
				ys[k] = size - 1 - ys[k];
			}
		}
	}

	public static class Poly extends Draw {
		public float x, y;
		public int sides;
		public float radius, rotation;

		@Override
		void flipY(int size){
			y = size - 1 - y;
			rotation = -rotation;
		}
	}

	private static class Candidate {
		final float[] vx = new float[maxSides], vy = new float[maxSides];
		int n;
		Draw draw;

		int minX, minY, maxX, maxY;
		int area;
		int fr, fg, fb;
		int improvement = Integer.MIN_VALUE;
	}
}
