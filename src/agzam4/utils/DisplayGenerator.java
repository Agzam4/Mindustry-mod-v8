package agzam4.utils;

import agzam4.ModWork;
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

public class DisplayGenerator {

	// 80x80
	// 176x176
	
	public static void show() {
		Vars.platform.showMultiFileChooser(file -> {
			BaseDialog dialog = new BaseDialog(ModWork.bungle("dialog.utils.select-display"));
			dialog.title.setColor(Color.white);
			dialog.titleTable.remove();
			dialog.closeOnBack();

			dialog.cont.pane(p -> {
				p.defaults().left();
				
				Table t = new Table();
				p.add(t).row();

	            t.button(Blocks.logicDisplay.emoji() + " " + Blocks.logicDisplay.localizedName, Styles.defaultt, () -> {
	                Pixmap pixmap = new Pixmap(file);
	                create(pixmap, 80);
	                pixmap.dispose();
	            	dialog.hide();
	        		PlayerUtils.hide();
	            }).growX().pad(10).padBottom(4).wrapLabel(false).row();

	            t.button(Blocks.largeLogicDisplay.emoji() + " " + Blocks.largeLogicDisplay.localizedName, Styles.defaultt, () -> {
	                Pixmap pixmap = new Pixmap(file);
	                create(pixmap, 176);
	                pixmap.dispose();
	            	dialog.hide();
	        		PlayerUtils.hide();
	            }).growX().pad(10).padBottom(4).wrapLabel(false).row();
	            
	            t.button("@back", Styles.defaultt, () -> {
	            	dialog.hide();
	        		PlayerUtils.hide();
	            }).growX().pad(10).padBottom(4).wrapLabel(false).row();
			});
			dialog.show();
		}, "png", "jpg", "jpeg");
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
		
		Seq<Stile> seq = new Seq<Schematic.Stile>(); // LogicBlock.compress(code, links)
		
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
						seq.add(new Stile(Blocks.message, x, y, "[gold]Auto generated images processor\n[lightgray]Agzam's mod", (byte) 0));
					} else {
						LogicLink link = new LogicLink(-x, -y, "agzamMod-delivery-autolink-" + index, false);
						seq.add(new Stile(Blocks.microProcessor, x, y, 
								LogicBlock.compress(c.toString(), new Seq<>(new LogicLink[] {link})), (byte) 0));
					}
				}
				if(index >= codes.size) break;
			}
			if(index >= codes.size) break;
		}
		wh += bsize;
		seq.add(new Stile(size == 80 ? Blocks.logicDisplay : Blocks.largeLogicDisplay, 0, 0, null, (byte) 0));
		
		Schematic schematic = new Schematic(seq, new StringMap(), wh+1, wh+1);
		
		Vars.control.input.useSchematic(schematic);
		
//		return new Schematic(seq, new StringMap(), 1, generateComment  ? 2 : 1);
		
		
//		StringBuilder commentMessage = new StringBuilder();
//		commentMessage.append("[gold]Auto generated images processor[]");
//		commentMessage.append("\n[lightgray]Agzam's mod");
//
//		LogicLink link = new LogicLink(0, -5, "", true);
//
//		String _code = code.toString();
//        Core.app.setClipboardText(_code);
//		addCode(_code, commentMessage.toString(), new Seq<>(new LogicLink[]{link}));
		

//		link = new LogicLink(to.centerX(), to.centerY(),
//				"agzamMod-delivery-autolink", false);
	}
	
//	private static void addCode(String code, String comment, Seq<LogicLink> links) {
//		Vars.control.input.useSchematic(Code.createBuildPlan(code, comment, links, true));		
//	}
}
