package agzam4.utils.animationgen;

public class DrawBatch {

	public final DrawRect[] rects;
	public final int frame;
	
	private int index = 0;

	public DrawBatch(int frame) {
		this(10, frame);
	}
	
	public DrawBatch(int size, int frame) {
		rects = new DrawRect[size];
		this.frame = frame;
	}
	
	public void add(DrawRect rect) {
		rects[index++] = rect;
	}
	
	public boolean full() {
		return index >= rects.length;
	}
	
}
