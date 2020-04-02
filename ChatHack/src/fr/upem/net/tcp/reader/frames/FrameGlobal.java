package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;
import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.StringReader;

public class FrameGlobal implements Reader<Data> {
	
	private enum State {
		DONE, WAITING_LOGIN, WAITING_READER, ERROR;
	}
	
	private State state = State.WAITING_LOGIN;
	private StringReader stringReader;
	private Data data;
	private String login;
	private String message;
	
	public FrameGlobal(ByteBuffer bb) {
		this.stringReader = new StringReader(bb);
	}
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalArgumentException();
		}
		ProcessStatus ps;
		switch (state) {
			case WAITING_LOGIN:
				ps = stringReader.process();
				if (ps != ProcessStatus.DONE) {
					return ps;
				}
				login = stringReader.get();
				state = State.WAITING_READER;
				stringReader.reset();
			case WAITING_READER:
				ps = stringReader.process();
				if (ps != ProcessStatus.DONE) {
					return ps;
				}
				message = stringReader.get();
				stringReader.reset();
				state = State.DONE;
				data = Data.createDataGlobalClient(StandardOperation.GLOBAL_MESSAGE, login, message);
				return ProcessStatus.DONE;
			default:
				throw new AssertionError();
		}
	}

	@Override
	public Data get() {
		if (state != State.DONE) {
			throw new AssertionError();
		}
		return data;
	}

	@Override
	public void reset() {
		state = State.WAITING_LOGIN;
		stringReader.reset();
	}

}
