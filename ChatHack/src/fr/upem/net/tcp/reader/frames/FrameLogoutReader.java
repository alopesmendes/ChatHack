package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.StringReader;

/**
 * <p>
 * The FrameLogoutReader will be use to read the data.<br>
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public class FrameLogoutReader implements Reader<Data> {
	
	private static enum State {
		DONE, WAITING_LOGIN, ERROR;
	}
	
	private Data data;
	private State state = State.WAITING_LOGIN;
	private final StringReader stringReader;
	private String login;
	
	/**
	 * Constructs a FrameLogoutReader with it's {@link ByteBuffer}.
	 * @param bb a {@link ByteBuffer}.
	 */
	public FrameLogoutReader(ByteBuffer bb) {
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
			data = Data.createDataLogout(StandardOperation.LOGOUT, login);
			return ProcessStatus.DONE;

		default:
			throw new AssertionError();
		}
	}

	/**
	 * @return {@link Data}
	 */
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
