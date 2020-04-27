package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.StringReader;

/**
 * <p>
 * The FramePrivateConnectionRequestReader will be use to read all the data.<br>
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public class FramePrivateConnectionRequestReader implements Reader<Data> {
	
	private enum State {
		DONE, WAITING_LOGIN_SENDER, WAITING_LOGIN_REQUEST, ERROR;
	}
	
	private State state = State.WAITING_LOGIN_SENDER;
	private final byte step;
	private String loginSender;
	private String loginRequest;
	private final StringReader stringReader;
	private Data data;
	
	/**
	 * Constructs a FramePrivateConnectionRequestReader with it's {@link Byte} and {@link ByteBuffer}.
	 * @param step a {@link Byte}.
	 * @param bb a {@link ByteBuffer}.
	 */
	public FramePrivateConnectionRequestReader(byte step, ByteBuffer bb) {
		stringReader = new StringReader(bb);
		this.step = step;
	}
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalArgumentException();
		}
		switch (state) {
			case WAITING_LOGIN_SENDER:
				ProcessStatus processLoginSender = stringReader.process();
				if (processLoginSender != ProcessStatus.DONE) {
					return processLoginSender;
				}
				loginSender = stringReader.get();
				stringReader.reset();
				state = State.WAITING_LOGIN_REQUEST;
			case WAITING_LOGIN_REQUEST:
				ProcessStatus processLoginRequest = stringReader.process();
				if (processLoginRequest != ProcessStatus.DONE) {
					return processLoginRequest;
				}
				loginRequest = stringReader.get();
				stringReader.reset();
				state = State.DONE;
				data = Data.createDataPrivateConnectionRequested(StandardOperation.PRIVATE_CONNEXION, step, loginSender, loginRequest);
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
			throw new IllegalArgumentException();
		}
		return data;
	}

	@Override
	public void reset() {
		state = State.WAITING_LOGIN_SENDER;
		stringReader.reset();
		
	}

}
