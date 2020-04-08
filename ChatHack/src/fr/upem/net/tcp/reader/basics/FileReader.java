package fr.upem.net.tcp.reader.basics;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.reader.Reader;

public class FileReader implements Reader<ByteBuffer>{

	private enum State {
		DONE, WAITING_SIZE, WAITING_BUFFER, ERROR;
	}

	public FileReader(ByteBuffer bb) {
		this.bb = bb;
	}

	private final ByteBuffer bb;
	private State state = State.WAITING_SIZE;
	private int size;
	private ByteBuffer buffer;

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		bb.flip();
		try {
			switch (state) {
			case WAITING_SIZE:
				
				
				if (bb.remaining() < Integer.BYTES) {
					return ProcessStatus.REFILL;
				}
				size = bb.getInt();
				if (size <= 0) {
					return ProcessStatus.ERROR;
				}
				state = State.WAITING_BUFFER;
				buffer = ByteBuffer.allocate(size);
			case WAITING_BUFFER:
				ProcessStatus processTransfer = transferBytes(bb, buffer);
				if (processTransfer != ProcessStatus.DONE) {
					return processTransfer;
				}
				buffer.flip();
				state = State.DONE;
				return ProcessStatus.DONE;

			default:
				throw new AssertionError();
			}
		} finally {
			bb.compact();
		}
	}
	
	private ProcessStatus transferBytes(ByteBuffer src, ByteBuffer dst) {
		while (src.hasRemaining() && dst.hasRemaining()) {
			dst.put(src.get());
		}
		return dst.hasRemaining() ? ProcessStatus.REFILL : ProcessStatus.DONE;
	}

	@Override
	public ByteBuffer get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return buffer;
	}

	@Override
	public void reset() {
		state = State.WAITING_SIZE;

	}

}
