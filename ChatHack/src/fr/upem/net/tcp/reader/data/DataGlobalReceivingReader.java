package fr.upem.net.tcp.reader.data;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;
import fr.upem.net.tcp.reader.basics.StringReader;

public class DataGlobalReceivingReader implements Reader<Data> {

	
	private enum State {
		DONE, WAITING_LOGIN, WAITING_TEXT, ERROR
	}
	
	private State state = State.WAITING_LOGIN;
	private final ByteReader byteReader;
	private final StringReader stringReader;
	private byte step;
	private String text;
	private String login;
	private Data data;
	
	/**
	 * @param bb
	 */
	public DataGlobalReceivingReader(ByteBuffer bb, byte step) {
		this.stringReader = new StringReader(bb);
		this.byteReader = new ByteReader(bb);
		this.step = step;
	}

	@Override
	public ProcessStatus process() {
		switch (state) {
		case WAITING_LOGIN:
			ProcessStatus loginStatus = stringReader.process();
			if (loginStatus != ProcessStatus.DONE) {
				return loginStatus;
			}
			login = stringReader.get();
			stringReader.reset();
			state = State.WAITING_TEXT;
			
		case WAITING_TEXT:
			ProcessStatus textStatus = stringReader.process();
			if (textStatus != ProcessStatus.DONE) {
				return textStatus;
			}
			text = stringReader.get();
			stringReader.reset();
			state = State.DONE;
			data = Data.createDataGlobalServer(StandardOperation.GLOBAL_MESSAGE, step, login, text);
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
		byteReader.reset();
		
	};
}
