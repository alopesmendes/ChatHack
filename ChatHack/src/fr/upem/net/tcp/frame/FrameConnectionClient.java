package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;
import java.util.Optional;

import fr.upem.net.tcp.frame.Data.DataConnectionClient;

class FrameConnectionClient implements Frame {
	
	private final DataConnectionClient data;
	
	/**
	 * @param data
	 */
	public FrameConnectionClient(DataConnectionClient data) {
		this.data = data;
	}

	@Override
	public ByteBuffer buffer() {
		ByteBuffer login = new FrameText(data.login).buffer();
		Optional<ByteBuffer> password = Optional.empty();
		int size = 2*Byte.BYTES+login.remaining();
		if (data.password.isPresent()) {
			password = Optional.of(new FrameText(data.password.get()).buffer());
			size += password.get().remaining();
		}
		ByteBuffer bb = ByteBuffer.allocate(size);
		bb.put(data.opcode.opcode()).put(data.connexion);
		bb.put(login);
		if (password.isPresent()) {
			bb.put(password.get());
		}
		bb.flip();
		return bb;
	}

}
