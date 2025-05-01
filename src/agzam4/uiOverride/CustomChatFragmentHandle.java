package agzam4.uiOverride;

import arc.scene.Group;
import arc.struct.Seq;
import mindustry.ui.fragments.ChatFragment;

public class CustomChatFragmentHandle extends ChatFragment {

	@SuppressWarnings("unused")
	private Seq<String> history = new Seq<>(); // some other mods compatibility
	
	public CustomChatFragment chatfrag;
	
	public CustomChatFragmentHandle() {
		super();
		chatfrag = new CustomChatFragment();
	}
	
	@Override
	public void build(Group parent) {
		chatfrag.build(parent);
	}
	
	@Override
    public void clearMessages(){
		chatfrag.clearMessages();
    }

	@Override
    public void toggle() {
		chatfrag.toggle();
    }

	@Override
	public void hide() {
		chatfrag.hide();
	}

	@Override
	public void updateChat() {
		chatfrag.updateChat();
	}

	@Override
	public void nextMode() {
		chatfrag.nextMode();
	}

	@Override
	public void clearChatInput() {
		chatfrag.clearChatInput();
	}

	@Override
	public void updateCursor(){
		chatfrag.updateCursor();
	}

	@Override
	public boolean shown(){
		return chatfrag.shown();
	}

	@Override
	public void addMessage(String message){
		chatfrag.addMessage(message);
	}
}