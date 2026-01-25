package agzam4.utils;

import agzam4.io.GifIO;
import agzam4.utils.animationgen.GifGenerator;
import agzam4.utils.code.Code;
import arc.files.Fi;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.struct.StringMap;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.game.Schematic.Stile;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.LogicBlock.LogicLink;
import mindustry.world.blocks.logic.LogicDisplay;

public class DisplayGenerator {

	// 80x80
	// 176x176
	
	public static void show() {
		Cons<Fi> cons = file -> {
			BaseDialog dialog = new BaseDialog(Bungle.dialog("utils.select-display"));
			dialog.title.setColor(Color.white);
			dialog.titleTable.remove();
			dialog.closeOnBack();

			dialog.cont.pane(p -> {
				p.defaults().left();
				
				Table t = new Table();
				p.add(t).row();

	            t.button(Blocks.logicDisplay.emoji() + " " + Blocks.logicDisplay.localizedName, Styles.defaultt, () -> {
	                create(file, 80);
	            	dialog.hide();
	            }).growX().pad(10).padBottom(4).wrapLabel(false).row();

	            t.button(Blocks.largeLogicDisplay.emoji() + " " + Blocks.largeLogicDisplay.localizedName, Styles.defaultt, () -> {
	                create(file, 176);
	            	dialog.hide();
	            }).growX().pad(10).padBottom(4).wrapLabel(false).row();
	            
	            t.button("@back", Styles.defaultt, () -> {
	            	dialog.hide();
	        		PlayerUtils.hide();
	            }).growX().pad(10).padBottom(4).wrapLabel(false).row();
			});
			dialog.show();
		};
		
		if(GifGenerator.avalible()) Vars.platform.showMultiFileChooser(cons, "png", "jpg", "jpeg", "gif");
		else Vars.platform.showMultiFileChooser(cons, "png", "jpg", "jpeg", "gif");
	}

	private static void create(Fi file, int size) {
    	if(file.extension().equals("gif")) {
    		try {
        		LogicDisplay display = (LogicDisplay) (size == 80 ? Blocks.logicDisplay : Blocks.largeLogicDisplay);
        		GifGenerator.scheme(GifIO.readGifFrames(file, display.displaySize), display);
        		PlayerUtils.hide();	
			} catch (Throwable e) {
				Vars.ui.showException(e);
			}	
    		return;
    	}
    	
        Pixmap pixmap = new Pixmap(file);
        create(pixmap, 80);
        pixmap.dispose();
		PlayerUtils.hide();		
	}
	

	public static void create(Pixmap pixmap, int size) {
		int rgb[][] = new int[size][size];
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				rgb[x][y] = pixmap.get(x*pixmap.width/size, y*pixmap.height/size);
			}
		}
		Seq<Code> codes = new Seq<Code>();

		final int maxCount = 999-2;
		
		Code code = new Code();
		code.getLink("#Display", 0);
		code.jump("equal #Display null", -1);
		int count = maxCount;
		
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				if(count <= 3) {
					if(code != null) {
						codes.add(code);
					}
					code = new Code();
					code.getLink("#Display", 0);
					code.jump("equal #Display null", -1);
					count = maxCount;
				}
				int w = 1;
				for (int sx = x+1; sx < size; sx++) {
					if(rgb[sx][y] != rgb[x][y]) {
						break;
					}
					w++;
				}
				code.color(rgb[x][y]);
				count--;
				code.drawRect(x, size-y-1, w, 1);
				count--;
				code.drawflush("#Display");
				count--;
				x += w-1;
			}
		}
		if(code != null) codes.add(code);
		
		Code nCode = null;
		codes.add(nCode);
		
		buildScheme(codes, size);
	}

	private static void buildScheme(Seq<Code> codes, int size) {
		Seq<Stile> tiles = new Seq<Schematic.Stile>(); // LogicBlock.compress(code, links)
		
		int bsize = size == 80 ? 2 : 4;
		int d = size == 80 ? 0 : -1;
		int wh = bsize;
		
		int index = 0;
		
		for (int bs = bsize; bs < 100; bs++) {
			for (int y = -bs-d; y <= bs; y++) {
				for (int x = -bs-d; x <= bs; x++) {
					if(x > -bs-d && y > -bs-d && x < bs && y < bs) continue;
					if(index >= codes.size) {
						wh = bs;
						break;
					}
					Code c = codes.get(index++);
					if(c == null) {
						tiles.add(new Stile(Blocks.message, x, y, "[gold]Auto generated images processor\n[lightgray]Agzam's mod", (byte) 0));
					} else {
						LogicLink link = new LogicLink(-x, -y, "agzamMod-delivery-autolink-" + index, false);
						tiles.add(new Stile(Blocks.microProcessor, x, y, 
								LogicBlock.compress(c.toString(), new Seq<>(new LogicLink[] {link})), (byte) 0));
					}
				}
				if(index >= codes.size) break;
			}
			if(index >= codes.size) break;
		}
		wh += bsize;
		tiles.add(new Stile(size == 80 ? Blocks.logicDisplay : Blocks.largeLogicDisplay, 0, 0, null, (byte) 0));
		
		Schematic schematic = new Schematic(tiles, new StringMap(), wh+1, wh+1);
		
		Vars.control.input.useSchematic(schematic);		
	}
	
	
	
}
