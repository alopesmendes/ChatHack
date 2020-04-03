package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataPrivateConnectionReponse;

class FramePrivateConnectionReponse implements Frame {

	private final DataPrivateConnectionReponse data;
	
	/**
	 * @param data
	 */
	public FramePrivateConnectionReponse(DataPrivateConnectionReponse data) {
		this.data = data;
	}

	@Override
	public ByteBuffer buffer() {
		ByteBuffer firstClient = new FrameText(data.firstClient).buffer();
		ByteBuffer secondClient = new FrameText(data.secondClient).buffer();
		ByteBuffer bb = ByteBuffer.allocate(3*Byte.BYTES + firstClient.remaining() + secondClient.remaining());
		bb.put(data.opcode.opcode()).put(data.step);
		bb.put(firstClient).put(secondClient);
		bb.put(data.state);
		bb.flip();
		return bb;
	}

}
