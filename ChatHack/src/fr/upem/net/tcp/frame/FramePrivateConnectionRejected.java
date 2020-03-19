package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataPrivateConnectionRejected;

class FramePrivateConnectionRejected implements Frame {
	
	private final DataPrivateConnectionRejected data;
	
	/**
	 * @param data
	 */
	private FramePrivateConnectionRejected(DataPrivateConnectionRejected data) {
		this.data = data;
	}

	@Override
	public ByteBuffer buffer() {
		ByteBuffer encodeLogin = new FrameText(data.loginReceiver).buffer();
		ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES+encodeLogin.remaining());
		bb.put(data.opcode.opcode());
		bb.put(encodeLogin);
		bb.flip();
		return bb;
	}

}
