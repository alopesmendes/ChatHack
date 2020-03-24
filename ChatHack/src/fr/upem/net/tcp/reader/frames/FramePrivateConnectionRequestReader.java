package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.StringReader;

public class FramePrivateConnectionRequestReader implements Reader<Data> {
	
	private enum State {
		DONE, WAITING_LOGIN, ERROR;
	}
	
	private State state = State.WAITING_LOGIN;
	private final byte step;
	private String login;
	private final StringReader loginReader;
	private Data data;
	
	public FramePrivateConnectionRequestReader(byte step, ByteBuffer bb) {
		loginReader = new StringReader(bb);
		this.step = step;
	}
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalArgumentException();
		}
		switch (state) {
			case WAITING_LOGIN:
				ProcessStatus processLogin = loginReader.process();
				if (processLogin != ProcessStatus.DONE) {
					return processLogin;
				}
				login = loginReader.get();
				loginReader.reset();
				state = State.DONE;
				data = Data.createDataPrivateConnectionRequested(StandardOperation.PRIVATE_CONNEXION, step, login);
				return ProcessStatus.DONE;
	
			default:
				throw new IllegalArgumentException("Unexpected value: " + state);
		}
	}

	@Override
	public Data get() {
		if (state != State.DONE) {
			throw new IllegalArgumentException();
		}
		return data;
	}

	@Override
	public void reset() {
		state = State.WAITING_LOGIN;
		loginReader.reset();
		
	}

}
