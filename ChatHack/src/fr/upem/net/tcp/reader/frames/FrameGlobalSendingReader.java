package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Frame;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;
import fr.upem.net.tcp.reader.basics.StringReader;


public class FrameGlobalSendingReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING_OP_CODE,WAITING_STEP, WAITING_TEXT, ERROR
	};	
	
	private final ByteBuffer bb;
	private State state = State.WAITING_OP_CODE;
	private final ByteReader byteReader;
	private final StringReader stringReader;
	
	private byte op_code;
	private byte step;
	private String text;
	
	/**
	 * @param bb
	 */
	public FrameGlobalSendingReader(ByteBuffer bb) {
		this.bb = bb;
		this.stringReader = new StringReader(bb);
		this.byteReader = new ByteReader(bb);
	}
	
	
	@Override
	public ProcessStatus process() {
		switch (state) {
		case WAITING_OP_CODE:
			ProcessStatus opStatus = byteReader.process();
			if (opStatus != ProcessStatus.DONE) {
				return opStatus;
			}
			op_code = byteReader.get();
			byteReader.reset();
			state = State.WAITING_STEP;
			
		case WAITING_STEP:
			ProcessStatus stepStatus = byteReader.process();
			if (stepStatus != ProcessStatus.DONE) {
				return stepStatus;
			}
			step = byteReader.get();
			byteReader.reset();
			state = State.WAITING_TEXT;
			
		case WAITING_TEXT:
			ProcessStatus textStatus = stringReader.process();
			if (textStatus != ProcessStatus.DONE) {
				return textStatus;
			}
			text = stringReader.get();
			stringReader.reset();
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
		return null;
	}

	@Override
	public void reset() {
		state = State.WAITING_OP_CODE;
		stringReader.reset();
		byteReader.reset();
		
	}

}
