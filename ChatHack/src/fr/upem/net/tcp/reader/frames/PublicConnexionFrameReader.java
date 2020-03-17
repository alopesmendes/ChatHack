package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Frame;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;
import fr.upem.net.tcp.reader.basics.StringReader;


public class PublicConnexionFrameReader implements Reader<Frame> {

	private enum State {
		DONE, WAITING_OP_CODE,WAITING_CONNEXION_TYPE,WAITING_LOGIN, ERROR
	};	
	
	private final ByteBuffer bb;
	private State state = State.WAITING_LOGIN;
	private final StringReader stringReader;
	private final ByteReader byteReader;
	
	private Byte op_code;
	private Byte connexionType;
	private String login;
	
	
	/**
	 * @param bb
	 */
	public PublicConnexionFrameReader(ByteBuffer bb) {
		this.bb = bb;
		this.stringReader = new StringReader(bb);
		this.byteReader = new ByteReader(bb);
	}

	
	@Override
	public ProcessStatus process() {
		if (state==State.DONE || state==State.ERROR) {
			throw new IllegalStateException();
		}
		switch (state) {
		case WAITING_OP_CODE:
		}
		
		return null;
	}

	@Override
	public Frame get() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		state = State.WAITING_LOGIN;
		stringReader.reset();
	}

}
