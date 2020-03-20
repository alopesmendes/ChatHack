package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;


import fr.upem.net.tcp.frame.Frame;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;




public class FrameErrorReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING_OP_CODE,WAITING_OP_REQUEST, ERROR
	};	
	
	private final ByteBuffer bb;
	private State state = State.WAITING_OP_CODE;
	private final ByteReader byteReader;
	
	private byte op_code;
	private byte op_request;
	
	/**
	 * @param bb
	 */
	public FrameErrorReader(ByteBuffer bb) {
		this.bb = bb;
		this.byteReader = new ByteReader(bb);
	}
	
	@Override
	public ProcessStatus process() {
		switch(state) {
		case WAITING_OP_CODE:
			ProcessStatus opStatus = byteReader.process();
			if (opStatus != ProcessStatus.DONE) {
				return opStatus;
			}
			op_code = byteReader.get();
			byteReader.reset();
			state = State.WAITING_OP_REQUEST;
		case WAITING_OP_REQUEST:
			ProcessStatus requestStatus = byteReader.process();
			if (requestStatus != ProcessStatus.DONE) {
				return requestStatus;
			}
			op_request = byteReader.get();
			byteReader.reset();
			state = State.DONE;
			return ProcessStatus.DONE;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		//return Frame.createFrameError();
		return null;
	}

	@Override
	public void reset() {
		state = State.WAITING_OP_CODE;
		byteReader.reset();
		
	}

}
