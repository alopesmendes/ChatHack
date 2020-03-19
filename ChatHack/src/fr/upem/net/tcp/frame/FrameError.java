package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataError;

class FrameError implements Frame {
	
	private final DataError dataError;
	
	public FrameError(DataError dataError) {
		this.dataError = dataError;
	}

	@Override
	public ByteBuffer buffer() {
		ByteBuffer bb = ByteBuffer.allocate(2*Byte.BYTES);
		bb.put(dataError.opcode.opcode()).put(dataError.requestCode);
		bb.flip();
		return bb;
	}

}
