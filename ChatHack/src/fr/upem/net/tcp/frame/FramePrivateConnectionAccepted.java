package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataPrivateConnectionAccepted;

class FramePrivateConnectionAccepted implements Frame {
	private final DataPrivateConnectionAccepted data;
	
	

	/**
	 * @param data
	 */
	public FramePrivateConnectionAccepted(DataPrivateConnectionAccepted data) {
		this.data = data;
	}
	
	@Override
	public ByteBuffer buffer() {
		ByteBuffer encodeLogin = new FrameText(data.loginReceiver).buffer();
		ByteBuffer encodeSocket = new FrameText(data.host).buffer();
		ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + 2*Byte.BYTES + Long.BYTES + encodeLogin.remaining() + encodeSocket.remaining());
		bb.put(data.opcode.opcode());
		bb.put(data.step);
		bb.put(encodeLogin);
		bb.putInt(data.port);
		bb.put(encodeSocket);
		bb.putLong(data.token);
		bb.flip();
		return bb;
	}

}
