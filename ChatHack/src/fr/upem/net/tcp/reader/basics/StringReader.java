package fr.upem.net.tcp.reader.basics;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import fr.upem.net.tcp.reader.Reader;

/**
 * <p>
 * The StringReader will be use to read an {@link String}.<br>
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public class StringReader implements Reader<String>{
	private enum State {
		DONE, WAITING_SIZE, WAITING_MSG, ERROR
	};

	private ByteBuffer bb;
	private ByteBuffer buffer;
	private State state = State.WAITING_SIZE;
	private int size;
	private String text;

	/**
	 * Constructs a StringReader with it's {@link ByteBuffer}.
	 * <p>
     * The StringReader will flip at the start and compact at the end after it gets an {@link String}.<br>
     * The method get will return a {@link ByteBuffer}.
     * </p>
	 * @param bb a {@link ByteBuffer}.
	 */
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
	/**
	 * Will transfer every byte of src until dst is filled.
	 * @param src a {@link ByteBuffer}.
	 * @param dst a {@link ByteBuffer}
	 * @return DONE if dst is filled otherwise REFILL.
	 */
	private ProcessStatus transferBytes(ByteBuffer src, ByteBuffer dst) {
		while (src.hasRemaining() && dst.hasRemaining()) {
			dst.put(src.get());
		}
		return dst.hasRemaining() ? ProcessStatus.REFILL : ProcessStatus.DONE;
	}

	/**
	 * @return {@link String}
	 */
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
