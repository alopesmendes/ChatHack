package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import fr.upem.net.tcp.frame.Data.DataText;

/**
 * <p>
 * The FrameText will be use to create a Frame which will create a ByteByffer with the data.<br>
 * This class will implement {@link Frame}.
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
class FrameText implements Frame {
	
	private final DataText dataText;
	
	/**
	 * Constructs a FrameText with it's DataText.
	 * @param text a {@link DataText}.
	 */
	public FrameText(DataText text) {
		this.dataText = text;
	}
	
	@Override
	public ByteBuffer buffer() {
		ByteBuffer textBuffer = StandardCharsets.UTF_8.encode(dataText.text);
		ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + textBuffer.remaining());
		bb.putInt(textBuffer.remaining());
		bb.put(textBuffer);
		bb.flip();
		return bb;
	}

}
