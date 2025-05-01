package agzam4.industry;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.TextureRegion;
import arc.util.Align;
import arc.util.Nullable;
import arc.util.pooling.Pools;
import mindustry.ui.Fonts;

public class OverlayLine {

	public @Nullable TextureRegion firstContent, secondContent;
	public @Nullable String before, firstString, secondString;
	
	private GlyphLayout 
			beforeLayout = Pools.obtain(GlyphLayout.class, GlyphLayout::new),
			firstLayout = Pools.obtain(GlyphLayout.class, GlyphLayout::new),
			secondLayout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
	
	Color color = null;

	public OverlayLine first(TextureRegion firstContent, String firstString) {
		this.firstContent = firstContent;
		return firstString(firstString);
	}
	
	public OverlayLine second(TextureRegion secondContent, String secondString) {
		this.secondContent = secondContent;
		return secondString(secondString);
	}
	
//	public OverlayLine firstContent(UnlockableContent firstContent) {
//		this.firstContent = firstContent;
//		return this;
//	}
//	
//	public OverlayLine secondContent(UnlockableContent secondContent) {
//		this.secondContent = secondContent;
//		return this;
//	}
	
	public OverlayLine before(String before) {
		this.before = before;
		if(before != null) beforeLayout.setText(Fonts.outline, before);
		return this;
	}
	
	public OverlayLine firstString(String firstString) {
		this.firstString = firstString;
		if(firstString != null) firstLayout.setText(Fonts.outline, firstString);
		return this;
	}
	
	public OverlayLine secondString(String secondString) {
		this.secondString = secondString;
		if(secondString != null) secondLayout.setText(Fonts.outline, secondString);
		return this;
	}

//	@Override
	public float width() {
		return (firstContent == null ? 0 : lineHeight())
				+(secondContent == null ? 0 : lineHeight())
				+ firstLayout.width + secondLayout.width + beforeLayout.width;
	}
	
	//	@Override
	public void draw(float x, final float y, float width) {
//		if(color != null) {
//			Draw.color(color);
//			Fill.rect(x+width/2f, y, width+lineHeight(), lineHeight());
//		}
		if(before != null) {
			drawText(before, x, y);
			x += beforeLayout.width;
		}
		if(firstContent != null) {
			drawContent(firstContent, x, y);
			x += lineHeight();
		}
		if(firstString != null) {
			drawText(firstString, x, y);
			x += firstLayout.width;
		}
		if(secondContent != null) {
			drawContent(secondContent, x, y);
			x += lineHeight();
		}
		if(secondString != null) {
			drawText(secondString, x, y);
		}
	}


	public void update() {
		if(before != null) {
			beforeLayout.setText(Fonts.outline, before);
		}
		if(firstString != null) {
			firstLayout.setText(Fonts.outline, firstString);
		}
		if(secondString != null) {
			secondLayout.setText(Fonts.outline, secondString);
		}
	}
	
//	public void draw(float x, float y) {}

//	public float width() {
//		return 0;
//	}

	public float lineHeight() {
		return Fonts.outline.getLineHeight();
	}
	
	public float textHeight() {
		return lineHeight()/2f;
	}

	public void drawContent(TextureRegion content, float x, float y) {
        Draw.blend(Blending.normal);
		if(color == null) Draw.color();
		else Draw.color(color);
		final float width = content.width*contentHeight()/content.height;
        Draw.rect(content, x + textHeight(), y + textHeight()/2f, width, contentHeight());
        Draw.blend();
		if(color != null) Draw.color();
	}

	private float contentHeight() {
		return lineHeight()*.75f;
	}

	public void drawText(String text, float x, float y) {
		Draw.color();
		Fonts.outline.draw(text, x, y + textHeight(), 0, Align.left, false);
	}
}
