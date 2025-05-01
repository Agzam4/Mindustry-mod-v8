package agzam4.utils;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.struct.StringMap;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.game.Schematic.Stile;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.LogicBlock.LogicLink;

public class Code {

	Seq<String> code = new Seq<String>();
	Seq<String> marks = new Seq<String>();
	ObjectMap<Integer, String> jumps = new ObjectMap<>();
	ObjectMap<Integer, Integer> dJumps = new ObjectMap<>();
	
	UnitType type;
	
	public void ubind(UnitType type) {
		this.type = type;
		line("ubind @" + type.name);
	}

	public void uSetFlag(int flag) {
		line("ucontrol flag " + flag + " 0 0 0 0");
	}

	public void uFlag(String var) {
		line("sensor " + var + " @unit @flag");
	}

	public void sum(String result, String a, String b) {
		line("op add " + result + " " + a + " " + b);	
	}

	public void getLink(String var, int i) {
		line("getlink " + var + " " + i);			
	}

	public void takeItems(String build, String items) {
		line("ucontrol itemTake " + build + " " + items + " #ItemsCapacity 0 0");
	}

	public void dropItems(String build) {
		line("ucontrol itemDrop " + build + " #Items 0 0 0");
	}

	public void getBlock(Tile tile) {
		line("ucontrol getBlock " + tile.centerX() + " " + tile.centerY() + " #Type #Building #Floor");
	}

	public void uSensorItems(String var) {
		line("sensor " + var + " @unit @firstItem");
	}
	public void uSensorItem(String var, Item item) {
		line("sensor " + var + " @unit @" + item.name);
	}

	public void approachAndMine() {
		line("ucontrol approach #OreX #OreY 5 0 0");
		line("ucontrol mine #OreX #OreY 5 0 0");
	}

	public void ulocateOre(String ore) {
		line("ulocate ore core true " + ore + " #OreX #OreY #Found building");
	}

	public void set(String var, String value) {
		line("set " + var + " " + value);
	}
	
	public void set(String var, Item item) {
		set(var, "@" + item.name);
	}

	public void sensorItems(String var, String from, Item item) {
		line("sensor " + var + " " + from + " @" + item.name);
	}
	
	public void sensorAmmo(String var, String from) {
		line("sensor " + var + " " + from + " @ammo");
	}
	
	public void sensorItems(String var, Item item) {
		line("sensor " + var + " #Core @" + item.name);
	}

	public void boost(boolean b) {
		line("ucontrol boost " + (b ? 1 : 0) + " 0 0 0 0");
	}

	public void end() {
		line("end");
	}

	public void itemDrop(String string) {
		line("ucontrol itemDrop " + string + " #Items 0 0 0");
	}

	public void approachToCore() {
		if(type.flying) line("ucontrol approach #CoreX #CoreY 5 0 0");
		else pathfindToCore();
	}

	public void pathfindToCore() {
		line("ucontrol pathfind #CoreX #CoreY 0 0 0");
	}
	
	public void approachTo(int x, int y) {
		if(type.flying) line("ucontrol approach " + x + " " + y + " 3 0 0");
		else pathfindTo(x, y);
	}
	
	public void pathfindTo(int x, int y) {
		line("ucontrol pathfind " + x + " " + y + " 0 0 0");
	}

	public void color(int rrggbbaa88) {
        int r = ((rrggbbaa88 & 0xff000000) >>> 24);
        int g = ((rrggbbaa88 & 0x00ff0000) >>> 16);
        int b = ((rrggbbaa88 & 0x0000ff00) >>> 8);
        int a = ((rrggbbaa88 & 0x000000ff));
		line("draw color " + r + " " + g + " " + b + " " + a + " 0 0");
	}

	public void color(int r, int g, int b, int a) {
		line("draw color " + r + " " + g + " " + b + " " + a + " 0 0");
	}
	
	public void drawClear(int r, int g, int b, int a) {
		line("draw clear " + r + " " + g + " " + b + " " + a + " 0 0");
	}

	public void drawRect(int x, int y, int w, int h) {
		line("draw rect " + x + " " + y + " " + w + " " + h + " 0 0");
	}

	public void drawTriangle(int[] xs, int[] ys) {
		line("draw triangle " + xs[0] + " " + ys[0] + " " + xs[1] + " " + ys[1] + " " + xs[2] + " " + ys[2]);
	}

	public void drawflush(String display) {
		line("drawflush " + display);
	}

	public void jump(String condition, String line) {
		jumps.put(marks.size, line);
		line("jump @line " + condition);
	}

	public void jump(String condition, int change) {
		dJumps.put(marks.size, change);
		line("jump @line " + condition);
	}

	public void uItemStack() {
		line("sensor #Items @unit @totalItems");
		line("sensor #ItemsCapacity @unit @itemCapacity");
	}

	public void ulocateCore() {
		line("ulocate building core 0 @copper #CoreX #CoreY #Found #Core");
	}
	
	private void line(String line) {
		code.add(line);
//		code.append(line);
//		code.append('\n');
		marks.add("line#" + marks.size);
	}
	
	public void markLast(String mark) {
		marks.set(marks.size-1, mark);
	}
	
	@Override
	public String toString() {
		StringBuilder codeBuilder = new StringBuilder();
//		Log.info(jumps);
//		Log.info(dJumps);
		for (int i = 0; i < code.size; i++) {
			String line = code.get(i).replaceAll("#", "agzamMod"); // Need to other modifications can recognize my mod's code 
			String jumpMark = jumps.get(i);
			Integer dJumpMark = dJumps.get(i);
			if(jumpMark != null) line = line.replaceFirst("@line", "" + getLine(jumpMark));
			else if(dJumpMark != null) line = line.replaceFirst("@line", "" + (i+dJumpMark.intValue()));
			codeBuilder.append(line);
			codeBuilder.append('\n');
		}
		return codeBuilder.toString();
	}
	
	
	public int getLine(String mark) {
		return marks.indexOf(mark);
	}

	public static Schematic createBuildPlan(String code, String comment, Seq<LogicLink> links, boolean generateComment) {
		Seq<Stile> seq = new Seq<Schematic.Stile>();
		seq.add(new Stile(Blocks.microProcessor, 0, 0, LogicBlock.compress(code, links), (byte) 0));
		if(generateComment) {
			seq.add(new Stile(Blocks.message, 0, 1, comment, (byte) 0));
		}
		return new Schematic(seq, new StringMap(), 1, generateComment  ? 2 : 1);
	}

	public static Schematic createDisplayBuildPlan(String code, String comment, Seq<LogicLink> links, boolean generateComment) {
		Seq<Stile> seq = new Seq<Schematic.Stile>();
		seq.add(new Stile(Blocks.microProcessor, 0, 0, LogicBlock.compress(code, links), (byte) 0));
		seq.add(new Stile(Blocks.logicDisplay, 0, -5, null, (byte) 0));
		if(generateComment) {
			seq.add(new Stile(Blocks.message, 0, 1, comment, (byte) 0));
		}
		return new Schematic(seq, new StringMap(), 1, generateComment  ? 2 : 1);
	}
}