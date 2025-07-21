package agzam4.ui;

import arc.graphics.Color;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.util.Reflect;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

public class ModStyles {

	public static Color mobileBackground = new Color(1f, 1f, 1f);
	public static Color mobileChecked = new Color(1f, 1f, 1f);
	public static Color mobileHover = new Color(1f, 1f, 1f);
	public static Color mobileActive = new Color(1f, 1f, 1f);
	public static Color mobileDisabled = new Color(1f, 1f, 1f);

	public static TextButtonStyle mobileToggle, mobileButton;
	
	public static void init() {
		TextureRegionDrawable whiteui = (TextureRegionDrawable) Tex.whiteui;
		TextureRegionDrawable dMobileBackground = (TextureRegionDrawable) whiteui.tint(Pal.darkestGray);
		TextureRegionDrawable dMobileHover = (TextureRegionDrawable) whiteui.tint(Color.valueOf("454545"));
		TextureRegionDrawable dMobileActive = (TextureRegionDrawable) whiteui.tint(Color.valueOf("777777"));
		TextureRegionDrawable dMobileChecked = (TextureRegionDrawable) whiteui.tint(Pal.accent);
		TextureRegionDrawable dMobileDisabled = (TextureRegionDrawable) whiteui.tint(Pal.darkestestGray);
		
		mobileBackground = Reflect.get(dMobileBackground, "tint");
		mobileChecked = Reflect.get(dMobileChecked, "tint");
		mobileHover = Reflect.get(dMobileHover, "tint");
		mobileActive = Reflect.get(dMobileActive, "tint");
		mobileDisabled = Reflect.get(dMobileDisabled, "tint");
		
		mobileToggle = new TextButtonStyle() {{
            up = dMobileBackground;
	        over = dMobileHover;
	        down = dMobileActive;
	        checked = dMobileChecked;
	        disabled = dMobileDisabled;
	        
	        font = Fonts.outline;
	        fontColor = Color.white;
	        checkedFontColor = Pal.accent;
	        disabledFontColor = Color.gray;
	    }};
	    
	    mobileButton = new TextButtonStyle() {{
            up = dMobileBackground;
	        over = dMobileHover;
	        down = dMobileActive;
	        disabled = dMobileDisabled;
            
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
	    }};
	}
	
	public static void mobileAlpha(float a) {
		mobileBackground.a(a);
		mobileChecked.a(a);
		mobileHover.a(a);
		mobileActive.a(a);
		mobileDisabled.a(a);
	}
}
