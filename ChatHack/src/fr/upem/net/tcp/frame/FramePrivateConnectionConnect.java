package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataPrivateConnectionConnect;

class FramePrivateConnectionConnect implements Frame {
	
	private final DataPrivateConnectionConnect data;
	
	/**
	 * @param data
	 */
	public FramePrivateConnectionConnect(DataPrivateConnectionConnect data) {
		this.data = data;
	}



	@Override
	public ByteBuffer buffer() {
		ByteBuffer login = new FrameText(data.login).buffer();
		ByteBuffer bb = ByteBuffer.allocate(2*Byte.BYTES + login.remaining() + Long.BYTES);
		bb.put(data.opcode.opcode()).put(data.step);
		bb.put(login);
		bb.putLong(data.token);
		bb.flip();
		return bb;
	}

}
