package agzam4.utils.animationgen;

public class DrawRect {

	/** Dimension **/
	public int x, y, w, h;
	/** Color of rect in rgba8888 **/
	public int rgb;
	/** Frame number, 0 - for first or static image **/
	public int frame;

	/** Logic display has limited operations so split frame on subframes **/
	public int sync;
	
	public DrawRect(int x, int y, int w, int h, int rgb) {
		this(x,y,w,h,rgb,0);
	}
	
	public DrawRect(int x, int y, int w, int h, int rgb, int frame) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.rgb = rgb;
		this.frame = frame;
	}
	
}
