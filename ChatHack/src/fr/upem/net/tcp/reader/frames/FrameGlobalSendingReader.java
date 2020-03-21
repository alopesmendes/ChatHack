package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.Frame;
import fr.upem.net.tcp.frame.FrameVisitor;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;
import fr.upem.net.tcp.reader.basics.StringReader;


public class FrameGlobalSendingReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING_OP_CODE,WAITING_STEP, WAITING_TEXT, ERROR
	};	
	
	private State state = State.WAITING_OP_CODE;
	private final ByteReader byteReader;
	private final StringReader stringReader;
	private final String login;
	private byte op_code;
	private byte step;
	private String text;
	private Frame frame;
	
	/**
	 * @param bb
	 */
	public FrameGlobalSendingReader(ByteBuffer bb, String login) {
		this.stringReader = new StringReader(bb);
		this.byteReader = new ByteReader(bb);
		this.login = login;
	}
	
	
	@Override
	public ProcessStatus process(FrameVisitor fv) {
		switch (state) {
		case WAITING_OP_CODE:
			ProcessStatus opStatus = byteReader.process(fv);
			if (opStatus != ProcessStatus.DONE) {
				return opStatus;
			}
			op_code = byteReader.get();
			if (op_code != StandardOperation.GLOBAL_MESSAGE.opcode()) {
				return ProcessStatus.ERROR;
			}
			byteReader.reset();
			state = State.WAITING_STEP;
			
		case WAITING_STEP:
			ProcessStatus stepStatus = byteReader.process(fv);
			if (stepStatus != ProcessStatus.DONE) {
				return stepStatus;
			}
			step = byteReader.get();
			byteReader.reset();
			state = State.WAITING_TEXT;
			
		case WAITING_TEXT:
			ProcessStatus textStatus = stringReader.process(fv);
			if (textStatus != ProcessStatus.DONE) {
				return textStatus;
			}
			text = stringReader.get();
			stringReader.reset();
			state = State.DONE;
			frame = fv.call(Data.createDataGlobalServer(StandardOperation.GLOBAL_MESSAGE, step, login, text));
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
		return frame;
	}

	@Override
	public void reset() {
		state = State.WAITING_OP_CODE;
		stringReader.reset();
		byteReader.reset();
		
	}

}
