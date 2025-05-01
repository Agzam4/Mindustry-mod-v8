package agzam4.ui;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.style.Drawable;
import arc.scene.ui.Button;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Cell;
import arc.util.Scaling;
import arc.util.Time;
import mindustry.ui.Styles;

public class CrossImageButton extends Button {
	
	public static ButtonStyle defaultStyle = new ButtonStyle(){{
        checked = Styles.black;
        down = UIUtils.gray;
        up = Styles.black;
        over = Styles.flatOver;
        disabled = Styles.black;
    }};
	
	Image image;
	Drawable icon;
	
	public CrossImageButton(Drawable icon) {
		this.icon = icon;
		image = new Image(icon);
		image.setScaling(Scaling.fit);
		image.setSize(20f);
		add(image);
		setStyle(defaultStyle);
        getImageCell().size(20f);
	}

    public Cell<?> getImageCell(){
        return getCell(image) == null ? getCells().first() : getCell(image);
    }
	
	float lineFrom, lineTo;
	private float pad = 10f;

	@Override
	public void draw() {
        image.setDrawable(icon);
        image.setColor(color);
        
        if(isChecked()) {
        	lineFrom = Mathf.approach(lineFrom, 0, Time.delta/10f);
        	lineTo = Mathf.approach(lineTo, 1f, Time.delta/10f);
        } else {
        	lineFrom = Mathf.approach(lineFrom, .5f, Time.delta/10f);
        	lineTo = Mathf.approach(lineTo, .5f, Time.delta/10f);
        }
		super.draw();
//		if(lineFrom <= lineTo) {
	        Draw.color(.2f,0,0,1f);
	        Lines.stroke(4f);
	        Lines.line(
	        		Mathf.lerp(x+pad, x+width-pad, lineFrom), 
	        		Mathf.lerp(y+pad, y+height-pad, lineFrom), 
	        		Mathf.lerp(x+pad, x+width-pad, lineTo), 
	        		Mathf.lerp(y+pad, y+height-pad, lineTo));
	        Draw.color(1f,.1f,.1f);
	        Lines.stroke(2f);
	        Lines.line(
	        		Mathf.lerp(x+pad, x+width-pad, lineFrom), 
	        		Mathf.lerp(y+pad, y+height-pad, lineFrom), 
	        		Mathf.lerp(x+pad, x+width-pad, lineTo), 
	        		Mathf.lerp(y+pad, y+height-pad, lineTo));
//		}
        
	}
	
	
}
