package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.LongReader;

/**
 * <p>
 * The FrameMdpReader will be use to read all the data.<br>
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public class FrameMdpReader implements Reader<Data> {
	
	private enum State {
		DONE,WAITING_ID, ERROR
	};	
	
	private State state = State.WAITING_ID;
	private final LongReader longReader;
	
	private byte op_code;
	private long id;
	private Data data;
	
	
	/**
	 * Constructs a FrameMdpReader with it's a {@link ByteBuffer} and a {@link Byte}.
	 * @param bb a {@link ByteBuffer}.
	 * @param op_code a {@link Byte}.
	 */
	public FrameMdpReader(ByteBuffer bb, byte op_code) {
		this.longReader = new LongReader(bb);
		this.op_code = op_code;
	}
	
	@Override
	public ProcessStatus process() {
		switch(state) {
		case WAITING_ID:
			ProcessStatus requestStatus = longReader.process();
			if (requestStatus != ProcessStatus.DONE) {
				return requestStatus;
			}
			id = longReader.get();
			longReader.reset();
			state = State.DONE;
			data = Data.createDataConnectionServerMdpReponse(op_code, id);
			return ProcessStatus.DONE;
		default:
			throw new AssertionError();
		}
	}

	/**
	 * @return {@link Data}
	 */
	@Override
	public Data get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return data;
	}

	@Override
	public void reset() {
		state = State.WAITING_ID;
		longReader.reset();
		
	}

}
