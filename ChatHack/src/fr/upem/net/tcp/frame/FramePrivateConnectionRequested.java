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
		ByteBuffer firstClient = new FrameText(data.firstClient).buffer();
		ByteBuffer secondClient = new FrameText(data.secondClient).buffer();
		ByteBuffer bb = ByteBuffer.allocate(2*Byte.BYTES + secondClient.remaining() +  firstClient.remaining());
		bb.put(data.opcode.opcode()).put(data.step);
		bb.put(firstClient).put(secondClient);
		bb.flip();
		return bb;
	}
	
}
