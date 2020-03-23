package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataConnectionServerMdp;

class FrameConnectMdp implements Frame {

	private final DataConnectionServerMdp data;
	
	/**
	 * @param data
	 */
	public FrameConnectMdp(DataConnectionServerMdp data) {
		this.data = data;
	}

	@Override
	public ByteBuffer buffer() {
		ByteBuffer bb = ByteBuffer.allocate(1_024);

		bb.put(data.typeConnexion);
		bb.putLong(data.id);
		bb.put(new FrameText(data.login).buffer());
		if (data.password.isPresent()) {
			bb.put(new FrameText(data.password.get()).buffer());
		}
		bb.flip();
		return bb;
	}

}
