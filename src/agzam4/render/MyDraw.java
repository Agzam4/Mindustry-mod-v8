package agzam4.render;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.geom.Position;
import arc.scene.ui.layout.Scl;
import arc.util.Align;
import arc.util.Time;
import arc.util.pooling.Pools;
import mindustry.graphics.Layer;
import mindustry.ui.Fonts;

public class MyDraw {

    public static void normal(TextureRegion region, Color color, float x, float y, float layer){
        normal(region, color, 1f, x, y, 0f, layer);
    }

    public static void normal(TextureRegion region, Color color, float alpha, float x, float y, float layer){
        normal(region, color, alpha, x, y, 0f, layer);
    }

    public static void normal(TextureRegion region, Color color, float alpha, float x, float y, float rotation, float layer){
        float pz = Draw.z();
        Draw.z(layer);
        Draw.color(color, alpha * color.a);
        Draw.blend(Blending.normal);
        Draw.rect(region, x, y, rotation);
        Draw.blend();
        Draw.color();
        Draw.z(pz);
    }
    
    public static void normal(TextureRegion region, float x, float y, float w, float h, float layer){
        float pz = Draw.z();
        Draw.z(layer);
        Draw.color(Color.white);
        Draw.blend(Blending.normal);
        Draw.rect(region, x, y, w, h, 0);
        Draw.blend();
        Draw.color();
        Draw.z(pz);
    }
    
    
    public static void additive(TextureRegion region, Color color, float alpha, float x, float y, float rotation, float layer){
        float pz = Draw.z();
        Draw.z(layer);
        Draw.color(color, alpha * color.a);
        Draw.blend(Blending.additive);
        Draw.rect(region, x, y, rotation);
        Draw.blend();
        Draw.color();
        Draw.z(pz);
    }

    @Deprecated
	public static GlyphLayout text(String text, float x, float y, boolean rect) {
		return text(text, x, y, Align.center, rect);
	}

    @Deprecated
	public static GlyphLayout text(String text, float x, float y, float alpha, boolean rect) {
		return text(text, x, y, alpha, Align.center, rect);
	}

    @Deprecated
	public static GlyphLayout text(String text, float x, float y, float alpha, int align, boolean rect) {
		return text(text, x, y, alpha, align, textHeight, rect);
	}

    @Deprecated
	public static GlyphLayout text(String text, float x, float y, float alpha, int align, int textHeight, boolean rect) {
		Draw.z(Layer.playerName);
//		float z = Drawf.text();

		Font font = Fonts.outline;

		GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);

		font.setUseIntegerPositions(false);
		font.getData().setScale(0.25f / Scl.scl(1f));
		layout.setText(font, text);
		if(rect) {
			Draw.color(0f, 0f, 0f, 0.3f);
			Fill.rect(x, y + textHeight - layout.height / 2, layout.width + 2, layout.height + 3);
		}
		Draw.color();
		Draw.alpha(alpha);
		font.setColor(1, 1, 1, alpha);
		font.draw(text, x, y + textHeight, 0, align, false);
		Draw.color();
		
		font.getData().setScale(1f);
		
		return layout;
	}

	public static final int textHeight = 11;
	public static final int align = Align.center;

	public static GlyphLayout textColor(String text, float x, float y, float r, float g, float b, float scale, int align) {
		Draw.z(Layer.playerName);
//		float z = Drawf.text();

		Font font = Fonts.outline;

		GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);

		font.setUseIntegerPositions(false);
		font.getData().setScale(0.25f*scale / Scl.scl(1f));
		layout.setText(font, text);
		
		Draw.color();
		Draw.alpha(1f);
		font.setColor(r, g, b, 1);
		font.draw(text, x, y + textHeight*scale, 0, align, false);
		Draw.color();
		
		font.getData().setScale(0.25f / Scl.scl(1f));
		return layout;
	}

	public static GlyphLayout drawTooltip(String text, float x, float y) {
		Draw.z(Layer.playerName);
//		float z = Drawf.text();

		Font font = Fonts.outline;

		GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);

		font.setUseIntegerPositions(false);
		font.getData().setScale(0.25f / Scl.scl(1f));
		font.getData().setLineHeight(textHeight*2f * Scl.scl(1f));
		layout.setText(font, text);
		
		y += layout.height;
		
		Draw.color(0f, 0f, 0f, 0.5f);
		Fill.rect(x + layout.width / 2, y + textHeight - layout.height / 2, layout.width + 4, layout.height + 3);
		
		Draw.color();
		Draw.alpha(1f);
		font.setColor(1, 1, 1, 1);
		font.draw(text, x, y + textHeight, 0, Align.left, false);
		Draw.color();
		
		font.getData().setScale(1f);
		
		return layout;
	}

	public static void rotatingArcs(Position center, float rad, float speed) {
		if(center == null) return;
		float statAngle = Time.time * speed;//*speed*.36f)%360; // TODO: check
		for (int angle = 0; angle < 360; angle+=90) {
			Lines.arc(center.getX(), center.getY(), rad, .2f, statAngle+angle);
		}		
	}
	

	public static GlyphLayout textOld(String text, float x, float y, float size, int align, Font font) {
		GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
		font.setUseIntegerPositions(false);

		font.getData().setScale(1f / Scl.scl(1f));
		layout.setText(font, text);
		
		float scale = size / layout.height;
		
		font.getData().setScale(scale / Scl.scl(1f));
		layout.setText(font, text);
		
		font.setColor(Draw.getColor());

		y += layout.height/2f;//layout.height/2f;
		
		if(Align.isTop(align)) {
			y += layout.height/2f;
		}
		if(Align.isBottom(align)) {
			y -= layout.height/2f;
		}
		font.draw(text, x, y, 0, align, false);
		font.getData().setScale(0.25f / Scl.scl(1f));
		return layout;
	}
	
	public static GlyphLayout text(String text, float x, float y, float size, int align, Font font) {
		GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
		font.setUseIntegerPositions(false);

		font.getData().setScale(1f / Scl.scl(1f));
		
		float scale = size / font.getCapHeight();
		
		font.getData().setScale(scale / Scl.scl(1f));
		layout.setText(font, text);
		
		font.setColor(Draw.getColor());

		y += size/2f;//layout.height/2f;
		
		if(Align.isTop(align)) {
			y += size/2f;
		}
		if(Align.isBottom(align)) {
			y -= size/2f;
		}
		font.draw(text, x, y, 0, align, false);
		font.getData().setScale(1f);
		return layout;
	}
	
}
