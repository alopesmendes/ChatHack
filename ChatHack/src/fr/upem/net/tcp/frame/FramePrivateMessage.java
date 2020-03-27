package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataPrivateMessage;

class FramePrivateMessage implements Frame {
	
	private final DataPrivateMessage data;
	
	/**
	 * @param data
	 */
	public FramePrivateMessage(DataPrivateMessage data) {
		this.data = data;
	}

	@Override
	public ByteBuffer buffer() {
		ByteBuffer messageEncode = new FrameText(data.message).buffer();
		ByteBuffer loginEncode = new FrameText(data.login).buffer();
		ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES+messageEncode.remaining()+loginEncode.remaining());
		bb.put(data.opcode.opcode());
		bb.put(loginEncode).put(messageEncode);
		bb.flip();
		return bb;
	}

}
