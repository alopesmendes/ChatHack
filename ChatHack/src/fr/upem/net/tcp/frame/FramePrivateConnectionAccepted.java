package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataPrivateConnectionAccepted;

class FramePrivateConnectionAccepted implements Frame {
	private final DataPrivateConnectionAccepted data;
	
	

	/**
	 * @param data
	 */
	private FramePrivateConnectionAccepted(DataPrivateConnectionAccepted data) {
		this.data = data;
	}
	
	@Override
	public ByteBuffer buffer() {
		ByteBuffer encodeLogin = new FrameText(data.loginReceiver).buffer();
		ByteBuffer encodeSocket = new FrameText(data.socketAdress).buffer();
		ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES+Long.BYTES+encodeLogin.remaining()+encodeSocket.remaining());
		bb.put(data.opcode.opcode());
		bb.put(encodeLogin);
		bb.put(encodeSocket);
		bb.putLong(data.token);
		bb.flip();
		return bb;
	}

}
