package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.StringReader;

public class FrameDeconnexionReader implements Reader<Data> {
	
	private static enum State {
		DONE, WAITING_LOGIN, ERROR;
	}
	
	private Data data;
	private State state = State.WAITING_LOGIN;
	private final StringReader stringReader;
	private String login;
	
	public FrameDeconnexionReader(ByteBuffer bb) {
		stringReader = new StringReader(bb);
	}
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		switch (state) {
		case WAITING_LOGIN:
			ProcessStatus processLogin = stringReader.process();
			if (processLogin != ProcessStatus.DONE) {
				return processLogin;
			}
			login = stringReader.get();
			stringReader.reset();
			state = State.DONE;
			data = Data.createDataDeconnexion(StandardOperation.DECONNEXION, login);
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
		state = State.WAITING_LOGIN;
		stringReader.reset();
		
	}
	
}
