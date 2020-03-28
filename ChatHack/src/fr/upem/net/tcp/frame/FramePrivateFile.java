package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data.DataPrivateFile;

class FramePrivateFile implements Frame {
	
	private final DataPrivateFile data;
	
	/**
	 * @param data
	 */
	public FramePrivateFile(DataPrivateFile data) {
		this.data = data;
	}

	@Override
	public ByteBuffer buffer() {
		ByteBuffer fileName = new FrameText(data.fileName).buffer();
		ByteBuffer login = new FrameText(data.login).buffer();
		ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + fileName.remaining() + login.remaining()
				+ Integer.BYTES + data.buff.remaining());
		
		bb.put(data.opcode.opcode());
		bb.put(login).put(fileName);
		bb.putInt(data.buff.remaining());
		bb.put(data.buff);
		data.buff.flip();
		bb.flip();
		return bb;
	}

}
