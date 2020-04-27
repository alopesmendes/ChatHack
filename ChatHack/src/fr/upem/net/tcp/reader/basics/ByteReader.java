package fr.upem.net.tcp.reader.basics;

import java.nio.ByteBuffer;
import fr.upem.net.tcp.reader.Reader;

/**
 * <p>
 * The ByteReader will be use to read a only one byte.<br>
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public class ByteReader implements Reader<Byte> {

	private enum State {DONE,WAITING,ERROR};
    private final ByteBuffer bb;
    private State state = State.WAITING;
    private Byte value;
    
    /**
     * Constructs a ByteReader with it's ByteBuffer.
     * <p>
     * The ByteReader will flip at the start and compact at the end after it get's the {@link Byte}.<br>
     * The method get will return a {@link Byte}.
     * </p>
     * @param bb a {@link ByteBuffer}.
     */
    public ByteReader(ByteBuffer bb) {
        this.bb = bb;
    }
    
   
    @Override
    public ProcessStatus process() {
        if (state==State.DONE || state==State.ERROR) {
            throw new IllegalStateException();
        }
        bb.flip();
        try {
            if (bb.remaining() >= Byte.BYTES) {
                value = bb.get();
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
     * @return {@link Byte}
     */
    @Override
    public Byte get() {
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
