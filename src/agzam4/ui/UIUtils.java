package agzam4.ui;

import arc.graphics.Color;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import mindustry.gen.Tex;

public class UIUtils {

	public static Drawable background = ((TextureRegionDrawable)Tex.whiteui).tint(.2f, .2f, .2f, 1f);
	public static Drawable gray = ((TextureRegionDrawable)Tex.whiteui).tint(Color.valueOf("777777"));
	
}
