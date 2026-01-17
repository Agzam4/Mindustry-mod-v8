package agzam4.io;

import java.nio.ByteBuffer;

import mindustry.Vars;

public class ByteBufferIO {

	public static void writeString(ByteBuffer buffer, String string){
        byte[] bytes = string.getBytes(Vars.charset);
        buffer.put((byte)bytes.length);
        buffer.put(bytes);
    }

	public static String readString(ByteBuffer buffer){
        byte[] bytes = new byte[buffer.get()];
        buffer.get(bytes);
        return new String(bytes, Vars.charset);
    }
    
}
