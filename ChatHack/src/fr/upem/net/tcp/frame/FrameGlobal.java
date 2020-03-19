package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataGlobal;

class FrameGlobal implements Frame {
	private final DataGlobal dataGlobal;
	
	public FrameGlobal(DataGlobal dataGlobal) {
		this.dataGlobal = dataGlobal;
	}
	
	@Override
	public ByteBuffer buffer() {
		ByteBuffer pseudo = new FrameText(dataGlobal.pseudo).buffer();
		ByteBuffer message = new FrameText(dataGlobal.message).buffer();
		ByteBuffer bb = ByteBuffer.allocate(2*Byte.BYTES+pseudo.remaining()+message.remaining());
		bb.put(dataGlobal.ack).put(dataGlobal.step);
		bb.put(pseudo).put(message);
		bb.flip();
		return bb;
	}

}
