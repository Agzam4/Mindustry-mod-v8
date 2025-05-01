package agzam4;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import arc.graphics.g2d.TextureRegion;
import arc.graphics.g2d.Font.Glyph;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Scaling;
import mindustry.ui.Fonts;

public class MyFonts {

    private static final int LOG2_PAGE_SIZE = 9;
    private static final int PAGE_SIZE = 1 << LOG2_PAGE_SIZE;
//    private static final int PAGES = 0x10000 / PAGE_SIZE;

    private static Seq<TextureRegion> regions;
    
	private static char lastFreeCharacter = Character.MIN_VALUE;

	private static ObjectMap<String, String> stringIcons = new ObjectMap<>();

	public static final char noneEmoji = createEmoji(UnitTextures.none, "none");

//	public static String emoji(Seq<TextureRegion> region) {
//		if(content.hasEmoji()) return content.emoji();
//        return stringIcons.get(content.name, noneEmoji + "");
//	}
	
	public static char createEmoji(TextureRegion region, String name) {
        for (char ch = lastFreeCharacter; ch < Character.MAX_VALUE; ch++) {
			if(Fonts.outline.getData().hasGlyph(ch)) continue;
			lastFreeCharacter = ch;
			createEmoji(region, name, ch);
			return ch;
		}
        return ' ';
	}
	
	public static void createEmoji(TextureRegion region, String name, int ch) {
        int size = (int)(Fonts.def.getData().lineHeight/Fonts.def.getData().scaleY);
        Vec2 out = Scaling.fit.apply(region.width, region.height, size, size);
        Glyph glyph = new Glyph();
        glyph.id = ch;
        glyph.srcX = 0;
        glyph.srcY = 0;
        glyph.width = (int)out.x;
        glyph.height = (int)out.y;
        glyph.u = region.u;
        glyph.v = region.v2;
        glyph.u2 = region.u2;
        glyph.v2 = region.v;
        glyph.xoffset = 0;
        glyph.yoffset = -size;
        glyph.xadvance = size;
        glyph.kerning = null;
        glyph.fixedWidth = true;
        glyph.page = 0;
        stringIcons.put(name, ((char)ch) + "");
        Seq.with(Fonts.def, Fonts.outline).each(f -> {
        	f.getData().setGlyph(ch, glyph);
        	f.getData().setGlyphRegion(glyph, region);
        });
	}

	@SuppressWarnings("unchecked")
	public static void load() {
		Field[] allFields = Fonts.class.getDeclaredFields();
		
		for (Field field : allFields) {
			if (Modifier.isPrivate(field.getModifiers())) {
				field.setAccessible(true);
				try {
					if(field.getName().equals("stringIcons")) {
						stringIcons = (ObjectMap<String, String>) field.get(Fonts.class);
						Log.info("field: @", field.getName());
					} 
					if(field.getName().equals("regions")) {
						regions = (Seq<TextureRegion>) field.get(Fonts.class);
						lastFreeCharacter = (char) (regions.size * PAGE_SIZE);
						Log.info("field: @", field.getName());
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Log.err(e);
				}
			}
		}		
	}
}
