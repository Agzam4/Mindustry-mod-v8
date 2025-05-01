package agzam4.industry;

import agzam4.MyDraw;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.ctype.UnlockableContent;
import mindustry.ui.Fonts;

public class BuildTooltip implements IndustryElement {

	Seq<OverlayLine> lines = new Seq<>();
	
	private int line = 0;

	public float padX, padY;

	public void rebuild() {
		line = 0;
	}

	@Override
	public void line(String before, TextureRegion c1, String s1, TextureRegion c2, String s2) {
		if(line >= lines.size) lines.add(new OverlayLine().before(before).first(c1, s1).second(c2, s2));
		else lines.get(line).before(before).first(c1, s1).second(c2, s2);
		line++;
		color(null);
	}

	private static final @Nullable TextureRegion nullable = null;
	
	@Override
	public void line(String before) {
		line(before, nullable, null, null, null);
	}
	
	@Override
	public void line(String before, TextureRegion c1, String s1) {
		line(before, c1, s1, null, null);
	}
	
	@Override
	public void line(TextureRegion c1, String s1, TextureRegion c2, String s2) {
		line(null, c1, s1, c2, s2);
	}

	@Override
	public void line(TextureRegion c1, String s1) {
		line(null, c1, s1, null, null);
	}

	@Override
	public void line(UnlockableContent c1, String s1) {
		line(c1.uiIcon, s1);
	}

	@Override
	public void line(UnlockableContent c1, String s1, UnlockableContent c2, String s2) {
		line(c1.uiIcon, s1, c2.uiIcon, s2);
	}

	@Override
	public void line(String before, UnlockableContent c1, String s1) {
		line(before, c1.uiIcon, s1);
	}

	@Override
	public void line(String before, UnlockableContent c1, String s1, UnlockableContent c2, String s2) {
		line(before, c1.uiIcon, s1, c2.uiIcon, s2);
	}

	@Override
	public void color(Color color) {
		if(line-1 >= 0 && line-1 < lines.size) lines.get(line-1).color = color;
	}
	
	@Override
	public float height() {
		return line * Fonts.outline.getLineHeight();
	}

	@Override
	public void draw(float x, float y) {
		Fonts.outline.setUseIntegerPositions(false);
		Fonts.outline.getData().setScale(0.25f / Scl.scl(1f));
		Fonts.outline.getData().setLineHeight(MyDraw.textHeight*2f * Scl.scl(1f));
		draw(x, y, width());
	}
	
	
	@Override
	public void draw(float x, float y, float width) {
		x += 4;
		
		final float lineHeight = Fonts.outline.getLineHeight();
		float height = lineHeight*line;
		
		
		Draw.color(0f, 0f, 0f, 0.5f);
		Fill.rect(x + width/2f, y + height/2f + lineHeight*2 + 1, width + 4 + padX, height + 2 + padY);
		
		Draw.color();

		x += 2;
		y += Fonts.outline.getLineHeight();
		
		for (int i = line-1; i >= 0; i--) {
			y += lineHeight;
			lines.get(i).draw(x-2, y+2, width);
		}
	}

	public float width() {
		float width = 0;
		for (int i = 0; i < line; i++) {
			OverlayLine l = lines.get(i);
			l.update();
			width = Math.max(l.width(), width);
		}
		return width;
	}

	public boolean isEmpty() {
		return line == 0;
	}

}
