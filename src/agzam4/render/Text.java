package agzam4.render;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.scene.ui.layout.Scl;
import arc.util.Align;
import mindustry.ui.Fonts;

public class Text {

	private static Font font = Fonts.outline;

	public static void at(String text, float x, float y) {
		at(text, x, y, Align.center);
	}
	
	public static void at(String text, float x, float y, int align) {
		font.setColor(Draw.getColor());
		y += font.getLineHeight()/2f;
		if(Align.isTop(align)) y += font.getLineHeight()/2f;
		if(Align.isBottom(align)) y -= font.getLineHeight()/2f;
		
		font.draw(text, x, y, 0, align, false);
	}

	public static void font(Font font) {
		Text.font = font;
	}

	public static void size(float fontSize) {
		if(fontSize < .01f) fontSize = .01f;
		font.getData().setScale(0.25f / Scl.scl(1f) * fontSize);
	}

	public static void size() {
		font.getData().setScale(1f);
	}
	
}
