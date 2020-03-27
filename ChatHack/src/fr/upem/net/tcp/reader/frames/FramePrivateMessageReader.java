package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.StringReader;

public class FramePrivateMessageReader implements Reader<Data> {
	
	private enum State {
		DONE, WAITING_LOGIN, WAITING_TEXT, ERROR;
	}
	
	private State state = State.WAITING_LOGIN;
	private final StringReader stringReader;
	private String login;
	private String message;
	private Data data;
	
	public FramePrivateMessageReader(ByteBuffer bb) {
		stringReader = new StringReader(bb);
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
			state = State.WAITING_TEXT;
			stringReader.reset();
		case WAITING_TEXT:
			ProcessStatus processText = stringReader.process();
			if (processText != ProcessStatus.DONE) {
				return processText;
			}
			message = stringReader.get();
			state = State.DONE;
			stringReader.reset();
			data = Data.createDataPrivateMessage(StandardOperation.PRIVATE_MESSAGE, login, message);
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
		
	}

}
