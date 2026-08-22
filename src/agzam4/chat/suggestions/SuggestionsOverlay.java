package agzam4.chat.suggestions;

import static arc.Core.input;
import static arc.Core.scene;

import static agzam4.uiOverride.CustomChatFragment.font;

import agzam4.uiOverride.CustomChatFragment;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.GlyphLayout;
import arc.scene.ui.Label;
import arc.util.Align;
import arc.util.Time;
import mindustry.graphics.Pal;
import mindustry.input.Binding;

public class SuggestionsOverlay  {

	CustomChatFragment chat;
    public Label ghostField = new Label("");
    
	public boolean active;
	public int position;
	
	public SuggestionsOverlay(CustomChatFragment chat) {
		this.chat = chat;
        ghostField.getStyle().background = null;
        ghostField.getStyle().fontColor = Color.gray;
	}

	private float suggestionsWidth;
//	private int suggestionsHeight;
	
	public long nextKeyCooldown;
	public long keyCooldown;
	
	public void draw() {
		if(!active) return;
		
		final float padX = 5;

		var suggestions = Suggestions.visible;
		if(suggestions == null) return;
		
		float x = chat.fieldStack.x - chat.offsetx;
		float sy = scene.marginBottom + chat.fieldStack.getTop();
		boolean scrollbar = Suggestions.amount > Suggestions.maxVisibleSuggestions;

		// Recalculating suggestions box
		if(suggestionsWidth == 0 && Suggestions.current != null) {
//			suggestionsHeight = 0;
			for (int i = 0; i < Suggestions.current.length; i++) {
				if(!Suggestions.isMatched(i)) continue;
				chat.layout.setText(font, Suggestions.string(i), Color.white, scene.getWidth(), Align.bottomLeft, false);
				suggestionsWidth = Math.max(suggestionsWidth, chat.layout.width);
			}
		}

		// Calculate & add prefix offset
		{
			boolean markup = font.data.markupEnabled;
			font.data.markupEnabled = false;
			chat.layout.setText(font, Suggestions.suggestionsPrefix(), Color.white, scene.getWidth(), Align.bottomLeft, false);
			font.data.markupEnabled = markup;
			x += chat.layout.width;
		}

		// Background
		Draw.color(chat.suggestionsColor);
		chat.rect(x-padX, sy, suggestionsWidth + (scrollbar ? 7 + padX : 0) + padX * 2, Suggestions.visibleAmount * chat.chatfield.getHeight());

		if(scrollbar) {
			Draw.color(Color.white, chat.suggestionsColor.a);
			chat.rect(
					x + suggestionsWidth + padX*2 + 1, 
					sy + Suggestions.visibleOffset * chat.chatfield.getHeight() * Suggestions.visibleAmount / Suggestions.amount, 
					5, Suggestions.visibleAmount * chat.chatfield.getHeight() * Suggestions.visibleAmount / Suggestions.amount);
		}

		// Suggestions
		for (int vi = 0; vi < suggestions.length; vi++) {
			int i = suggestions[vi];
			if(i == -1) break;

			String text = Suggestions.string(i);
			chat.layout.setText(font, text, Color.white, scene.getWidth(), Align.bottomLeft, false);
			font.getCache().clear();
			font.getCache().setColor(Suggestions.select == i ? Pal.accent : Color.white);

			if(Suggestions.select == i) {
				Draw.color(Pal.darkerGray, chat.opacity);
				chat.rect(x-padX, sy, suggestionsWidth + padX*2 + (scrollbar ? padX : 0), chat.chatfield.getHeight() - 1);
			}

			int space = text.indexOf(' ');
			if(space == -1) font.getCache().addText(text, x, sy + chat.chatfield.getHeight() - (chat.chatfield.getHeight() - chat.layout.height)/2f, chat.layout.width, Align.bottomLeft, false);
			else {
				// Description
				GlyphLayout prelayout = font.getCache().addText(text.substring(0, space), x, sy + chat.chatfield.getHeight() - (chat.chatfield.getHeight() - chat.layout.height)/2f, chat.layout.width, Align.bottomLeft, false);
				float w = prelayout.width;
				font.getCache().draw();

				font.getCache().clear();
				font.getCache().setColor(Color.lightGray);
				font.getCache().addText(text.substring(space), x + w, sy + chat.chatfield.getHeight() - (chat.chatfield.getHeight() - chat.layout.height)/2f, chat.layout.width, Align.bottomLeft, false);
			}
			font.getCache().draw();

			sy += chat.chatfield.getHeight();
		}
	}


	public void hide() {
		active = false;
		ghostField.setText("");
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
			ghostField.setText(Suggestions.apply());
		}
		return true;
	}


	public void updateSuggestions(boolean force) {
//		suggestionsHeight = 0;
		if(Suggestions.update(userText(), force)) {
			suggestionsWidth = 0;
			nextKeyCooldown = 300;
		} else {
			suggestionsWidth = 0;
		}
		
		if(Suggestions.has()) {
			ghostField.setText(Suggestions.apply());
			show();
		} else {
			hide();
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
