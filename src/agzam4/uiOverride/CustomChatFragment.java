package agzam4.uiOverride;

import static arc.Core.input;
import static arc.Core.scene;
import static mindustry.Vars.maxTextLength;
import static mindustry.Vars.mobile;
import static mindustry.Vars.net;
import static mindustry.Vars.player;
import static mindustry.Vars.ui;

import java.nio.ByteBuffer;
import agzam4.ModWork;
import agzam4.io.ByteBufferIO;
import agzam4.utils.Prefs;
import arc.Core;
import arc.Events;
import arc.Input.TextInput;
import arc.func.Boolp;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.scene.Group;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.ui.Label.LabelStyle;
import arc.scene.ui.TextField.TextFieldStyle;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType.ClientChatEvent;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.input.Binding;
import mindustry.ui.Fonts;

public class CustomChatFragment extends Table {

	private static byte suggestionsId = 0;

	private static ObjectMap<Byte, Object[]> suggestions = new ObjectMap<>();
	private static ObjectMap<String, Byte> suggestionsIds = new ObjectMap<>();
	private Object[] currentSuggestions = null;
	private String suggestionsPrefix = "";
	private String suggestionsFilter = "";
	private int suggestionsSelect = -1;
	private int suggestionsAmount = -1;
	private long keyCooldown = 0;
	private long nextKeyCooldown = 0;
	
	private static CustomChatFragment instance = null;
	
	public static Object[] getSuggestionsArray(byte id, int size) {
		Object[] arr = suggestions.get(id);
		if(arr == null || arr.length != size) {
			arr = new Object[size];
			suggestions.put(id, arr);
		}
		return arr;
	}
	
	public static void updateSuggestionsArray() {
		instance.updateCurrentSuggestions();
	}
	
	private static final int messagesShown = 10;

	public static final Seq<Color> messageColors = loadColors();
	public static String colorTrigger = ModWork.settingDef("messages-gradient-trigger", "");
	
	private static Seq<Color> loadColors() {
		Seq<Color> pal = new Seq<Color>();
		String[] colors = ModWork.settingDef("messages-gradient", "").split(" ");
		for (String color : colors) {
			if(color.isEmpty()) continue;
			pal.add(Color.valueOf(color));
		}
		return pal;
	}

	private Seq<String> messages = new Seq<>();
	private TextField chatfield;
	private Label fieldlabel;
	private GlyphLayout layout;
	private Seq<String> history;
	
	public static Font font = Fonts.outline;

	private float fadetime;
	private boolean shown = false;
	private ChatMode mode = ChatMode.normal;
	private float offsetx = Scl.scl(4), offsety = Scl.scl(4), fontoffsetx = Scl.scl(2), chatspace = Scl.scl(50);
	private Color shadowColor = new Color(0, 0, 0, 0.5f);
	private float textspacing = Scl.scl(10);
	private int historyPos = 0;
	private int scrollPos = 0;
	
	
	public CustomChatFragment() {
		super();
		instance = this;
		
		font = Prefs.settings.bool("outline-chat") ? Fonts.outline : Fonts.def;
		
		messages = Reflect.get(UiOverride.oldChatFragment, "messages");
		chatfield = Reflect.get(UiOverride.oldChatFragment, "chatfield");
		fieldlabel = Reflect.get(UiOverride.oldChatFragment, "fieldlabel");
		layout = Reflect.get(UiOverride.oldChatFragment, "layout");
		history = Reflect.get(UiOverride.oldChatFragment, "history");

        setFillParent(true);
        
        history.insert(0, "");
        setup();
        
		visible(() -> {
			if(!net.active() && messages.size > 0){
				clearMessages();

				if(shown){
					hide();
				}
			}
			return net.active() && ui.hudfrag.shown;
		});
		
		update(() -> {
			if(net.active() && input.keyTap(Binding.chat) && (scene.getKeyboardFocus() == chatfield || scene.getKeyboardFocus() == null || ui.minimapfrag.shown()) && !ui.consolefrag.shown()){
				toggle();
			}
			if(shown){
				if(needSuggestions()) {
					if(input.keyTap(Binding.chatMode)){
						String add = currentSuggestions[suggestionsSelect].toString();
						int space = add.indexOf(' ', 1);
						if(space != -1) add = add.substring(0, space);
						chatfield.setText(suggestionsPrefix + add);
						chatfield.setCursorPosition(chatfield.getText().length());
						return;
					}
					if(Time.millis() > keyCooldown || (input.keyTap(Binding.chatHistoryPrev) || input.keyTap(Binding.chatHistoryNext))) {
						keyCooldown = Time.millis() + nextKeyCooldown;
						nextKeyCooldown = 150;
						for (int i = 0; i < currentSuggestions.length; i++) {
							if(input.keyDown(Binding.chatHistoryPrev)){
								suggestionsSelect++;
							}
							if(input.keyDown(Binding.chatHistoryNext)){
								suggestionsSelect--;
							}
							suggestionsSelect = Mathf.mod(suggestionsSelect, currentSuggestions.length);
							if(filterSuggestion(currentSuggestions[suggestionsSelect])) break;
						}
					}
					return;
				}
				if(input.keyTap(Binding.chatHistoryPrev) && historyPos < history.size - 1){
					if(historyPos == 0){
						String message = chatfield.getText();
						if(!message.isEmpty()) {
							history.insert(0, message);
						}
					}
					historyPos++;
					updateChat();
				}
				if(input.keyTap(Binding.chatHistoryNext) && historyPos > 0){
					historyPos--;
					updateChat();
				}
				if(input.keyTap(Binding.chatMode)){
					nextMode();
				}
				scrollPos = (int)Mathf.clamp(scrollPos + input.axis(Binding.chatScroll), 0, Math.max(0, messages.size - messagesShown));
			}
		});
	}

	private boolean needSuggestions() {
		Log.info("[needSuggestions] @ @ @", currentSuggestions, suggestionsPrefix, suggestionsAmount);
		return currentSuggestions != null && suggestionsPrefix.length() > 0 && suggestionsAmount > 0;
	}

	private boolean filterSuggestion(Object obj) {
		if(obj.toString().toLowerCase().startsWith(suggestionsFilter.toLowerCase())) return true;
		if(obj instanceof UnlockableContent content) {
			return content.localizedName.toLowerCase().contains(suggestionsFilter.toLowerCase());
		}
		return false;
	}

	public void build(Group parent){
		scene.add(this);
	}

	public void clearMessages(){
		suggestions.clear();
		if(history.isEmpty()) history.insert(0, "");
	}

	private void setup(){
		fieldlabel.setStyle(new LabelStyle(fieldlabel.getStyle()));
		fieldlabel.getStyle().font = font;
		fieldlabel.setStyle(fieldlabel.getStyle());

		chatfield = new TextField("", new TextFieldStyle(scene.getStyle(TextFieldStyle.class)));
		chatfield.getStyle().background = null;
		chatfield.getStyle().fontColor = Color.white;
		chatfield.setStyle(chatfield.getStyle());
		
		chatfield.typed(this::handleType);

		bottom().left().marginBottom(offsety).marginLeft(offsetx * 2).add(fieldlabel).padBottom(6f);
		add(chatfield).padBottom(offsety).padLeft(offsetx).growX().padRight(offsetx).height(28);
        
		if(Vars.mobile){
			marginBottom(105f);
			marginRight(240f);
		}
	}

	boolean tips = false;

	private float suggestionsWidth = 0;
	private float suggestionsHeight = 0;
	
	//no mobile support.
	private void handleType(char c){
		int cursor = chatfield.getCursorPosition();
		if(c == ':') {
			int index = chatfield.getText().lastIndexOf(':', cursor - 2);
			if(index >= 0 && index < cursor){
				String text = chatfield.getText().substring(index + 1, cursor - 1);
				String uni = Fonts.getUnicodeStr(text);
				if(uni != null && uni.length() > 0){
					chatfield.setText(chatfield.getText().substring(0, index) + uni + chatfield.getText().substring(cursor));
					chatfield.setCursorPosition(index + uni.length());
				}
			}
		}
		
		if(chatfield.getText().startsWith("/")) {
			try {

				updateCurrentSuggestions();
				
				String command = chatfield.getText();
				if(!command.endsWith(" ")) return;
				int index = command.indexOf(' ');
				if(index == -1) return;
				
				String type = command.substring(1, index);
				
				int tmp = 0;
				for (int i = index+1; i < command.length(); i++) if(command.charAt(i) == ' ') tmp++;

				String[] args = new String[tmp];
				
				int argId = 0;
				tmp = index+1;
				
				
				for (int i = index+1; i < command.length(); i++) {
					if(command.charAt(i) == ' ') {
						args[argId++] = command.substring(tmp, i);
						tmp = i+1;
					}
				}
				
				byte id = suggestionsId++;
				suggestionsIds.put(command.substring(0, command.lastIndexOf(' ')), id);

				/**
				 * id
				 * type
				 * amount of arguments
				 * arguments...
				 */
				var res = ByteBuffer.allocate(2 + (args.length+1) * Byte.MAX_VALUE);
				res.put(id);
				ByteBufferIO.writeString(res, type);
				res.put((byte) args.length);
				for (int i = 0; i < args.length; i++) {
					ByteBufferIO.writeString(res, args[i]);
				}
				Call.serverBinaryPacketUnreliable("agzam4.cmd-sug", res.array());
			} catch (Exception e) {
				Log.err(e);
			}
		}
	}

	private void updateCurrentSuggestions() {
		String command = chatfield.getText();
		int space = command.lastIndexOf(' ');
		if(space != -1) {
			String cmd = command.substring(0, space);
			Byte id = suggestionsIds.get(cmd);
			if(id != null) {
				var suggestions = CustomChatFragment.suggestions.get(id.byteValue());
				if(suggestions != null) {
					boolean hasNull = false;
					for (int i = 0; i < suggestions.length; i++) {
						if(suggestions[i] != null) continue;
						hasNull = true;
						break;
					}
					if(!hasNull) {
						currentSuggestions = suggestions;
						suggestionsPrefix = cmd + " ";
						suggestionsFilter = command.substring(space+1);
						suggestionsWidth = 0;
						nextKeyCooldown = 300;
						if(currentSuggestions.length == 0) return;
						suggestionsSelect = Mathf.mod(suggestionsSelect, currentSuggestions.length);
						for (int i = 0; i < currentSuggestions.length; i++) {
							if(filterSuggestion(currentSuggestions[suggestionsSelect])) break;
							suggestionsSelect++;
							suggestionsSelect = Mathf.mod(suggestionsSelect, currentSuggestions.length);
						}

						suggestionsAmount = 0;
						for (int i = 0; i < currentSuggestions.length; i++) {
							if(!filterSuggestion(currentSuggestions[suggestionsSelect])) continue;
							suggestionsAmount++;
						}
						
						return;
					}
				}
			}
		}
		currentSuggestions = null;
		suggestionsFilter = "";
		suggestionsPrefix = "";
		suggestionsWidth = 0;
		suggestionsAmount = 0;
	}

	protected void rect(float x, float y, float w, float h){
		//prevents texture bindings; the string lookup is irrelevant as it is only called <10 times per frame, and maps are very fast anyway
		Draw.rect("whiteui", x + w/2f, y + h/2f, w, h);
	}

	@Override
	public void draw(){
		float opacity = Core.settings.getInt("chatopacity") / 100f;
		float textWidth = Math.min(Core.graphics.getWidth()/1.5f, Scl.scl(700f));

		Draw.color(shadowColor);

		if(shown){
			rect(offsetx, chatfield.y + scene.marginBottom, chatfield.getWidth() + 15f, chatfield.getHeight() - 1);
		}

		super.draw();

		float spacing = chatspace;

		chatfield.visible = shown;
		fieldlabel.visible = shown;

		Draw.color(shadowColor);
		Draw.alpha(shadowColor.a * opacity);

		float theight = offsety + spacing + getMarginBottom() + scene.marginBottom;
		for(int i = scrollPos; i < messages.size && i < messagesShown + scrollPos && (i < fadetime || shown); i++){

			layout.setText(font, messages.get(i), Color.white, textWidth, Align.bottomLeft, true);
			theight += layout.height + textspacing;
			if(i - scrollPos == 0) theight -= textspacing + 1;

			font.getCache().clear();
			font.getCache().setColor(Color.white);
			font.getCache().addText(messages.get(i), fontoffsetx + offsetx, offsety + theight, textWidth, Align.bottomLeft, true);

			if(!shown && fadetime - i < 1f && fadetime - i >= 0f){
				font.getCache().setAlphas((fadetime - i) * opacity);
				Draw.color(0, 0, 0, shadowColor.a * (fadetime - i) * opacity);
			}else{
				font.getCache().setAlphas(opacity);
			}

			rect(offsetx, theight - layout.height - 2, textWidth + Scl.scl(4f), layout.height + textspacing);
			Draw.color(shadowColor);
			Draw.alpha(opacity * shadowColor.a);

			font.getCache().draw();
		}
		
		if(shown) {
			var suggestions = currentSuggestions;
			if(suggestions != null) {
				float x = fieldlabel.getRight();
				
				if(suggestionsWidth == 0) {
					suggestionsHeight = 0;
					for (int i = 0; i < suggestions.length; i++) {
						if(!filterSuggestion(suggestions[i])) continue;
						layout.setText(font, suggestionToString(suggestions[i]), Color.white, scene.getWidth(), Align.bottomLeft, false);
						suggestionsWidth = Math.max(suggestionsWidth, layout.width);
						suggestionsHeight += chatfield.getHeight();
					}
				}

				layout.setText(font, suggestionsPrefix, Color.white, scene.getWidth(), Align.bottomLeft, false);

				x += layout.width;

				float sy = chatfield.y + scene.marginBottom + chatfield.getHeight();

				Draw.color(Color.black, opacity * shadowColor.a);
				rect(x, sy, suggestionsWidth + fontoffsetx*2, suggestionsHeight);
				
//				Draw.color(Pal.accent, opacity);
//				rect(x, sy, fontoffsetx, suggestionsHeight);
//				rect(x + suggestionsWidth + fontoffsetx*2, sy, fontoffsetx, suggestionsHeight);
//				rect(x, sy, suggestionsWidth + fontoffsetx*2, fontoffsetx);
//				rect(x, sy + suggestionsHeight, suggestionsWidth + fontoffsetx*2, fontoffsetx);
				
				for (int i = 0; i < suggestions.length; i++) {
					if(!filterSuggestion(suggestions[i])) continue;
					String text = suggestionToString(suggestions[i]);
					
					layout.setText(font, text, Color.white, scene.getWidth(), Align.bottomLeft, false);
					font.getCache().clear();
					font.getCache().setColor(suggestionsSelect == i ? Pal.accent : Color.white);

					if(suggestionsSelect == i) {
						Draw.color(Pal.darkerGray, opacity);
						rect(x, sy, suggestionsWidth + fontoffsetx*2, chatfield.getHeight() - 1);
					}
					
					int space = text.indexOf(' ');
					if(space == -1) font.getCache().addText(text, x + fontoffsetx, sy + chatfield.getHeight() - (chatfield.getHeight() - layout.height)/2f, layout.width, Align.bottomLeft, false);
					else {
						GlyphLayout prelayout = font.getCache().addText(text.substring(0, space), x + fontoffsetx, sy + chatfield.getHeight() - (chatfield.getHeight() - layout.height)/2f, layout.width, Align.bottomLeft, false);
						float w = prelayout.width;
						font.getCache().draw();
						
						font.getCache().clear();
						font.getCache().setColor(Color.lightGray);
						font.getCache().addText(text.substring(space), x + fontoffsetx + w, sy + chatfield.getHeight() - (chatfield.getHeight() - layout.height)/2f, layout.width, Align.bottomLeft, false);
					}
					

					font.getCache().draw();
					
					sy += chatfield.getHeight();
				}
			}
		}

		Draw.color();

		if(fadetime > 0 && !shown) {
			fadetime -= Time.delta / 180f;
		}
	}

	private String suggestionToString(Object suggestion) {
		return suggestion instanceof UnlockableContent unlock ? unlock.name + (" " + unlock.localizedName + " " + unlock.emoji()) : suggestion.toString();
	}

	private void sendMessage(){
		String message = chatfield.getText();
		clearChatInput();

//		if(message.startsWith(mode.prefix)){
//			message = message.substring(mode.prefix.length());
//		}
		message = message.trim();

		//avoid sending empty messages
		if(message.isEmpty()) return;

		history.insert(1, message);
		
		if(messageColors.size == 0 || message.startsWith("/") || !message.startsWith(colorTrigger) || message.length() != Strings.stripColors(message).length()) message = mode.normalizedPrefix() + message;
		else {
			message = message.substring(colorTrigger.length());
			StringBuilder msg = new StringBuilder(mode.normalizedPrefix());
			int lastColor = Color.white.rgb888();
			for (int i = 0; i < message.length(); i++) {
				Color color = messageColors.get(i*messageColors.size/message.length());
				if(color.rgb888() != lastColor) {
					lastColor = color.rgb888();
					msg.append("[#");
					msg.append(color.toString());
					msg.append("]");
				}
				msg.append(message.charAt(i));
			}
			message = msg.toString();
		}
		Events.fire(new ClientChatEvent(message));
		Call.sendChatMessage(message);
	}

	public void toggle(){

		if(!shown){
			scene.setKeyboardFocus(chatfield);
			shown = true;
			if(mobile){
				TextInput input = new TextInput();
				input.maxLength = maxTextLength;
				input.accepted = text -> {
					chatfield.setText(text);
					sendMessage();
					hide();
					Core.input.setOnscreenKeyboardVisible(false);
				};
				input.canceled = this::hide;
				Core.input.getTextInput(input);
			}else{
				chatfield.fireClick();
			}
		}else{
			//sending chat has a delay; workaround for issue #1943
			Time.runTask(2f, () ->{
				scene.setKeyboardFocus(null);
				shown = false;
				scrollPos = 0;
				sendMessage();
			});
		}
	}

	public void hide(){
		scene.setKeyboardFocus(null);
		shown = false;
		clearChatInput();
	}

	public void updateChat(){
		// mode.normalizedPrefix() + 
		chatfield.setText(history.get(historyPos));
		updateCursor();
	}
	
	public void nextMode() {
//		ChatMode prev = mode;

		do{
			mode = mode.next();
		}while(!mode.isValid());


		if(mode == ChatMode.normal) {
			fieldlabel.setText(mode.displayText);
			fieldlabel.setColor(Color.white);
		} else if(mode == ChatMode.team) {
			fieldlabel.setText("<" + Iconc.players + ">");
			fieldlabel.setColor(player.team().color);
		} else if(mode == ChatMode.admin) {
			fieldlabel.setText("<" + Iconc.admin + ">");
			fieldlabel.setColor(Color.red);
		}
		
//		if(chatfield.getText().startsWith(prev.normalizedPrefix())){
//			chatfield.setText(mode.normalizedPrefix() + chatfield.getText().substring(prev.normalizedPrefix().length()));
//		}else{
//			chatfield.setText(mode.normalizedPrefix());
//		}

		updateCursor();
	}

	public void clearChatInput(){
		historyPos = 0;
		history.set(0, "");
		chatfield.setText("");//mode.normalizedPrefix());
		updateCursor();
	}

	public void updateCursor(){
		chatfield.setCursorPosition(chatfield.getText().length());
		updateCurrentSuggestions();
	}

	public boolean shown(){
		return shown;
	}

	public void addMessage(String message){
		if(message == null) return;
		messages.insert(0, message);

		fadetime += 1f;
		fadetime = Math.min(fadetime, messagesShown) + 1f;

		if(scrollPos > 0) scrollPos++;
	}

	private enum ChatMode{
		normal("", ">"), // "<" + Iconc.chat + ">"
		team("/t", "<" + Iconc.players + ">"),
		admin("/a", "<" + Iconc.admin + ">")
		;

		public String prefix, displayText;
		public Boolp valid;
		public static final ChatMode[] all = values();

		ChatMode(String prefix, String displayText) {
			this.prefix = prefix;
			this.displayText = displayText;
			this.valid = () -> true;
		}

		ChatMode(String prefix, Boolp valid){
			this.prefix = prefix;
			this.valid = valid;
		}

		public ChatMode next(){
			return all[(ordinal() + 1) % all.length];
		}

		public String normalizedPrefix(){
			return prefix.isEmpty() ? "" : prefix + " ";
		}

		public boolean isValid(){
			return valid.get();
		}
	}

}
