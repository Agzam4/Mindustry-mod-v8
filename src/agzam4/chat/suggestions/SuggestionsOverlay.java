package agzam4.chat.suggestions;

import static arc.Core.input;
import static arc.Core.scene;

import static agzam4.uiOverride.CustomChatFragment.font;

import agzam4.uiOverride.CustomChatFragment;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.GlyphLayout;
import arc.util.Align;
import arc.util.Time;
import mindustry.graphics.Pal;
import mindustry.input.Binding;

public class SuggestionsOverlay  {

	CustomChatFragment chat;
	public boolean active;
	public int position;
	
	public SuggestionsOverlay(CustomChatFragment chat) {
		this.chat = chat;
	}

	private float suggestionsWidth;
	private float suggestionsHeight;
	public long nextKeyCooldown;
	public long keyCooldown;
	
	public void draw() {
		if(!active) return;
		
		var suggestions = Suggestions.current;
		if(suggestions != null) {
			float x = chat.fieldlabel.getRight();
			
			if(suggestionsWidth == 0) {
				suggestionsHeight = 0;
				for (int i = 0; i < suggestions.length; i++) {
					if(!Suggestions.matched(i)) continue;
					chat.layout.setText(font, Suggestions.string(i), Color.white, scene.getWidth(), Align.bottomLeft, false);
					suggestionsWidth = Math.max(suggestionsWidth, chat.layout.width);
					suggestionsHeight += chat.chatfield.getHeight();
				}
			}

			chat.layout.setText(font, Suggestions.suggestionsPrefix, Color.white, scene.getWidth(), Align.bottomLeft, false);

			x += chat.layout.width;

			float sy = chat.chatfield.y + scene.marginBottom + chat.chatfield.getHeight();

			Draw.color(Color.black, chat.opacity * chat.shadowColor.a);
			chat.rect(x, sy, suggestionsWidth + chat.fontoffsetx*2, suggestionsHeight);
			
			for (int i = 0; i < suggestions.length; i++) {
				if(!Suggestions.matched(i)) continue;
				String text = Suggestions.string(i);
				
				chat.layout.setText(font, text, Color.white, scene.getWidth(), Align.bottomLeft, false);
				font.getCache().clear();
				font.getCache().setColor(Suggestions.select == i ? Pal.accent : Color.white);

				if(Suggestions.select == i) {
					Draw.color(Pal.darkerGray, chat.opacity);
					chat.rect(x, sy, suggestionsWidth + chat.fontoffsetx*2, chat.chatfield.getHeight() - 1);
				}
				
				int space = text.indexOf(' ');
				if(space == -1) font.getCache().addText(text, x + chat.fontoffsetx, sy + chat.chatfield.getHeight() - (chat.chatfield.getHeight() - chat.layout.height)/2f, chat.layout.width, Align.bottomLeft, false);
				else {
					GlyphLayout prelayout = font.getCache().addText(text.substring(0, space), x + chat.fontoffsetx, sy + chat.chatfield.getHeight() - (chat.chatfield.getHeight() - chat.layout.height)/2f, chat.layout.width, Align.bottomLeft, false);
					float w = prelayout.width;
					font.getCache().draw();
					
					font.getCache().clear();
					font.getCache().setColor(Color.lightGray);
					font.getCache().addText(text.substring(space), x + chat.fontoffsetx + w, sy + chat.chatfield.getHeight() - (chat.chatfield.getHeight() - chat.layout.height)/2f, chat.layout.width, Align.bottomLeft, false);
				}
				

				font.getCache().draw();
				
				sy += chat.chatfield.getHeight();
			}
		}
	
	}


	public void hide() {
		active = false;
		chat.ghostField.setText("");
	}


	public void show() {
		active = true;
	}


	public boolean handleUpdate() {
		if(!active) return false;
		if(!Suggestions.has()) return false;
		
		if(input.keyTap(Binding.chatMode)){
			chat.chatfield.setText(Suggestions.apply());
			chat.chatfield.setCursorPosition(chat.chatfield.getText().length());
		}
		if(Time.millis() > keyCooldown || (input.keyTap(Binding.chatHistoryPrev) || input.keyTap(Binding.chatHistoryNext))) {
			keyCooldown = Time.millis() + nextKeyCooldown;
			nextKeyCooldown = 150;
			if(input.keyDown(Binding.chatHistoryNext)) Suggestions.next();
			if(input.keyDown(Binding.chatHistoryPrev)) Suggestions.prev();
			chat.ghostField.setText(Suggestions.apply());
		}
		return true;
	}


	public void updateSuggestions(boolean force) {
		suggestionsHeight = 0;
		if(Suggestions.update(userText(), force)) {
			suggestionsWidth = 0;
			nextKeyCooldown = 300;
			chat.ghostField.setText(Suggestions.apply());
		} else {
			chat.ghostField.setText("");
			suggestionsWidth = 0;
		}		
	}
	
	
	/**
	 * @return part of text for suggestions
	 */
	private String userText() {
		int position = chat.chatfield.getCursorPosition();
		String all = chat.chatfield.getText();
		if(position >= all.length()) return all;
		return all.substring(0, position);
	}
	
	
	
}
