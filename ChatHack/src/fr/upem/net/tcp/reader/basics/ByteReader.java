package fr.upem.net.tcp.reader.basics;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.FrameVisitor;
import fr.upem.net.tcp.reader.Reader;

public class ByteReader implements Reader<Byte> {

	private enum State {DONE,WAITING,ERROR};

    private final ByteBuffer bb;
    private State state = State.WAITING;
    private Byte value;

    public ByteReader(ByteBuffer bb) {
        this.bb = bb;
    }

    @Override
    public ProcessStatus process(FrameVisitor fv) {
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
