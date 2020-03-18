package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

class FrameText implements Frame {
	
	private final String text;
	
	public FrameText(String text) {
		this.text = text;
	}
	
	@Override
	public ByteBuffer buffer() {
		ByteBuffer textBuffer = StandardCharsets.UTF_8.encode(text);
		ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + textBuffer.remaining());
		bb.putInt(textBuffer.remaining());
		bb.put(textBuffer);
		bb.flip();
		return bb;
	}

}
