package agzam4.ui.editor;

import agzam4.ModWork;
import agzam4.ui.MobileUI.MobileButtons;
import arc.func.Boolc;
import arc.math.geom.Point2;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Strings;
import mindustry.ui.Styles;

import static agzam4.ui.MobileUI.tilesize;

public class ButtonProps {

	public final String name;

	public @Nullable Runnable onClick;
	public @Nullable Boolc onToggle;
	
	public Point2 position = new Point2(0, 0);
	public String text = "";
	public boolean collapseable = true;

	public ButtonProps() {
		this.name = MobileButtons.empty.prop.name;
	}
	
	public ButtonProps(String name, Runnable onClick) {
		this.name = name;
		this.onClick = onClick;
	}

	public ButtonProps(String name, Boolc onToggle) {
		this.name = name;
		this.onToggle = onToggle;
	}
	
	
	public boolean isToggle() {
		return onToggle != null;
	}
	
	public ButtonProps position(int x, int y) {
		position.x = x;
		position.y = y;
		return this;
	}

	public ButtonProps icon(char c) {
		text = Character.toString(c);
		return this;
	}

	public TextButton button(boolean listener) {
		boolean empty = name.isEmpty() || name.equals(MobileButtons.empty.name());
        TextButton button = new TextButton(text, isToggle() ? Styles.logicTogglet : Styles.grayt);
        if(listener) button.changed(() -> {
        	if(onToggle != null) onToggle.get(button.isChecked());
        	if(onClick != null) onClick.run();
        });
        button.setDisabled(empty && listener);
        return button;
	}
	
	public void button2(Table table) {
		
        TextButton button = new TextButton(text, isToggle() ? Styles.logicTogglet : Styles.grayt);
        button.changed(() -> {
        	if(onToggle != null) onToggle.get(button.isChecked());
        	if(onClick != null) onClick.run();
        });
//        button.setPosition(position.x * tilesize, position.y * tilesize);
//        button.setFillParent(true);
//        button.setSize(tilesize, tilesize);
        
//        button.update(() -> {
////            button.setFillParent(true);
////            button.setSize(tilesize, tilesize);
//            button.setBounds(position.x * tilesize, position.y * tilesize, tilesize, tilesize);
//        });
        button.setDisabled(name.isEmpty());
        
        var cell = table.add(button).margin(0).pad(0);
//        cell.setBounds(position.x * tilesize, position.y * tilesize, tilesize, tilesize);

        cell.update(t -> {
        	t.setBounds(position.x * tilesize, -position.y * tilesize, tilesize, tilesize);
        	cell.setBounds(position.x * tilesize, -position.y * tilesize, tilesize, tilesize);
        });
        
		Log.info("button [gold]@[] (@,@)", name, position.x * tilesize, position.y * tilesize);
	}

	public int x() {
		return position.x;
	}

	public int y() {
		return position.y;
	}

	@Override
	public String toString() {
		return Strings.format("button [gold]@[] (@,@)", name, position.x * tilesize, position.y * tilesize);
	}
	

	public ButtonProps collapseable(boolean collapseable) {
		this.collapseable = collapseable;
		return this;
	}
}
