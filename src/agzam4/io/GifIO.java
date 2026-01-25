package agzam4.io;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.Node;

import agzam4.Packages;
import arc.files.Fi;
import arc.graphics.Color;
import arc.struct.Seq;
import arc.util.Log;

public class GifIO {

	public static boolean avalible() {
		return Packages.avalible("javax.imageio");
	}

    public static Seq<int[][]> readGifFrames(Fi file, int size) {
    	Seq<int[][]> frames = new Seq<>();
    	
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();

        try (ImageInputStream ciis = ImageIO.createImageInputStream(file.file())) {
            reader.setInput(ciis, false);
            int numberOfFrames = reader.getNumImages(true);
            final int width = reader.getWidth(0);
            final int height = reader.getHeight(0);
        	
            for (int i = 0; i < numberOfFrames; i++) {
                BufferedImage frame = reader.read(i);
                var metadata = reader.getImageMetadata(i);
                String metaFormat = metadata.getNativeMetadataFormatName();
                var root = metadata.getAsTree(metaFormat);
                var imageDescriptor = findNode(root, "ImageDescriptor");

                int left = 0;
                int top = 0;
                
                if (imageDescriptor != null) {
                    var attrs = imageDescriptor.getAttributes();
                    left = Integer.parseInt(attrs.getNamedItem("imageLeftPosition").getNodeValue());
                    top = Integer.parseInt(attrs.getNamedItem("imageTopPosition").getNodeValue());
                }
                int[][] image = new int[size][size];
             
                final int fwidth = frame.getWidth();
                final int fheight = frame.getHeight();
                for (int y = 0; y < size; y++) {
					for (int x = 0; x < size; x++) {
						int px = x*width/size - left;
						int py = y*height/size - top;
						if(px < 0 || py < 0 || px >= fwidth || py >= fheight) continue;
						
						int argb = frame.getRGB(px, py);
						int alpha = (argb >> 24) & 0xFF;
						int red = (argb >> 16) & 0xFF;
						int green = (argb >> 8) & 0xFF;
						int blue = (argb >> 0) & 0xFF;
						image[x][size-y-1] = Color.rgba8888(red/255f, green/255f, blue/255f, alpha/255f);
					}
				}
                frames.add(image);
            }
        } catch (Exception e) {
        	Log.err(e);
        } finally {
            reader.dispose();
        }
        return frames;
    }
    private static Node findNode(Node root, String name) {
        Node node = root.getFirstChild();
        while (node != null) {
            if (node.getNodeName().equals(name)) return node;
            Node child = findNode(node, name);
            if (child != null) return child;
            node = node.getNextSibling();
        }
        return null;
    }
	
}
