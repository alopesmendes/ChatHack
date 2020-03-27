package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.LongReader;
import fr.upem.net.tcp.reader.basics.StringReader;

public class FramePrivateConnectionConnectReader implements Reader<Data> {
	
	private enum State {
		DONE, WAITING_LOGIN, WAITING_TOKEN, ERROR;
	}
	
	private final StringReader stringReader;
	private final LongReader longReader;
	private String login;
	private long token;
	private Data data;
	private State state = State.WAITING_LOGIN;
	private final byte step;
	
	public FramePrivateConnectionConnectReader(byte step, ByteBuffer bb) {
		this.step = step;
		stringReader = new StringReader(bb);
		longReader = new LongReader(bb);
	}
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalArgumentException();
		}
		switch (state) {
		case WAITING_LOGIN:
			ProcessStatus processLogin = stringReader.process();
			if (processLogin != ProcessStatus.DONE) {
				return processLogin;
			}
			login = stringReader.get();
			stringReader.reset();
			state = State.WAITING_TOKEN;
		case WAITING_TOKEN:
			ProcessStatus processToken = longReader.process();
			if (processToken != ProcessStatus.DONE) {
				return processToken;
			}
			token = longReader.get();
			longReader.reset();
			state = State.DONE;
			data = Data.createDataPrivateConnectionConnect(StandardOperation.PRIVATE_CONNEXION, step, login, token);
			return ProcessStatus.DONE;

		default:
			throw new IllegalArgumentException("Unexpected value: " + state);
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
		longReader.reset();
		
	}

}
