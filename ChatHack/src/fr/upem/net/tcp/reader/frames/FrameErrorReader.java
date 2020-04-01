package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;




public class FrameErrorReader implements Reader<Data> {

	private enum State {
		DONE,WAITING_OP_REQUEST, ERROR
	};	
	
	private State state = State.WAITING_OP_REQUEST;
	private final ByteReader byteReader;
	private Data data;
	private byte op_request;
	
	/**
	 * @param bb
	 */
	public FrameErrorReader(ByteBuffer bb) {
		this.byteReader = new ByteReader(bb);
	}
	
	@Override
	public ProcessStatus process() {
		switch(state) {
		case WAITING_OP_REQUEST:
			ProcessStatus requestStatus = byteReader.process();
			if (requestStatus != ProcessStatus.DONE) {
				return requestStatus;
			}
			op_request = byteReader.get();
			var op = StandardOperation.convert(op_request);
			if (op.isEmpty()) {
				return ProcessStatus.ERROR;
			}
			byteReader.reset();
			state = State.DONE;
			data = Data.createDataError(StandardOperation.ERROR, op.get());
			return ProcessStatus.DONE;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public Data get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return data;
	}

	@Override
	public void reset() {
		state = State.WAITING_OP_REQUEST;
		byteReader.reset();
		
	}

}
