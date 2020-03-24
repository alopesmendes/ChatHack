package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataPrivateConnectionRequested;

class FramePrivateConnectionRequested implements Frame {
	private final DataPrivateConnectionRequested data;

	/**
	 * @param data
	 */
	public FramePrivateConnectionRequested(DataPrivateConnectionRequested data) {
		this.data = data;
	}

	@Override
	public ByteBuffer buffer() {
		ByteBuffer login = new FrameText(data.loginRequest).buffer();
		ByteBuffer bb = ByteBuffer.allocate(2*Byte.BYTES + login.remaining());
		bb.put(data.opcode.opcode()).put(data.step);
		bb.put(login);
		bb.flip();
		return bb;
	}
	
}
