package fr.upem.net.tcp.reader.basics;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import fr.upem.net.tcp.reader.Reader;

public class StringReader implements Reader<String>{
	private enum State {
		DONE, WAITING_SIZE, WAITING_MSG, ERROR
	};

	private ByteBuffer bb;
	private ByteBuffer buffer;
	private State state = State.WAITING_SIZE;
	private int size;
	private String text;
	
	public StringReader(ByteBuffer bb) {
		this.bb = bb;
	}

	@Override
	public ProcessStatus process() {
		if (state==State.DONE || state==State.ERROR) {
			System.out.println(state);
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
				buffer = ByteBuffer.allocate(size);
				state = State.WAITING_MSG;
			case WAITING_MSG:
				
				ProcessStatus processTransfer = transferBytes(bb, buffer);
				if (processTransfer != ProcessStatus.DONE) {
					return processTransfer;
				}
				buffer.flip();
				text = (StandardCharsets.UTF_8.decode(buffer).toString());
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
	public String get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return text;
	}

	@Override
	public void reset() {
		state = State.WAITING_SIZE;
	}
}
