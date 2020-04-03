package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;
import fr.upem.net.tcp.reader.basics.StringReader;

public class FramePrivateConnectionReponseReader implements Reader<Data> {
	private enum State {
		DONE, WAITING_FIRST_CLIENT, WAITING_SECOND_CLIENT, WAITING_STATE, ERROR;
	}
	
	private State state = State.WAITING_FIRST_CLIENT;
	private final byte step;
	private byte status;
	private ByteReader byteReader;
	private StringReader stringReader;
	private String firstClient;
	private String secondClient;
	private Data data;
	
	public FramePrivateConnectionReponseReader(byte step, ByteBuffer bb) {
		this.step = step;
		byteReader = new ByteReader(bb);
		stringReader = new StringReader(bb);
	}
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		
		switch (state) {
			case WAITING_FIRST_CLIENT:
				ProcessStatus processLogin = stringReader.process();
				if (processLogin != ProcessStatus.DONE) {
					return processLogin;
				}
				firstClient = stringReader.get();
				stringReader.reset();
				state = State.WAITING_SECOND_CLIENT;
			case WAITING_SECOND_CLIENT:
				ProcessStatus processLoginReceive = stringReader.process();
				if (processLoginReceive != ProcessStatus.DONE) {
					return processLoginReceive;
				}
				secondClient = stringReader.get();
				stringReader.reset();
				state = State.WAITING_STATE;
			case WAITING_STATE:
				ProcessStatus processStatus = byteReader.process();
				if (processStatus != ProcessStatus.DONE) {
					return processStatus;
				}
				status = byteReader.get();
				byteReader.reset();
				data = Data.createDataPrivateConnectionReponse(StandardOperation.PRIVATE_CONNEXION, step, firstClient, secondClient, status);
				state = State.DONE;
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
		state = State.WAITING_FIRST_CLIENT;
		byteReader.reset();
		stringReader.reset();
		
	}

}
