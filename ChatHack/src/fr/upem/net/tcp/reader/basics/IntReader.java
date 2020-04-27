package fr.upem.net.tcp.reader.basics;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.reader.Reader;
/**
 * <p>
 * The IntReader will be use to read an {@link Integer}.<br>
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public class IntReader implements Reader<Integer> {

    private enum State {DONE,WAITING,ERROR};

    private final ByteBuffer bb;
    private State state = State.WAITING;
    private int value;
    
	/**
	 * Constructs a IntReader with it's {@link ByteBuffer}.
	 * <p>
     * The IntReader will flip at the start and compact at the end after it gets an {@link Integer}.<br>
     * The method get will return a {@link ByteBuffer}.
     * </p>
	 * @param bb a {@link ByteBuffer}.
	 */
    public IntReader(ByteBuffer bb) {
        this.bb = bb;
    }

    @Override
    public ProcessStatus process() {
        if (state==State.DONE || state==State.ERROR) {
            throw new IllegalStateException();
        }
        bb.flip();
        try {
            if (bb.remaining() >= Integer.BYTES) {
                value = bb.getInt();
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
     * @return {@link Integer}
     */
    @Override
    public Integer get() {
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
