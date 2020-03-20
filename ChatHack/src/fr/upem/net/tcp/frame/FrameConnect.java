package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;
import java.util.Optional;

class FrameConnect implements Frame {
	
	private final byte connect;
	private final ByteBuffer login;
	private final Optional<ByteBuffer> password;
	
	public FrameConnect(byte connect, ByteBuffer login, Optional<ByteBuffer> password) {
		this.connect = connect;
		this.login = login;
		this.password = password;
	}
	
	@Override
	public ByteBuffer buffer() {
		int p = password.isEmpty() ? 0 : password.get().remaining();
		ByteBuffer bb = ByteBuffer.allocate(2*Byte.BYTES + login.remaining() + p);
		bb.put((byte)0);
		bb.put(connect);
		bb.put(login);
		if (password.isPresent()) {
			bb.put(password.get());
		}
		bb.flip();
		return bb;
	}

}
