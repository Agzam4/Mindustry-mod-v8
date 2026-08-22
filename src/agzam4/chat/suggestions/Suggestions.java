package agzam4.chat.suggestions;

import java.nio.ByteBuffer;
import java.util.Arrays;

import agzam4.io.ByteBufferIO;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Call;

public class Suggestions {

	public static final int maxVisibleSuggestions = 10; // XXX: Is filtered arrays must have that size and updates on "next/prev"
	public static int visibleStart = 0; // first index of "visible" range

	/** Selected suggestions (null-terminated) **/
	public static @Nullable int[] visible = new int[maxVisibleSuggestions+1];
	public static int visibleAmount = 0;
	/** Matching elements before {@code visibleStart} */
	public static int visibleOffset = 0;
	
	private static byte suggestionsId = 0;

	private static ObjectMap<Byte, Object[]> suggestions = new ObjectMap<>(); // TODO: to Object[byte][]
	
	private static ObjectMap<Byte, String> suggestionsKeys = new ObjectMap<>();
	private static ObjectMap<String, Byte> suggestionsIds = new ObjectMap<>();

	/** All text before suggestion, for example: "/command arg1 " in "/command arg1 arg2" **/
	public static String suggestionsPrefix = "";
	
	/** All text after prefix, for example: "arg2" in "/command arg1 arg2" **/
	private static String suggestionsFilter = "";

	/** Selected suggestions **/
	public static @Nullable Object[] current = null;
	/** Index in virtual array of matched suggestions (-1 if not matched) **/
	private static @Nullable int[] currentIndex = null;
	/** Amount of matched suggestions in {@link #current} **/
	public static int amount = 0;
	
	/** Index of selected suggestion in {@link #current}**/
	public static int select = -1;


	/**
	 * Processes a byte array containing suggestion data / part of suggestion data
	 * @param bs the raw byte array containing encoded suggestion data
	 */
	public static void accept(byte[] bs) {
		var buffer = ByteBuffer.wrap(bs);

		byte id = buffer.get();
		int offset = Short.toUnsignedInt(buffer.getShort());
		int size = Byte.toUnsignedInt(buffer.get());
		int totalSize = Short.toUnsignedInt(buffer.getShort());

		Object[] ss = getSuggestionsArray(id, totalSize);
		for (int i = 0; i < size; i++) {
			byte type = buffer.get();
			if(type == -1) {
				ss[i+offset] = ByteBufferIO.readString(buffer);
			} else {
				short cid = buffer.getShort();
				ss[i+offset] = Vars.content.getByID(ContentType.values()[Byte.toUnsignedInt(type)], cid);
			}
		}
		Log.info(loaded(ss) ? "accepted [@]" : "accepted part [@]", id);
	}
	
	private static Object[] getSuggestionsArray(byte id, int size) {
		Object[] arr = suggestions.get(id);
		if(arr == null || arr.length != size) {
			arr = new Object[size];
			suggestions.put(id, arr);
		}
		return arr;
	}
	
	private static byte createId(String text) {
		Byte oldId = suggestionsIds.get(text);
		if(oldId != null) suggestionsKeys.remove(oldId);
		final byte id = suggestionsId++;
		suggestionsKeys.put(id, text);
		suggestionsIds.put(text, id);
		Log.info("Created id=@ for '@'", id, text);
		return id;
	}

	/**
	 * 
	 * @param text - empty means commands list suggestions<br>/command [args...]
	 * @return id of suggestions
	 */
	private static byte suggestionsId(final String text, final boolean force) {
		Byte b = suggestionsIds.get(text);
		
		boolean correct = b != null && suggestionsKeys.get(b).equals(text);
		if(!force && correct) return b; // suggestions id found, stored key not wrong, return it
		
		// Request suggestion from server
		byte id = force && correct ? b : createId(text); // Creating id only at request
		Log.info("Request suggestion from server");
		
		if(text.isEmpty()) { // Request commands list suggestions
			var res = ByteBuffer.allocate(5);
			Log.info("Comands-list: #@", id);
			res.put(id);
			ByteBufferIO.writeString(res, "");
			Call.serverBinaryPacketUnreliable("agzam4.cmd-sug", res.array());
			return id;
		}
		
//		if(!text.endsWith(" ")) return;
		int index = text.indexOf(' ');
		String type = index == -1 ? text.substring(1) : text.substring(1, index); // first sequence before space
		
		int tmp = 0;
		for (int i = index+1; i < text.length(); i++) if(text.charAt(i) == ' ') tmp++; // Amount of arguments

		String[] args = new String[tmp];
		
		int argId = 0;
		tmp = index+1;
		
		for (int i = index+1; i < text.length(); i++) {
			if(text.charAt(i) == ' ') {
				args[argId++] = text.substring(tmp, i);
				tmp = i+1;
			}
		}
		
		Log.info("args: @ for '@'", Arrays.toString(args), text);
//		suggestionsIds.put(command.substring(0, command.lastIndexOf(' ')), id);

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
		Log.info("request");
		Call.serverBinaryPacketUnreliable("agzam4.cmd-sug", res.array());
		return id;
	}
	
	public static boolean update(String command, boolean force) {
		if(!command.startsWith("/")) { // Suggestions only for commands
			clearCurrent();
			return false;
		}
		
		int space = command.lastIndexOf(' ');
		if(space == -1) { // No space -> commands list suggestions
			byte id = suggestionsId("", force && command.length() == 1); // Searching
			
			var list = suggestions.get(id);
			if(loaded(list)) {
				current = list;
				suggestionsPrefix  = "/";
				suggestionsFilter = command.substring(1);
				filter();
				updateVisible();
				return true;
			}
		} else { // Space found -> argument list suggestions
			String prefix = command.substring(0, space+1);
			byte id = suggestionsId(prefix, force && command.endsWith(" "));
			var list = Suggestions.suggestions.get(id);
			if(loaded(list)) {
				current = list;
				suggestionsPrefix = prefix;
				suggestionsFilter = command.substring(space+1);
				filter();
				updateVisible();
				return true;
			}
		}
		// No suggestions, reset it
		clearCurrent();
		return false;
	}

	private static void filter() {
		if(current.length == 0) return;
		if(currentIndex == null || currentIndex.length != current.length) currentIndex = new int[current.length];
		select = Mathf.mod(select, current.length);
		for (int i = 0; i < current.length; i++) {
			if(filterSuggestion(current[select])) break;
			select++;
			select = Mathf.mod(select, current.length);
		}
		
		amount = 0;
		for (int i = 0; i < current.length; i++) {
			boolean ok = filterSuggestion(current[i]);
			currentIndex[i] =ok ? amount++ : -1;
		}
	}
	
	
	private static int indexOf(int matchedIndex, int def) {
		for (int i = 0; i < currentIndex.length; i++) {
			if(currentIndex[i] == -1) continue;
			if(currentIndex[i] == matchedIndex) return i;
		}
		return def;
	}
	
	private static void updateVisible() {
		if(amount == 0) return;
		
		if(select < visibleStart) visibleStart = select;
		
		int selectIndex = matchedIndex(select);
		int minimum = indexOf(selectIndex-maxVisibleSuggestions+1, 0);
		
		int maximim = indexOf(Math.min(selectIndex+(maxVisibleSuggestions-1), amount-1), -1); // go forward and clamp
		maximim = indexOf(maximim-(maxVisibleSuggestions-1), 0); // go back

		if(visibleStart < minimum) visibleStart = minimum;
		if(visibleStart > maximim) visibleStart = maximim;

		int index = 0;
		for (int i = visibleStart; i < current.length; i++) {
			if(!isMatched(i)) continue;
			visible[index++] = i;
			if(index >= maxVisibleSuggestions) break;
		}
		visible[index] = -1; // -1 terminator (array is maxVisibleSuggestions+1 sized)
		visibleAmount = index;
		if(visibleAmount > 0) visibleOffset = matchedIndex(visible[0]);
	}

	/**
	 * Filters a suggestion based on {@link #suggestionsFilter} value
	 * @param obj the object to be checked against the filter
	 * @return {@code true} if the object matches the filter criteria, {@code false} otherwise
	 * @see #suggestionsFilter
	 */
	private static boolean filterSuggestion(Object obj) {
		if(obj.toString().equalsIgnoreCase(suggestionsFilter)) return false;
		if(obj.toString().toLowerCase().startsWith(suggestionsFilter.toLowerCase())) return true;
		if(obj instanceof UnlockableContent content) {
			return content.localizedName.toLowerCase().startsWith(suggestionsFilter.toLowerCase());
		}
		return false;
	}

	private static boolean loaded(Object[] suggestions) {
		if(suggestions == null) return false;
		for (int i = 0; i < suggestions.length; i++) {
			if(suggestions[i] == null) return false;
		}
		return true;
	}

	public static void clear() {
		suggestions.clear();
		suggestionsIds.clear();
		suggestionsKeys.clear();
		clearCurrent();
	}
	
	public static void clearCurrent() {
		Log.info("[clearCurrent]");
		current = null;
		suggestionsFilter = "";
		suggestionsPrefix = "";
		amount = 0;
	}

	public static boolean isMatched(int index) {
		return currentIndex[index] != -1;
	}
	
	public static int matchedIndex(int index) {
		return currentIndex[index];
	}

	public static boolean matchedf(int index) {
		return filterSuggestion(current[index]);
	}
	
	public static String string(int index) {
		var suggestion = current[index];
		return (
				suggestion instanceof UnlockableContent unlock ? 
						unlock.name + ((unlock.name == unlock.localizedName ? "" : " " + unlock.localizedName) + " " + unlock.emoji()) 
						: suggestion.toString()
				);
	}

	public static String apply() {
		String add = current[select].toString();
		int space = add.indexOf(' ', 1);
		if(space != -1) add = add.substring(0, space);
		return suggestionsPrefix + add;
	}

	public static String suggestionsPrefix() {
		return suggestionsPrefix;
	}

	public static boolean has() {
		return current != null && suggestionsPrefix.length() > 0 && amount > 0;
	}

	public static void next() {
		for (int i = 0; i < current.length; i++) {
			select = Mathf.mod(select-1, current.length);
			if(isMatched(select)) break;
		}
		updateVisible();
	}

	public static void prev() {
		for (int i = 0; i < current.length; i++) {
			select = Mathf.mod(select+1, current.length);
			if(isMatched(select)) break;
		}
		updateVisible();
	}
	
}
