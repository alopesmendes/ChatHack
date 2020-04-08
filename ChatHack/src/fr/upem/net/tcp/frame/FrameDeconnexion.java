package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataDeconnexion;

public class FrameDeconnexion implements Frame {
	private final DataDeconnexion data;
	
	/**
	 * @param data
	 */
	public FrameDeconnexion(DataDeconnexion data) {
		this.data = data;
	}

	@Override
	public ByteBuffer buffer() {
		ByteBuffer login = new FrameText(data.login).buffer();
		ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + login.remaining());
		bb.put(data.opcode.opcode());
		bb.put(login);
		bb.flip();
		return bb;
	}

}
