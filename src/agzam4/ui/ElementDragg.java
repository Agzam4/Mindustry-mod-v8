package agzam4.ui;

import agzam4.ModWork;
import arc.Core;
import arc.func.Cons;
import arc.func.Floatc;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.layout.Scl;
import arc.util.Nullable;

public class ElementDragg {
		
	public final Element element;

	protected Vec2 draggStart = null;

	private Rect tmp = new Rect();
//	public @Nullable Rect box = null;
	public @Nullable Cons<Rect> box = null;

	public ElementDragg(Element element, String name) {
		this.element = element;

		element.addListener(new InputListener() {

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
				draggStart = new Vec2(x, y);
				return true;
			}

			@Override
			public void touchDragged(InputEvent event, float x, float y, int pointer) {
				update(x, y);
			}

			@Override
			public void touchUp(InputEvent e, float x, float y, int pointer, KeyCode button) {
				update(x, y);
				draggStart = null;

				ModWork.setting(name + "-x", element.x);
				ModWork.setting(name + "-y", element.y);
			}
		});
		setPosition(
				ModWork.settingFloat(name + "-x", 0), 
				ModWork.settingFloat(name + "-y", 0)
				);
	}

	public void update(float x, float y) {
		if(box == null) {
			setPosition(
					Mathf.clamp(element.x-draggStart.x+x, 
							element.getPrefWidth()/2f, 
							Core.scene.getWidth() - element.getPrefWidth()/2f),

					Mathf.clamp(element.y-draggStart.y+y,
							element.getPrefHeight()/2f, 
							Core.scene.getHeight() - element.getPrefHeight()/2f));
			return;
		}
		setPosition(element.x-draggStart.x+x, element.y-draggStart.y+y);
	}

	public void setPosition(float x, float y) {
		float dx = element.getPrefWidth()/2f;
		float dy = element.getPrefHeight()/2f;
		
		
		if(box == null) {
			element.setPosition(
					x,
					Mathf.clamp(y, element.getPrefHeight()/2f, 
							Core.scene.getHeight() - element.getPrefHeight()/2f));
			return;
		}
		box.get(tmp);
		
		element.setPosition(
				Mathf.clamp(x-dx + tmp.x, 0, Core.scene.getWidth() - tmp.width)+dx - tmp.x,
				Mathf.clamp(y-dy + tmp.y, 0, Core.scene.getHeight() - tmp.height)+dy - tmp.y);
	}
}
