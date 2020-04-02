package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataAck;

class FrameAck implements Frame {
	
	
	private final DataAck data;
	
	
	
	/**
	 * @param data
	 */
	public FrameAck(DataAck data) {
		this.data = data;
	}



	@Override
	public ByteBuffer buffer() {
		ByteBuffer bb = ByteBuffer.allocate(2 * Byte.BYTES);
		bb.put(data.opcode.opcode()).put(data.requestCode.opcode());
		bb.flip();
		return bb;
	}
	
}
