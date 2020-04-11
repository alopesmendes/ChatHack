package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import fr.upem.net.tcp.frame.Data.DataText;

class FrameText implements Frame {
	
	private final DataText dataText;
	
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
