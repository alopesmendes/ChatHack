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
		ByteBuffer bb = ByteBuffer.allocate(3 * Byte.BYTES);
		bb.put(data.opcode.opcode()).put(data.requestCode).put(data.step);
		bb.flip();
		return bb;
	}

}
