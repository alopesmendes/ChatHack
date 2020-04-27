package fr.upem.net.tcp.reader.basics;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.reader.Reader;

/**
 * <p>
 * The LongReader will be use to read {@link Long}.<br>
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public class LongReader implements Reader<Long> {

	
	private enum State {DONE,WAITING,ERROR};

    private final ByteBuffer bb;
    private State state = State.WAITING;
    private long value;
    
    
	/**
	 * Constructs a LongReader with it's {@link ByteBuffer}.
	 * <p>
     * The LongReader will flip at the start and compact at the end after it gets an {@link Long}.<br>
     * The method get will return a {@link ByteBuffer}.
     * </p>
	 * @param bb a {@link ByteBuffer}.
	 */
	public LongReader(ByteBuffer bb) {
		this.bb = bb;
	}

	@Override
	public ProcessStatus process() {
		if (state==State.DONE || state==State.ERROR) {
            throw new IllegalStateException();
        }
        bb.flip();
        try {
            if (bb.remaining() >= Long.BYTES) {
                value = bb.getLong();
                state = State.DONE;
                return ProcessStatus.DONE;
            } else {
                return ProcessStatus.REFILL;
            }
        } finally {
            bb.compact();
        }
	}

	/**
	 * @return {@link Long}.
	 */
	@Override
	public Long get() {
		if (state!=State.DONE) {
            throw new IllegalStateException();
        }
        return value;
	}

	@Override
	public void reset() {
		 state=State.WAITING;

	}

}
