package agzam4.industry;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import mindustry.ctype.UnlockableContent;

public interface IndustryElement {

	public void draw(float x, float y);
	public void draw(float x, float y, float width);

	public void line(TextureRegion c1, String s1);
	
	public void line(TextureRegion c1, String s1, TextureRegion c2, String s2);

	public void line(String before, TextureRegion c1, String s1);

	public void line(String before);
	
	public void line(String before, TextureRegion c1, String s1, TextureRegion c2, String s2);
	

	public void line(UnlockableContent c1, String s1);
	
	public void line(UnlockableContent c1, String s1, UnlockableContent c2, String s2);

	public void line(String before, UnlockableContent c1, String s1);

	public void line(String before, UnlockableContent c1, String s1, UnlockableContent c2, String s2);
	
	public void color(Color color);

	public float height();

}
