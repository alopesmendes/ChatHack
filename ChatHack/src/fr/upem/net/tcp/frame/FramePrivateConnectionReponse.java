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
		ByteBuffer requested = new FramePrivateConnectionRequested(data.dataPrivateConnectionRequested).buffer();
		ByteBuffer bb = ByteBuffer.allocate(requested.remaining()+Byte.BYTES);
		bb.put(requested);
		bb.put(data.state);
		bb.flip();
		return bb;
	}

}
