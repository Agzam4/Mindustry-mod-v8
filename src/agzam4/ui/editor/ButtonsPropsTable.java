package agzam4.ui.editor;

import java.util.Iterator;

import agzam4.AgzamMod;
import agzam4.ModWork;
import agzam4.ModWork.KeyBinds;
import agzam4.industry.IndustryCalculator;
import agzam4.ui.MobileUI.MobileButtons;
import agzam4.utils.PlayerUtils;
import arc.func.Cons;
import arc.func.Prov;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import mindustry.gen.Iconc;

public class ButtonsPropsTable implements Iterable<ButtonProps> {

	public Seq<ButtonProps> buttons;

	public int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
	public int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
	public int width, height;

	public ButtonsPropsTable(Seq<ButtonProps> buttons) {
		this.buttons = buttons;
		update();
	}
	
	public ButtonsPropsTable(ButtonProps... props) {
		buttons = Seq.with(props);
		update();
	}

	@Override
	public Iterator<ButtonProps> iterator() {
		return buttons.iterator();
	}

	public void update() {
		minX = Integer.MAX_VALUE; maxX = Integer.MIN_VALUE;
		minY = Integer.MAX_VALUE; maxY = Integer.MIN_VALUE;
		for (var button : buttons) {
			minX = Math.min(minX, button.x());
			maxX = Math.max(maxX, button.x());
			minY = Math.min(minY, button.y());
			maxY = Math.max(maxY, button.y());
		}
		width = maxX - minX + 1;
		height = maxY - minY + 1;
	}
	
	public boolean has(int x, int y) {
		for (var b : buttons) {
			if(b.x() == x && b.y() == y) return true;
		}
		return false;
	}

	public void set(Point2 pos, MobileButtons type) {
		buttons.removeAll(b -> b.position.equals(pos.x, pos.y));
		if(type == MobileButtons.remove) return;
		ButtonProps prop = type == MobileButtons.empty ? new ButtonProps() : buttons.find(b -> b.position.equals(pos.x, pos.y));
		if(prop == null) {
			prop = type.prop;
			buttons.add(prop);
		}
		prop.position(pos.x, pos.y);
		if(type == MobileButtons.empty) buttons.add(prop);
		Log.info(buttons);
	}

	public static ButtonsPropsTable load(String name, Prov<ButtonsPropsTable> def) {
		try {
			String settings = ModWork.settingDef(name, "");
			if(settings.isEmpty()) return def.get();
			ButtonsPropsTable table = new ButtonsPropsTable();
			for (var line : settings.split("\n")) {
				var data = line.split(";");
				if(data.length != 3) continue;
				
				MobileButtons button = MobileButtons.find(data[0]);
				if(button == null) continue;
				int x = Strings.parseInt(data[1], 0);
				int y = Strings.parseInt(data[2], 0);
				table.set(new Point2(x, y), button);
			}
			return table;
		} catch (Exception e) {
			Log.err(e);
		}
		return def.get();
	}
	
	public void save(String name) {
		ModWork.setting(name, buttons.toString("\n", b -> Strings.format("@;@;@", b.name, b.position.x, b.position.y)));
	}
	
}
