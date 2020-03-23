package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataConnectionServerMdpReponse;

class FrameConnectMdpServer implements Frame {
	
	private final DataConnectionServerMdpReponse data;
	
	
	/**
	 * @param data
	 */
	public FrameConnectMdpServer(DataConnectionServerMdpReponse data) {
		this.data = data;
	}


	@Override
	public ByteBuffer buffer() {
		ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES+Long.BYTES);
		bb.put(data.opcode);
		bb.putLong(data.id);
		bb.flip();
		return bb;
	}

}
