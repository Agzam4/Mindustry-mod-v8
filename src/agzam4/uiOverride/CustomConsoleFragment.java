package agzam4.uiOverride;

import static arc.Core.graphics;
import static arc.Core.input;
import static arc.Core.scene;
import static arc.Core.settings;
import static mindustry.Vars.mobile;
import static mindustry.Vars.mods;
import static mindustry.Vars.ui;

import arc.Core;
import arc.Events;
import arc.Input.TextInput;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.math.Mathf;
import arc.scene.Group;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import arc.scene.ui.Label.LabelStyle;
import arc.scene.ui.TextField.TextFieldStyle;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.FloatSeq;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Log;
import arc.util.Reflect;
import arc.util.Strings;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Iconc;
import mindustry.input.Binding;
import mindustry.ui.Fonts;

public class CustomConsoleFragment extends Table {
	
	private static final int messagesShown = 30;
	private Seq<String> messages = new Seq<>();
	private boolean open = false, shown;
	private TextField chatfield;
	private Label fieldlabel = new Label(">");
	private Font font;
	private GlyphLayout layout = new GlyphLayout();
	private float offsetx = Scl.scl(4), offsety = Scl.scl(4), fontoffsetx = Scl.scl(2), chatspace = Scl.scl(50);
	private Color 
	shadowColor = Color.valueOf("282828"), borderColor = Color.valueOf("4A4A4A"),
	infoShadowColor = Color.valueOf("282F3D"), 
	warnShadowColor = Color.valueOf("413A2A"), 
	errShadowColor = Color.valueOf("4E3534");
	
	private float textspacing = Scl.scl(25);
	private Seq<String> history = new Seq<>();
	private int historyPos = 0;
	private int scrollPos = 0;
	
	private boolean needProposals = false;
	
	Seq<String> proposals = new Seq<>();

	public CustomConsoleFragment() {
		setFillParent(true);
		font = Fonts.def;//ModWork.setting("outline-chat") ? Fonts.outline : Fonts.def;

		visible(() -> {
			if(input.keyTap(Binding.console) && settings.getBool("console") && (scene.getKeyboardFocus() == chatfield || scene.getKeyboardFocus() == null) && !ui.chatfrag.shown()){
				shown = !shown;
				if(shown && !open && settings.getBool("console")){
					toggle();
				}
				if(shown){
					chatfield.requestKeyboard();
				}else if(scene.getKeyboardFocus() == chatfield){
					scene.setKeyboardFocus(null);
					scene.setScrollFocus(null);
				}
				clearChatInput();
			}

			return shown;
		});

		update(() -> {
			if(input.keyTap(Binding.chat) && settings.getBool("console") && (scene.getKeyboardFocus() == chatfield || scene.getKeyboardFocus() == null)){
				toggle();
			}

			if(open){
				if(needProposals) {
					if(input.keyTap(Binding.chatHistoryPrev) && historyPos < history.size - 1){ // TODO
					}
	                if(input.keyTap(Binding.chatHistoryNext) && historyPos > 0){
	                }
				} else {
					if(input.keyTap(Binding.chatHistoryPrev) && historyPos < history.size - 1){
						if(historyPos == 0) history.set(0, chatfield.getText());
						historyPos++;
						updateChat();
					}
	                if(input.keyTap(Binding.chatHistoryNext) && historyPos > 0){
	                    historyPos--;
	                    updateChat();
	                }
				}
//				ConsoleFragment
//				if(input.keyTap(KeyCode.controlLeft) && input.keyTap(KeyCode.space)){ TODO
//					needProposals = !needProposals;
//				}
			}

			scrollPos = (int)Mathf.clamp(scrollPos + input.axis(Binding.chatScroll), 0, Math.max(0, messages.size));
		});

		history.insert(0, "");
		setup();
		
		chatfield.setTextFieldListener((t, c) -> {
//			if(proposals.size == 0) {
//				proposals.add(Vars.player.name);
//				proposals.add("Agzam");
//			}
//			createProposals();
//			proposals.add("Agzam");
//			Log.info("Text changed: " + c); // FIXME: CTRL+V
		});
	}

	public void build(Group parent){
		scene.add(this);
	}

	public void clearMessages(){
		messages.clear();
		history.clear();
		history.insert(0, "");
	}
	
	private static Seq<Class<?>> classes = null;
	
	private static Seq<Class<?>> getClassList() {
		if(classes == null) {
//			classes = new Seq<Class<?>>();
			try {
				String[] clses = {"Vars"}; // TODO
				classes = new Seq<Class<?>>();
				for (String name : clses) {
					classes.add(Class.forName("mindustry." + name));
				}
//				URL resources = Vars.mods.mainLoader().getResource("mindustry");//				Vars.mods.getScripts().context.clas
////				sun.misc.Launcher;
//				Fi.get(resources.getPath());
//				// Reflect.get(Vars.mods.getScripts().context.getApplicationClassLoader(), "classes")
//				
//				Seq<Class<?>> clses = new Seq<>();
//				ModClassLoader loader = (ModClassLoader) Vars.mods.mainLoader();
//			    Seq<ClassLoader> children = Reflect.get(loader, "children");
//			    
//			    // Reflect.get(Vars.mods.mainLoader(), "children")
//				Field f = ClassLoader.class.getDeclaredField("classes");
//				f.setAccessible(true);
////				mindustry.core.Platform;
//			    for (ClassLoader cl : children) {
//			    	try {
//				    	Iterable<Class<?>> iterable = Reflect.get(cl, "classes");
//				    	for (Class<?> cls : iterable) {
//				    		clses.add(cls);
//						}
//					} catch (Exception e) {
//						Log.err(e);
//					}
//				}
				Log.info(classes);
			} catch (Exception e) {
				Log.err(e);
				classes = new Seq<Class<?>>();
			}
		}
		return classes;
	}
	
	public void createProposals() {
		proposals.clear();
		
		String text = chatfield.getText() + " ";
		boolean isString = false;
		int startIndex = 0;
		int ss = chatfield.getSelectionStart();
		String search = "<no proposals>";
		for (int i = 0; i < text.length(); i++) {
			if(text.charAt(i) == '"') {
				isString = !isString;
				continue;
			}
			if(text.charAt(i) == ' ') {
				if(i-startIndex > 1) {
					search = text.substring(startIndex, Math.max(i, 0));
					break;
				}
				if(i > ss) break;
				startIndex = i;
				continue;
			}
		}
		String[] data = search.split(".");
		if(data.length == 1) {
			for (int i = 0; i < getClassList().size; i++) {
				Class<?> cls = getClassList().get(i);
				if(cls.getName().indexOf(data[0]) != 0) {
					proposals.add(cls.getName());
				}
			}
		} else {
			
		}
		proposals.add(search);
	}

	private void setup(){
		
		fieldlabel.setStyle(new LabelStyle(fieldlabel.getStyle()));
		fieldlabel.getStyle().font = font;
		fieldlabel.setStyle(fieldlabel.getStyle());

		chatfield = new TextField("", new TextFieldStyle(scene.getStyle(TextFieldStyle.class)));
		chatfield.getStyle().background = null;
		chatfield.getStyle().fontColor = Color.white;
		chatfield.setStyle(chatfield.getStyle());

		bottom().left().marginBottom(offsety).marginLeft(offsetx * 2).add(fieldlabel).padBottom(6f);

		add(chatfield).padBottom(offsety).padLeft(offsetx).growX().padRight(offsetx).height(28);
	}

	protected void rect(float x, float y, float w, float h){
		Draw.rect("whiteui", x + w/2f, y + h/2f, w, h);
	}

	@Override
	public void draw(){
		float opacity = 1f;
		float textWidth = graphics.getWidth() - offsetx*2f;

		Draw.color(shadowColor);

		if(open){
			rect(offsetx, chatfield.y + scene.marginBottom, chatfield.getWidth() + 15f, chatfield.getHeight() - 1);
		}

		super.draw();

		float spacing = chatspace;

		chatfield.visible = open;
		fieldlabel.visible = open;

		Draw.color(shadowColor);
		Draw.alpha(shadowColor.a * opacity);

		float theight = offsety + spacing + getMarginBottom() + scene.marginBottom;
		for(int i = scrollPos; i < messages.size && i < messagesShown + scrollPos; i++){

			String strip = Strings.stripColors(messages.get(i));
			if(messages.get(i).length() < 1) return;
			String message = messages.get(i).substring(1);
			layout.setText(font, message, Color.white, textWidth, Align.bottomLeft, true);
			theight += layout.height + textspacing;
			if(i - scrollPos == 0) theight -= textspacing + 1;

			font.getCache().clear();
			font.getCache().setColor(Color.white);
			font.getCache().addText(message, fontoffsetx + offsetx, offsety + theight - textspacing*2/3f, textWidth, Align.bottomLeft, true);

			if(!open){
				font.getCache().setAlphas(opacity);
				Draw.color(0, 0, 0, shadowColor.a * opacity);
			}else{
				font.getCache().setAlphas(opacity);
			}

			if(strip.startsWith("I")) {
				Draw.color(infoShadowColor);
			} else if(strip.startsWith("W")) {
				Draw.color(warnShadowColor);
			} else if(strip.startsWith("E")) {
				Draw.color(errShadowColor);
			} else {
				Draw.color(shadowColor);
			}
			Draw.alpha(opacity * .7f);
			rect(offsetx, theight - layout.height - 2 - textspacing, textWidth + Scl.scl(4f), layout.height + textspacing);

			font.getCache().draw();
		}

		if(needProposals) {
			theight = offsety + spacing + getMarginBottom() + scene.marginBottom;
			FloatSeq glyphPositions = (FloatSeq)Reflect.get(chatfield, "glyphPositions");
			float dx = chatfield.x + (float)Reflect.get(chatfield, "textOffset") 
					+ glyphPositions.get(chatfield.getCursorPosition()) 
					- glyphPositions.get(Reflect.get(chatfield, "visibleTextStart")) + (float)Reflect.get(chatfield, "fontOffset") 
					+ font.getData().cursorX - textspacing/2f;
			
			for (int i = 0; i < proposals.size; i++) {
				layout.setText(font, proposals.get(i), Color.white, textWidth, Align.bottomLeft, true);
				theight += layout.height + textspacing;
				if(i - scrollPos == 0) theight -= textspacing + 1;
				
				font.getCache().clear();
				font.getCache().setColor(Color.white);
				font.getCache().addText(proposals.get(i), fontoffsetx + offsetx + dx, offsety + theight, textWidth, Align.bottomLeft, true);

				if(!open){
					font.getCache().setAlphas(opacity);
					Draw.color(0, 0, 0, shadowColor.a * opacity);
				} else {
					font.getCache().setAlphas(opacity);
				}
				Draw.color(borderColor);
				rect(offsetx + dx - 1, theight - layout.height - 3, textWidth + Scl.scl(4f) + 2, layout.height + textspacing + 2);
				
				Draw.color(shadowColor);
				Draw.alpha(opacity * shadowColor.a);
				rect(offsetx + dx, theight - layout.height - 2, textWidth + Scl.scl(4f), layout.height + textspacing);

				font.getCache().draw();
			}
		}
		Draw.color();
	}

	private void sendMessage(){
		String message = chatfield.getText();
		clearChatInput();

		if(message.replace(" ", "").isEmpty()) return;

		//special case for 'clear' command
		if(message.equals("clear")){
			clearMessages();
			return;
		}

		history.insert(1, message);

		addMessage("[lightgray]> " + message.replace("[", "[["));
		addMessage(mods.getScripts().runConsole(message).replace("[", "[["));
	}

	public void toggle(){

		if(!open){
			Events.fire(Trigger.openConsole);
			scene.setKeyboardFocus(chatfield);
			open = !open;
			if(mobile){
				TextInput input = new TextInput();
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
			scene.setKeyboardFocus(null);
			open = !open;
			scrollPos = 0;
			sendMessage();
		}
	}

	public void hide(){
		scene.setKeyboardFocus(null);
		open = false;
		clearChatInput();
	}

	public void updateChat(){
		chatfield.setText(history.get(historyPos));
		chatfield.setCursorPosition(chatfield.getText().length());
	}

	public void clearChatInput(){
		historyPos = 0;
		history.set(0, "");
		chatfield.setText("");
	}

	public boolean open(){
		return open;
	}

	public boolean shown(){
		return shown;
	}

	public void addMessage(String message){
		char type = ' ';
		String strip = Strings.stripColors(message);
		if(strip.startsWith("[I]")) type = 'I';
		if(strip.startsWith("[W]")) type = 'W';
		if(strip.startsWith("[E]")) type = 'E';
		
		message = type + message
				.replaceAll("\\[I\\]", "[#77B2FF]" + Iconc.warning)
				.replaceAll("\\[W\\]", "[#F2AB27]" + Iconc.warning)
				.replaceAll("\\[E\\]", "[#FF8076]" + Iconc.warning)
				.replaceAll("\r", "").replaceAll("\t", "        ");
		messages.insert(0, message);
	}
}
