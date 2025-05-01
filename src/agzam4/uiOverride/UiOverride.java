package agzam4.uiOverride;

import agzam4.ModWork;
import arc.Core;
import mindustry.Vars;
import mindustry.ui.fragments.ChatFragment;
import mindustry.ui.fragments.ConsoleFragment;

public class UiOverride {

	public static CustomChatFragmentHandle customChatFragment;
	public static ChatFragment oldChatFragment;

	public static CustomConsoleFragmentHandle customConsoleFragment;
	public static ConsoleFragment oldConsoleFragment;
	
	public static void init() {
		oldChatFragment = Vars.ui.chatfrag;
		oldConsoleFragment = Vars.ui.consolefrag;
		
		set();
	}
	
	public static void set() {
		if(ModWork.setting("custom-chat-fragment")) {
			if(customChatFragment == null) customChatFragment = new CustomChatFragmentHandle();
			Core.scene.root.removeChild(Vars.ui.chatfrag);
			Vars.ui.chatfrag = customChatFragment;
			Vars.ui.chatfrag.build(Core.scene.root);
		} else {
			if(customChatFragment != null) {
				Core.scene.root.removeChild(customChatFragment);
				Core.scene.root.removeChild(customChatFragment.chatfrag);
			}
			Core.scene.root.removeChild(Vars.ui.chatfrag);
			Vars.ui.chatfrag = oldChatFragment;
			Vars.ui.chatfrag.build(Core.scene.root);
		}
		

//		if(ModWork.setting("custom-chat-fragment")) {
			if(customConsoleFragment == null) customConsoleFragment = new CustomConsoleFragmentHandle();
			Core.scene.root.removeChild(Vars.ui.consolefrag);
			Vars.ui.consolefrag = customConsoleFragment;
			Vars.ui.consolefrag.build(Core.scene.root);
//		} else {
//			if(customChatFragment != null) {
//				Core.scene.root.removeChild(customChatFragment);
//				Core.scene.root.removeChild(customChatFragment.chatfrag);
//			}
//			Core.scene.root.removeChild(Vars.ui.chatfrag);
//			Vars.ui.chatfrag = oldChatFragment;
//			Vars.ui.chatfrag.build(Core.scene.root);
//		}
		
	}

}
