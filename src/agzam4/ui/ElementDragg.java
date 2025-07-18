package agzam4.ui;

import agzam4.ModWork;
import arc.Core;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;

public class ElementDragg {
		
	public final Element element;

	protected Vec2 draggStart = null;

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
		setPosition(
				Mathf.clamp(element.x-draggStart.x+x, 
						element.getPrefWidth()/2f, 
						Core.scene.getWidth() - element.getPrefWidth()/2f),

				Mathf.clamp(element.y-draggStart.y+y,
						element.getPrefHeight()/2f, 
						Core.scene.getHeight() - element.getPrefHeight()/2f));
	}

	public void setPosition(float x, float y) {
		element.setPosition(
				Mathf.clamp(x, element.getPrefWidth()/2f, 
						Core.scene.getWidth() - element.getPrefWidth()/2f),
				Mathf.clamp(y, element.getPrefHeight()/2f, 
						Core.scene.getHeight() - element.getPrefHeight()/2f));
	}
}
