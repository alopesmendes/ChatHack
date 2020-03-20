package fr.upem.net.tcp.reader.basics;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.reader.Reader;


public class MessageReader implements Reader<Message> {
	

	private enum State {
		DONE, WAITING_LOGIN, WAITING_MSG, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITING_LOGIN;

	private final StringReader stringReader;
	private String login;
	private String msg;
	
	public MessageReader(ByteBuffer bb) {
		this.bb = bb;
		this.stringReader = new StringReader(bb);
	}

	@Override
	public ProcessStatus process() {
		// TODO Auto-generated method stub
//		if (state==State.DONE || state==State.ERROR) {
//			throw new IllegalStateException();
//		}

		switch (state) {
		case WAITING_LOGIN:
			ProcessStatus loginStatus = stringReader.process();
			if (loginStatus != ProcessStatus.DONE) {
				return loginStatus;
			}
			login = stringReader.get();
			stringReader.reset();
			state = State.WAITING_MSG;

		case WAITING_MSG:
			ProcessStatus msgStatus = stringReader.process();
			if (msgStatus != ProcessStatus.DONE) {
				return msgStatus;
			}
			msg = stringReader.get();
			stringReader.reset();
			state = State.DONE;
			return ProcessStatus.DONE;

		default:
			throw new AssertionError();
		}
	}

	@Override
	public Message get() {
		// TODO Auto-generated method stub
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new Message(login, msg);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		state = State.WAITING_LOGIN;
		stringReader.reset();
	}

}
