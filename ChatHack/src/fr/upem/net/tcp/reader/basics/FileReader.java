package fr.upem.net.tcp.reader.basics;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.reader.Reader;

/**
 * <p>
 * The FileReader will be use to read all the bytes of a File.<br>
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public class FileReader implements Reader<ByteBuffer>{

	private enum State {
		DONE, WAITING_SIZE, WAITING_BUFFER, ERROR;
	}

	/**
	 * Constructs a FileReader with it's {@link ByteBuffer}.
	 * <p>
     * The FileReader will flip at the start and compact at the end after it gets all bytes.<br>
     * The method get will return a {@link ByteBuffer}.
     * </p>
	 * @param bb a {@link ByteBuffer}.
	 */
	public FileReader(ByteBuffer bb) {
		this.bb = bb;
	}

	private final ByteBuffer bb;
	private State state = State.WAITING_SIZE;
	private int size;
	private ByteBuffer buffer;

	/**
	 * {@inheritDoc}
	 * Will first get the size.
	 * Then will create a {@link ByteBuffer} of the correct size.
	 * Will transfer every byte to our buffer.
	 * If the buffer is not fill this method will return REFILL, otherwise DONE.
	 */
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
	 * @return {@link ByteBuffer}
	 */
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
