package agzam4.uiOverride;

import static arc.Core.*;
import static mindustry.Vars.*;

import agzam4.ModWork;
import agzam4.chat.suggestions.Suggestions;
import agzam4.chat.suggestions.SuggestionsOverlay;
import agzam4.utils.Prefs;
import arc.Core;
import arc.Events;
import arc.Input.TextInput;
import arc.func.Boolp;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.Group;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.ui.Label.LabelStyle;
import arc.scene.ui.TextField.TextFieldStyle;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.game.EventType.ClientChatEvent;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.input.Binding;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

public class CustomChatFragment extends Table {

	private static CustomChatFragment instance = null;
	
	private static final int messagesShown = 10;
    private Seq<String> messages = new Seq<>();
    public TextField chatfield;
    
    public Label fieldlabel = new Label(">");
    public GlyphLayout layout = new GlyphLayout();
    private Seq<String> history = new Seq<>();
    
    private static SuggestionsOverlay suggestions;

    public static Font font;
    private float fadetime;
    private boolean shown = false;
    private ChatMode mode = ChatMode.normal;
    public float offsetx = Scl.scl(4), offsety = Scl.scl(4), fontoffsetx = Scl.scl(2), chatspace = Scl.scl(50);
    public Color shadowColor = new Color(0, 0, 0, 0.5f);
    public Color suggestionsColor = new Color(0, 0, 0, 0.8f);
    private float textspacing = Scl.scl(10);
    private int historyPos = 0;
    private int scrollPos = 0;
	private boolean lastFrameHadFocus;
	public boolean preventHideOnEscape = false;
	
	public Stack fieldStack = new Stack();

	public float opacity;
	
	public static void updateSuggestions(boolean force) {
		if(suggestions == null) return;
		suggestions.updateSuggestions(force);
	}
	

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

	
	
	public CustomChatFragment() {
		super();
		instance = this;
		suggestions = new SuggestionsOverlay(this);
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
				Log.info("clear");

				if(shown){
					Log.info("hide");
					hide();
				}
			}
			return net.active() && ui.hudfrag.shown;
		});
		
		update(() -> {
			preventHideOnEscape = true;
			if(shown && input.keyTap(KeyCode.escape)) {
				return; 
			}
			    

            boolean hasOtherFocus = (scene.getKeyboardFocus() != null && !chatfield.hasKeyboard()) && !(ui.minimapfrag.shown() && !(scene.getKeyboardFocus() instanceof TextField));

            if(net.active() && input.keyTap(Binding.chat) && !hasOtherFocus && !lastFrameHadFocus && !ui.consolefrag.shown()){
                toggle();
            }
            
			if(shown){
				if(suggestions.handleUpdate()) return;
				
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

	public void build(Group parent){
		scene.add(this);
	}

	public void clearMessages(){
		Suggestions.clear();
		if(history.isEmpty()) history.insert(0, "");
	}

	private void setup(){
		fieldlabel.setStyle(new LabelStyle(fieldlabel.getStyle()));
		fieldlabel.getStyle().font = font;
		fieldlabel.setStyle(fieldlabel.getStyle());
        fieldlabel.getStyle().background = null;

		chatfield = new TextField("", new TextFieldStyle(scene.getStyle(TextFieldStyle.class)));
        chatfield.setMaxLength(Vars.maxTextLength);
        chatfield.getStyle().background = null;
        chatfield.getStyle().fontColor = Color.white;
        chatfield.setStyle(chatfield.getStyle());

        chatfield.typed(this::handleType);




	    fieldStack.add(suggestions.ghostField);
	    fieldStack.add(chatfield);

        bottom().left().marginBottom(offsety).marginLeft(offsetx * 2).add(fieldlabel).padBottom(6f);
        add(fieldStack).padBottom(offsety).padLeft(offsetx).growX().padRight(offsetx).height(28);

        if(Vars.mobile){
            marginBottom(105f);
            marginRight(240f);
        }
        
	}

	boolean tips = false;
	
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
		// Update suggestions
		if(cursor < 1) {
			suggestions.hide();
			return;
		}
		suggestions.position = cursor;
		updateSuggestions(true);
	}

	public void rect(float x, float y, float w, float h){
		//prevents texture bindings; the string lookup is irrelevant as it is only called <10 times per frame, and maps are very fast anyway
		Draw.rect("whiteui", x + w/2f, y + h/2f, w, h);
	}

	@Override
	public void draw(){
		opacity = Core.settings.getInt("chatopacity") / 100f;
		float textWidth = Math.min(Core.graphics.getWidth()/1.5f, Scl.scl(700f));

		Draw.color(shadowColor);

		if(shown){
			rect(offsetx, fieldStack.y + scene.marginBottom, fieldStack.getWidth() + 15f, fieldStack.getHeight() - 1);
		}

		super.draw();

		float spacing = chatspace;

		fieldStack.visible = shown;
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
			suggestions.draw();
		}

		Draw.color();

		if(fadetime > 0 && !shown) {
			fadetime -= Time.delta / 180f;
		}
	}
	
    //ping format: "x,y [text]"
    private void checkPing(String message){
        int comma = message.indexOf(',');
        if(comma != -1){
            int space = message.indexOf(' ', comma + 1);
            //handle a space after the comma
            boolean extra = false;
            if(space == comma + 1){
                extra = true;
                space = message.indexOf(' ', comma + 2);
            }
            if(space != -1){
                int x = Strings.parseInt(message, 10, -1, 0, comma), y = Strings.parseInt(message, 10, -1, comma + 1 + (extra ? 1 : 0), space);
                if(world.tiles.in(x, y)){
                    Call.pingLocation(player, x * tilesize, y * tilesize, message.substring(space).trim());
                }
            }
        }
    }

	private void sendMessage(){
		String message = chatfield.getText();
		clearChatInput();

		message = message.trim();
		checkPing(message);

		//avoid sending empty messages
		if(message.isEmpty()) return;
		
		history.insert(1, message);
		
		if(message.startsWith("/")) {
			Events.fire(new ClientChatEvent(message));
			Call.sendChatMessage(message);
			return;
		}
		
		if(messageColors.size == 0 || message.startsWith("/") || !message.startsWith(colorTrigger) || message.length() != Strings.stripColors(message).length()) {
			message = mode.normalizedPrefix() + message;
		} else {
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
		if(suggestions.active) {
			suggestions.hide();
			return;
		}
		scene.setKeyboardFocus(null);
		shown = false;
		clearChatInput();
	}

	public void updateChat(){
		chatfield.setText(history.get(historyPos));
		updateCursor();
	}
	
	public void nextMode() {
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
		updateSuggestions(false);
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
