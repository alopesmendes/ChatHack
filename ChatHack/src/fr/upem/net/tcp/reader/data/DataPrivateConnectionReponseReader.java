package fr.upem.net.tcp.reader.data;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;
import fr.upem.net.tcp.reader.basics.StringReader;

public class DataPrivateConnectionReponseReader implements Reader<Data> {
	private enum State {
		DONE, WAITING_LOGIN, WAITING_STATE, ERROR;
	}
	
	private State state = State.WAITING_LOGIN;
	private final byte step;
	private byte status;
	private ByteReader byteReader;
	private StringReader loginReader;
	private String login;
	private Data data;
	
	public DataPrivateConnectionReponseReader(byte step, ByteBuffer bb) {
		this.step = step;
		byteReader = new ByteReader(bb);
		loginReader = new StringReader(bb);
	}
	
	@Override
	public ProcessStatus process() {
		switch (state) {
			case WAITING_LOGIN:
				ProcessStatus processLogin = loginReader.process();
				if (processLogin != ProcessStatus.DONE) {
					return processLogin;
				}
				login = loginReader.get();
				loginReader.reset();
				state = State.WAITING_STATE;
			case WAITING_STATE:
				ProcessStatus processStatus = byteReader.process();
				if (processStatus != ProcessStatus.DONE) {
					return processStatus;
				}
				status = byteReader.get();
				byteReader.reset();
				data = Data.createDataPrivateConnectionReponse(StandardOperation.PRIVATE_CONNEXION, step, login, status);
				state = State.DONE;
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
		byteReader.reset();
		loginReader.reset();
		
	}

}
