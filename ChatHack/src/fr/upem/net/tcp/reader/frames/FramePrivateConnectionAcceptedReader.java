package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.IntReader;
import fr.upem.net.tcp.reader.basics.LongReader;
import fr.upem.net.tcp.reader.basics.StringReader;

public class FramePrivateConnectionAcceptedReader implements Reader<Data> {
	
	private enum State {
		DONE, WAITING_LOGIN, WAITING_PORT, WAITING_HOST, WAITING_TOKEN, ERROR;
	}
	
	private State state = State.WAITING_LOGIN;
	private final byte step;
	private StringReader stringReader;
	private IntReader intReader;
	private LongReader longReader;
	private String login;
	private int port;
	private String host;
	private Data data;
	
	public FramePrivateConnectionAcceptedReader(byte step, ByteBuffer bb) {
		this.step = step;
		stringReader = new StringReader(bb);
		intReader = new IntReader(bb);
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
				state = State.WAITING_PORT;
			case WAITING_PORT:
				ProcessStatus processPort = intReader.process();
				if (processPort != ProcessStatus.DONE) {
					return processPort;
				}
				port = intReader.get();
				intReader.reset();
				state = State.WAITING_HOST;
			case WAITING_HOST:
				ProcessStatus processHost = stringReader.process();
				if (processHost != ProcessStatus.DONE) {
					return processHost;
				}
				host = stringReader.get();
				stringReader.reset();
				state = State.WAITING_TOKEN;
			case WAITING_TOKEN:
				ProcessStatus processToken = longReader.process();
				if (processToken != ProcessStatus.DONE) {
					return processToken;
				}
				long token = longReader.get();
				longReader.reset();
				state = State.DONE;
				data = Data.createDataPrivateConnectionAccepted(StandardOperation.PRIVATE_CONNEXION, step, login, port, host, token);
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
		stringReader.reset();
		intReader.reset();
		longReader.reset();
		
	}

}
