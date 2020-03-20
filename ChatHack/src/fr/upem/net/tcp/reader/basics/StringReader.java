package fr.upem.net.tcp.reader.basics;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.Reader.ProcessStatus;

public class StringReader implements Reader<String>{
	private enum State {
		DONE, WAITING_SIZE, WAITING_MSG, ERROR
	};

	private ByteBuffer bb;
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
				if (size <= 0 || size > 1024) {
					return ProcessStatus.ERROR;
				}
				state = State.WAITING_MSG;

			case WAITING_MSG:
				if (bb.remaining() < size) {
					return ProcessStatus.REFILL;
				}
				int oldLimit = bb.limit();
				bb.limit(bb.position() + size);
				text = (StandardCharsets.UTF_8.decode(bb).toString());
				bb.limit(oldLimit);
				state = State.DONE;
				return ProcessStatus.DONE;
				
			default:
				throw new AssertionError();
			}
		} finally {
			bb.compact();
		}
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
