package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

class FrameGlobal implements Frame {
	
	private final ByteBuffer pseudo;
	private final ByteBuffer message;
	private static final byte ACK_GLOBAL = 1;
	private static final byte ACK_STEP = 2;
	
	public FrameGlobal(ByteBuffer pseudo, ByteBuffer message) {
		this.pseudo = pseudo;
		this.message = message;
	}
	
	@Override
	public ByteBuffer buffer() {
		ByteBuffer bb = ByteBuffer.allocate(2*Byte.BYTES+pseudo.remaining()+message.remaining());
		bb.put(ACK_GLOBAL);
		bb.put(ACK_STEP);
		bb.put(pseudo);
		bb.put(message);
		bb.flip();
		return bb;
	}

}
