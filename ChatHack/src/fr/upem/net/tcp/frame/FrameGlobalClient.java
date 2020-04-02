package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataGlobalClient;

class FrameGlobalClient implements Frame {
	private final DataGlobalClient dataGlobal;
	
	public FrameGlobalClient(DataGlobalClient dataGlobal) {
		this.dataGlobal = dataGlobal;
	}
	
	@Override
	public ByteBuffer buffer() {
		ByteBuffer message = new FrameText(dataGlobal.message).buffer();
		ByteBuffer login = new FrameText(dataGlobal.login).buffer();
		ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + login.remaining() + message.remaining());
		bb.put(dataGlobal.opcode.opcode());
		bb.put(login).put(message);
		bb.flip();
		return bb;
	}

}
